package client;

import exceptions.MessageSizeException;
import protocol.Message;
import protocol.Request;
import protocol.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private String token;
    private String username;

    public Client() {
        token = "";
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    private String validateIP(BufferedReader reader) {
        System.out.print("Veuillez entrez l'adresse IP du serveur : ");
        String serverAddress = "";
        boolean valid = false;
        String IP_PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        while (!valid) {
            try {
                serverAddress = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (serverAddress.matches(IP_PATTERN)) {
                    valid = true;
                } else {
                    System.out.print("Adresse IP entree invalide! Veuillez entre une adresse du format XXX.XXX.XXX.XXX : ");
                }
            }
        }
        return serverAddress;
    }

    private int validatePort(BufferedReader reader) {
        System.out.print("Veuillez entrez le port d'ecoute du serveur : ");
        int serverPort = 0;
        boolean valid = false;
        while (!valid) {
            try {
                serverPort = Integer.parseInt(reader.readLine());
            } catch (NumberFormatException e) {
                serverPort = 0;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (serverPort >= 5000 && serverPort <= 5050) {
                    valid = true;
                } else {
                    System.out.print("Port invalide! (Veuillez entrez un nombre entre 5000 et 5050) ");
                }
            }
        }

        return serverPort;
    }

    private Map<String, String> sendRequest(String serverAddress, int serverPort, String requestID, Map<String, String> payload) {
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            Request request = new Request(
                    requestID,
                    token,
                    payload
            );
            outputStream.writeUTF(request.encodeRequest());
            socket.close();
            Response response = Response.decodeResponse(inputStream.readUTF());
            if (response.getResponse().equals("OK")) {
                return response.getPayload();
            } else {
                System.out.println("Error: " + response.getPayload().get("Type"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }


    private void login(BufferedReader reader, String serverAddress, int serverPort) {
        try {
            System.out.print("Veuillez entrer le nom d'utilisateur : ");
            username = reader.readLine();
            System.out.print("Veuillez entrer le mot de passe : ");
            String password = reader.readLine();
            ServerSocket listeningSocket = new ServerSocket(0);
            int listeningPort = listeningSocket.getLocalPort();
            listeningSocket.setReuseAddress(true);
            Map<String, String> requestPayload = Map.of(
                    "listening_port", Integer.toString(listeningPort),
                    "username", username,
                    "password", password
            );
            Map<String, String> responsePayload = sendRequest(serverAddress, serverPort, "LOG_IN", requestPayload);
            token = responsePayload.get("Token") != null ? responsePayload.get("Token") : "";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logout(BufferedReader reader, String serverAddress, int serverPort) {
        Map<String, String> requestPayload = Map.of();
        Map<String, String> responsePayload = sendRequest(serverAddress, serverPort, "LOG_OUT", requestPayload);
        String username = responsePayload.get("username");
        if (username != null) {
            token = "";
            System.out.print("Logout avec succes: " + username);
        }
    }

    private void printLastMessages(BufferedReader reader, String serverAddress, int serverPort) {
        Map<String, String> requestPayload = Map.of();
        Map<String, String> responsePayload = sendRequest(serverAddress, serverPort, "GET_MESSAGES", requestPayload);
        int size = Integer.parseInt(responsePayload.get("size"));
        Message message;
        for (int i = 1; i <= size; ++i) {
            message = Message.decodeMessage(responsePayload.get(Integer.toString(size - i)));
            if (message != null) {
                System.out.println(message.toConsole());
            }
        }
    }

    private boolean selectAction(BufferedReader reader, String serverAddress, int serverPort) {
        if (token.matches("")) {
            login(reader, serverAddress, serverPort);
            printLastMessages(reader, serverAddress, serverPort);
            ReadMessage messageListener = new ReadMessage();
            messageListener.start();
            return false;
        } else {
            Map<String, Runnable> actionMap = Map.of(
                    "a", () -> new SendMessage(serverAddress, serverPort).start(),
                    "b", () -> logout(reader, serverAddress, serverPort)
            );
            System.out.print(
                    "Veuillez selectionner une action\n" +
                            "   a) Nouveau message\n" +
                            "   b) Logout\n" +
                            "   c) Fermer" +
                            "Selection: "
            );
            try {
                String action = reader.readLine();
                if (action.matches("c")) {
                    return true;
                } else {
                    actionMap.get(action).run();
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        }
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String serverAddress = validateIP(reader);
        int serverPort = validatePort(reader);
        boolean quit = false;
        while (!quit) {
            quit = selectAction(reader, serverAddress, serverPort);
        }
    }

    private class SendMessage extends Thread {
        String serverAddress;
        int serverPort;

        public SendMessage(String serverAddress, int serverPort) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        public void run() {
            String inputMessage;
            Message message;
            Request request;

            while (true) {
                try {
                    inputMessage = userIn.readLine();
                    serverSocket = new Socket(serverAddress, serverPort);
                    DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());
                    DataInputStream inputStream = new DataInputStream(serverSocket.getInputStream());

                    try {
                        message = new Message(user, serverSocket.getInetAddress().toString(), serverSocket.getLocalPort(),
                                Instant.now(), inputMessage);
                        request = new Request("NEW_MESSAGE", token, Map.of("Message", message.encodeMessage()));
                        outputStream.writeUTF(request.encodeRequest());
                        Response response = Response.decodeResponse(inputStream.readUTF());
                    } catch (MessageSizeException e) {
                        System.out.println("Erreur: la taille du message doit être de 200 caractères ou moins.");
                    } finally {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    System.out.println("Erreur dans l'envoi du message! Déconnexion.");
                }
            }
        }
    }

    private class ReadMessage extends Thread {
        private boolean running;

        public ReadMessage() {
            running = true;
        }

        public void run() {
            while (running) {
                try {
                    Socket currentServerSocket = listeningSocket.accept();
                    DataInputStream inputStream = new DataInputStream(currentServerSocket.getInputStream());
                    Message message = Message.decodeMessage(inputStream.readUTF());
                    if (message != null) {
                        System.out.println(message.toConsole());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        listeningSocket.close();
                        listeningSocket = new ServerSocket(listeningPort);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void terminate() {
            running = false;
        }
    }


    public static void main(String[] args) throws Exception
    {
        Client client = new Client();
        client.run();
    }
}
