package ca.polymtl.inf3405.server;

import ca.polymtl.inf3405.database.Database;
import ca.polymtl.inf3405.exceptions.DatabaseInsertionException;
import ca.polymtl.inf3405.exceptions.NoUserException;
import ca.polymtl.inf3405.protocol.Message;
import ca.polymtl.inf3405.protocol.Request;
import ca.polymtl.inf3405.protocol.Response;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Thread Serveur
 */
public class Serveur extends Thread {
    private volatile static ConcurrentMap<String, ConnectedUser> connectedUsers;
    private volatile static BlockingQueue<Message> messagesQueue;
    private static ServerSocket listener;
    private boolean serverRunning;

    /**
     * Constructeur serveur
     */
    public Serveur() {
        connectedUsers = new ConcurrentHashMap<>();
        messagesQueue = new LinkedBlockingQueue<>();
        serverRunning = false;
    }

    /**
     * Accesseurs de serverRunning
     * @return boolean : serverRunning
     */
    public boolean getServerRunning() {
        return serverRunning;
    }

    /**
     * Fonctions permettant de lancer les validations initiales
     * et d'initialiser le socket d'ecoute.
     */
    private void initiateServer() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String serverAddress = validateIP(reader);
        int serverPort = validatePort(reader);

        try {
            listener = new ServerSocket();
            listener.setReuseAddress(true);
            InetAddress serverIP = InetAddress.getByName(serverAddress);
            listener.bind(new InetSocketAddress(serverIP, serverPort));
        } catch (Exception e) {
        }

