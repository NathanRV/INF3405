package ca.polymtl.inf3405.server;

import ca.polymtl.inf3405.database.Database;
import ca.polymtl.inf3405.exceptions.DatabaseInsertionException;
import ca.polymtl.inf3405.exceptions.MessageSizeException;
import ca.polymtl.inf3405.exceptions.NoUserException;
import ca.polymtl.inf3405.protocol.request.Request;
import ca.polymtl.inf3405.protocol.requestEncoding.RequestDecoder;
import ca.polymtl.inf3405.protocol.response.*;
import ca.polymtl.inf3405.protocol.responseEncoding.ResponseEncoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ClientHandler implements Runnable {
    private volatile boolean running = true;
    private Map<String, ConnectedUser> connectedUsers;
    private Queue<Message> messagesQueue;
    private final String listeningAddress;
    private final int listeningPort;
    private ServerSocket listeningSocket;
    private RequestDecoder requestDecoder;
    private ResponseEncoder responseEncoder;
    private Database database;

    public ClientHandler(Map<String, ConnectedUser> connectedUsers, Queue<Message> messagesQueue, String listeningAddress,
                         int listeningPort) {
        this.connectedUsers = connectedUsers;
        this.messagesQueue = messagesQueue;
        this.listeningAddress = listeningAddress;
        this.listeningPort = listeningPort;
        this.requestDecoder = new RequestDecoder();
        this.responseEncoder = new ResponseEncoder();
        this.database = Database.getInstance();
    }

    @Override
    public void run() {
        try {
            startListening();
        } catch (IOException e) {
            running = false;
            e.printStackTrace();
        }
        while (running) {
            try {
                handleRequest(listeningSocket.accept());
                listeningSocket.close();
            } catch (IOException e) {
                running = false;
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(Socket clientSocket) throws IOException {
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        int size = in.readInt();

        ByteBuffer inputData = ByteBuffer.allocate(size);
        inputData.putInt(size);
        inputData.put(in.readNBytes(size-Integer.BYTES ));

        Request request = requestDecoder.decodeRequest(inputData.array());
        processRequest(clientSocket, request);
    }

    private void processRequest(Socket clientSocket, Request request) {
        switch (request.getRequest()) {
            case LOG_IN:
                processLogIn(clientSocket, request);
                break;
            case LOG_OUT:
                processLogOut(clientSocket, request);
                break;
            case SEND_MESSAGE:
                processIncomingMessage(clientSocket, request);
                break;
            case REQUEST_NEW_MESSAGES:
                processRequestNewMessages(clientSocket, request);
        }
    }

    private void processLogIn(Socket clientSocket, Request request) {
        int USERNAME = 0;
        int PASSWORD = 1;
        int N_OF_STRINGS = 2;
        Response response;

        String[] credentials = request.getPayload().split("\n", N_OF_STRINGS);
        try {
            User user = database.getUser(credentials[USERNAME]);
            if (user.isGoodPassword(credentials[PASSWORD])) {
                ConnectedUser connectedUser = new ConnectedUser(user);
                connectedUsers.put(connectedUser.getToken(), connectedUser);
                response =  new Response(Responses.AUTHENTICATED, connectedUser.getToken());
            }
            else {
                response = new Response(Responses.WRONG_PASSWORD, "");
            }
        } catch (NoUserException e) {
            User user = new User(credentials[USERNAME]);
            user.setPassword(credentials[PASSWORD]);
            try {
                database.insertNewUser(user);
                response = new Response(Responses.USER_CREATED, "");
            } catch (DatabaseInsertionException de) {
                response = new Response(Responses.ERROR, "");
            }
        }

        sendResponse(clientSocket, response);
    }

    private void processLogOut(Socket clientSocket, Request request) {
        ConnectedUser u = connectedUsers.remove(request.getToken());
        Response response;
        if (u == null) {
            response = new Response(Responses.ERROR, "");
        }
        else {
            response = new Response(Responses.OK, "");
        }

        sendResponse(clientSocket, response);
    }

    private void processIncomingMessage(Socket clientSocket, Request request) {
        ConnectedUser u = connectedUsers.get(request.getToken());
        Response response;

        if (u == null) {
            response = new Response(Responses.WRONG_TOKEN, "");
        }
        else {
            u.updateLastConnection();
            try {
                Message m =  requestDecoder.decodeMessage(request.getPayload());
                if (m.getSenderName().equals(u.getUserName())) {
                    database.insertNewMessage(m);
                    messagesQueue.add(m);
                    response = new Response(Responses.OK, "");
                }
                else {
                    response = new Response(Responses.ERROR, "WrongSendingUser");
                }
            } catch (MessageSizeException e) {
                response = new Response(Responses.ERROR, "MessageSizeException");
            } catch (DatabaseInsertionException e) {
                response = new Response(Responses.ERROR, "DatabaseInsertionException");
            }
        }

        sendResponse(clientSocket, response);
    }

    private void processRequestNewMessages(Socket clientSocket, Request request) {
        int NUMBER_OF_MESSAGES = 15;
        ConnectedUser u = connectedUsers.get(request.getToken());
        Response response;

        if (u == null) {
            response = new Response(Responses.WRONG_TOKEN, "");
        }
        else {
            u.updateLastConnection();
            List<Message> messages = database.getLastMessages(NUMBER_OF_MESSAGES);
            String responsePayload = String.join("\0", (CharSequence) messages);
            response = new Response(Responses.OK, responsePayload);
        }

        sendResponse(clientSocket, response);
    }

    private void sendResponse(Socket clientSocket, Response response) {
        try {
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            out.write(responseEncoder.encodeResponse(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListening() throws IOException {
        listeningSocket = new ServerSocket();
        listeningSocket.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(listeningAddress);
        listeningSocket.bind(new InetSocketAddress(serverIP, listeningPort));
    }

    public void stop() {
        running = false;
    }
}