        System.out.format("L'adresse et le port du serveur sont %s:%d%n", serverAddress, serverPort);
        serverRunning = true;
    }

    /**
     * Fonctions permettant de valider l'adresse IP entre
     * sur la console.
     *
     * @param reader : BufferedReader (Entree du texte)
     * @return String : Adresse IP valide.
     */
    private String validateIP(BufferedReader reader) {
        System.out.print("Veuillez entrez l'adresse IP du serveur : ");
        String serverAddress = "";
        boolean valid = false;
        String IP_PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        while (!valid) {
            try {
                serverAddress = reader.readLine();
            } catch (IOException e) {
            }
            if (serverAddress.matches(IP_PATTERN)) {
                valid = true;
            } else {
                System.out.print("Adresse IP entree invalide! Veuillez entre une adresse du format XXX.XXX.XXX.XXX : ");
            }
        }
        return serverAddress;
    }

    /**
     * Fonctions permettant de valider le port entre sur
     * la console.
     *
     * @param reader : BufferedReader (Entree du texte)
     * @return int : Port valide
     */
    private int validatePort(BufferedReader reader) {
        System.out.print("Veuillez entrez le port d'ecoute du serveur : ");
        int serverPort = 0;
        boolean valid = false;
        while (!valid) {
            try {
                serverPort = Integer.parseInt(reader.readLine());
            } catch (Exception e) {
                serverPort = 0;
            }
            if (serverPort >= 5000 && serverPort <= 5050) {
                valid = true;
            } else {
                System.out.print("Port invalide! (Veuillez entrez un nombre entre 5000 et 5050) ");
            }
        }

        return serverPort;
    }

    /**
     * Fonction permettant de fermer le socket d'ecoute.
     */
    public void closeSocket() {
        try {
            listener.close();
        } catch (IOException e) {
        }
    }

    /**
     * Fonctions permettant d'accepter les nouveaux clients.
     */
    public void run() {
        initiateServer();
        MessageHandler messageHandler = new MessageHandler(connectedUsers, messagesQueue);
        messageHandler.start();

        try {
            while (true) {
                ClientHandler client = new ClientHandler(listener.accept(), connectedUsers, messagesQueue);
                client.start();
            }
        } catch (IOException e) {
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Thread de gestion de clients
     */
    private class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream reader;
        private DataOutputStream writer;
        private Database database;
        private volatile ConcurrentMap<String, ConnectedUser> connectedUsers;
        private volatile BlockingQueue<Message> messagesQueue;

        /**
         * Constructeur de gestionnaire client
         *
         * @param socket : Socket (Port de connexion)
         * @param connectedUsers : ConcurrentMap<String, ConnectedUser>
         * @param messagesQueue : BlockingQueue<Message>
         */
        public ClientHandler(Socket socket, ConcurrentMap<String, ConnectedUser> connectedUsers,
                             BlockingQueue<Message> messagesQueue) {
            this.socket = socket;
            try {
                this.reader = new DataInputStream(socket.getInputStream());
                this.writer = new DataOutputStream(socket.getOutputStream());
                this.database = Database.getInstance();
                this.connectedUsers = connectedUsers;
                this.messagesQueue = messagesQueue;
            } catch (IOException e) {
            }
        }

        /**
         * Fonctions permettant de recevoir les requetes.
         */
        public void run() {
            try {
                Request request = Request.decodeRequest(reader.readUTF());
                processRequest(request);
            } catch (IOException e) {
            }
        }

        /**
         * Fonctions de gestions des requetes.
         *
         * @param request : Request (requete)
         */
        private void processRequest(Request request) {
            switch (request.getRequest()) {
                case "LOG_IN":
                    processLogIn(request);
                    break;
                case "LOG_OUT":
                    processLogOut(request);
                    break;
                case "NEW_MESSAGE":
                    processNewMessage(request);
                    break;
                case "GET_MESSAGES":
                    processMessagesRequest(request);
                    break;
                default:
                    sendErrorResponse(Map.of("Type", "Wrong request"));
            }
        }

        /**
         * Gestion requete de connexion.
         *
         * @param request : Request (requete)
         */
        private void processLogIn(Request request) {
            String username = request.getPayload().get("username");
            String password = request.getPayload().get("password");
            String port = request.getPayload().get("listening_port");
            if (username == null || password == null) {
                sendErrorResponse(Map.of("Type", "No username"));
                return;
            }
            int listeningClientPort;
            try {
                listeningClientPort = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                sendErrorResponse(Map.of("Type", "Wrong port format"));
                return;
            }
            if (listeningClientPort < 1025 || listeningClientPort > 49152) {
                sendErrorResponse(Map.of("Type", "Wrong port number"));
                return;
            }

            try {
                User user = database.getUser(username);
                if (user.isGoodPassword(password)) {
                    ConnectedUser connectedUser = new ConnectedUser(user, socket.getInetAddress(), listeningClientPort);
                    connectedUsers.put(connectedUser.getToken(), connectedUser);
                    Response response = new Response("OK", Map.of("Token", connectedUser.getToken()));
                    sendResponse(response);
                } else {
                    sendErrorResponse(Map.of("Type", "Wrong password"));
                }
            } catch (NoUserException e) {
                processNewUser(request, username, password, listeningClientPort);
            }
        }

        /**
         * Fonction de creation de nouveau utilisateur.
         *
         * @param request : Request (requete)
         * @param username : String (nom d'utilisateur)
         * @param password : String (mot de passe)
         * @param port : int (Port de connexion)
         */
        private void processNewUser(Request request, String username, String password, int port) {
            User user = new User(username);
            user.setPassword(password);
            try {
                database.insertNewUser(user);
            } catch (DatabaseInsertionException e) {
                sendErrorResponse(Map.of("Type", "Database insertion error"));
            }
            ConnectedUser connectedUser = new ConnectedUser(user, socket.getInetAddress(), port);
            connectedUsers.put(connectedUser.getToken(), connectedUser);
            Response response = new Response("OK", Map.of("NewUser", "true", "Token",
                    connectedUser.getToken()));
            sendResponse(response);
        }

        /**
         * Fonction de gestion requete de deconnexion.
         *
         * @param request : Request (requete)
         */
        private void processLogOut(Request request) {
            String token = request.getToken();
            ConnectedUser connectedUser = connectedUsers.remove(token);
            if (connectedUser == null) {
                sendErrorResponse(Map.of("Type", "Wrong token"));
            } else {
                Response response = new Response("OK", Map.of("username", connectedUser.getUserName()));
                sendResponse(response);
            }
        }

        /**
         * Fonction gestion requete nouveau message.
         *
         * @param request : Request (requete)
         */
        private void processNewMessage(Request request) {
            ConnectedUser connectedUser = connectedUsers.get(request.getToken());
            if (connectedUser == null) {
                sendErrorResponse(Map.of("Type", "Wrong token"));
            } else {
                String encodedMessage = request.getPayload().get("Message");
                if (encodedMessage == null) {
                    sendErrorResponse(Map.of("Type", "No message"));
                    return;
                }
                Message message = Message.decodeMessage(encodedMessage);
                if (message == null) {
                    sendErrorResponse(Map.of("Type", "Wrong message formatting"));
                    return;
                }
                messagesQueue.add(message);
                Response response = new Response("OK", Map.of("username", connectedUser.getUserName()));
                sendResponse(response);
            }
        }

        /**
         * Fonction de gestion requete d'anciens messages.
         *
         * @param request : Request (requete)
         */
        private void processMessagesRequest(Request request) {
            ConnectedUser connectedUser = connectedUsers.get(request.getToken());
            if (connectedUser == null) {
                sendErrorResponse(Map.of("Type", "Wrong token"));
            } else {
                int NUMBER_OF_MESSAGES = 15;
                List<Message> messages = database.getLastMessages(NUMBER_OF_MESSAGES);
                HashMap<String, String> payload = new HashMap<>();
                int i = 0;

                payload.put("size", Integer.toString(messages.size()));
                for (Message m : messages) {
                    payload.put(Integer.toString(i++), m.encodeMessage());
                }
                Response response = new Response("OK", payload);
                sendResponse(response);
            }
        }

        /**
         * Fonction d'envoi de message d'erreur.
         *
         * @param payload : Map<String, String>
         */
        private void sendErrorResponse(Map<String, String> payload) {
            Response response = new Response("ERROR", payload);
            sendResponse(response);
        }

        /**
         * Fonction d'envoi de reponse.
         *
         * @param response : Response (reponse)
         */
        private void sendResponse(Response response) {
            try {
                writer.writeUTF(response.encodeResponse());
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Thread de gestion de message.
     */
    private class MessageHandler extends Thread {
        private volatile ConcurrentMap<String, ConnectedUser> connectedUsers;
        private volatile BlockingQueue<Message> messagesQueue;
        private volatile Boolean running = true;
        private Database database;

        /**
         * Constructeur de gestionnaire de message.
         *
         * @param connectedUsers : ConcurrentMap<String,ConnectedUser>
         * @param messagesQueue : BlockingQueue<Message>
         */
        public MessageHandler(ConcurrentMap<String, ConnectedUser> connectedUsers,
                              BlockingQueue<Message> messagesQueue) {
            this.connectedUsers = connectedUsers;
            this.messagesQueue = messagesQueue;
            database = Database.getInstance();
        }

        /**
         * Fonction run cherchant message
         * dans la queue et appellant sendMessage.
         */
        public void run() {
            Message message;
            while (running) {
                try {
                    message = messagesQueue.take();
                    sendMessage(message);
                } catch (InterruptedException e) {
                }
            }
        }

        /**
         * Fonction d'envoi de message.
         *
         * @param message : Message
         */
        private void sendMessage(Message message) {
            Map<String, ConnectedUser> users = Collections.unmodifiableMap(connectedUsers);
            users.forEach((k, u) -> {
                MessageSender sender = new MessageSender(u, message);
                sender.start();
            });
            try {
                database.insertNewMessage(message);
            } catch (DatabaseInsertionException e) {
            }
            String fancyPrint = String.format(
                    "[%s - %s:%d - %s]: %s",
                    message.getSenderName(),
                    message.getSenderIp(),
                    message.getSenderPort(),
                    message.getTime(),
                    message.getMessage()
            );
            System.out.println(fancyPrint);
        }

        /**
         * Fonction permettant d'arrêter le thread.
         */
        public void terminate() {
            running = false;
        }
    }

    /**
     * Thread d'envoi de message.
     */
    private class MessageSender extends Thread {
        private final ConnectedUser user;
        private final Message message;

        /**
         * Constructeur du thread.
         *
         * @param user : ConnectedUser
         * @param message : Message
         */
        MessageSender(ConnectedUser user, Message message) {
            this.user = user;
            this.message = message;
        }

        /**
         * Fonction qui envoie le message.
         */
        public void run() {
            try {
                Socket socket = new Socket(user.getUserAddress(), user.getUserPort());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                output.writeUTF(message.encodeMessage());
            } catch (IOException e) {
            }
        }
    }

    /**
     * Fonction principale offrant menu pour
     * fermer le programme ou le relancer.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Serveur serveur = new Serveur();
        String choice = "start";
        while (!choice.matches("quit")) {
            if (choice.matches("restart") || choice.matches("start")) {
                if (choice.matches("restart")) {
                    System.out.println("Fermeture du serveur...");
                    serveur.closeSocket();
                    serveur.interrupt();
                    serveur = new Serveur();
                    System.out.println("SUCCES");
                }
                serveur.setDaemon(true);
                serveur.start();
                while (!serveur.getServerRunning()) {
                    Thread.sleep(100);
                }
            }
            System.out.println(
                    "Fermer le programme: quit\n" +
                            "Relancer serveur: restart"
            );
            choice = reader.readLine();
        }
        serveur.interrupt();
    }
}
