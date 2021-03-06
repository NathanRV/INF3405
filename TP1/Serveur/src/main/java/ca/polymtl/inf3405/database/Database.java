package ca.polymtl.inf3405.database;

import ca.polymtl.inf3405.protocol.Message;
import ca.polymtl.inf3405.server.User;
import ca.polymtl.inf3405.exceptions.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui permet l'accès à la base de données SQLite.
 */
public class Database {
    private static volatile Database instance;
    private static final String createUsersQuery = "CREATE TABLE IF NOT EXISTS users (" +
            "username TEXT PRIMARY KEY, hash TEXT NOT NULL )";
    private static final String createMessagesQuery = "CREATE TABLE IF NOT EXISTS messages (" +
            "message_id INTEGER PRIMARY KEY, sender_name TEXT NOT NULL, sender_ip TEXT NOT NULL," +
            "sender_port INTEGER, time TEXT NOT NULL, message TEXT NOT NULL )";
    private static final String path = "messenger.sqlite";

    /**
     * Constructeur privé (Singleton) qui vérifie la capacité d'effectuer une connection avec le fichier contenant la
     * base de données.
     */
    private Database() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate(createUsersQuery);
            statement.executeUpdate(createMessagesQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Getter permettant d'avoir une instance de la base de données
     * @return l'instance de la base de données
     */
    public static Database getInstance() {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    instance = new Database();
                }
            }
        }
        return instance;
    }

    /**
     * Méthode permettant l'insertion d'un nouveau message dans la base de données
     * @param m                            le message  à insérer
     * @throws DatabaseInsertionException  lorsqu'il y a une erreur d'insertion
     */
    public synchronized void insertNewMessage(Message m) throws DatabaseInsertionException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:messenger.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("INSERT OR IGNORE INTO messages (sender_name, sender_ip, sender_port, time, message) " +
                    "VALUES (" + String.join(",", "\"" + m.getSenderName() + "\"", "\"" + m.getSenderIp() + "\"",
                    "\"" + m.getSenderPort().toString() + "\"", "\"" + m.getTime().toString() + "\"", "\"" + m.getMessage() + "\"") + ")");
        } catch (SQLException e) {
            throw new DatabaseInsertionException(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Méthode permettant l'insertion d'un nouveau utilisateur dans la base de données
     * @param u                            l'utilisateur à ajouter
     * @throws DatabaseInsertionException  lorsqu'il y a une erreur d'insertion
     */
    public synchronized void insertNewUser(User u) throws DatabaseInsertionException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:messenger.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("INSERT OR IGNORE INTO users (username, hash) VALUES (" +
                    String.join(",", "\"" + u.getUserName() + "\"", "\"" + u.getPasswordHash() + "\"") + ");");
        } catch (SQLException e) {
            throw new DatabaseInsertionException(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Méthode permettant d'obtenir un utilisateur stocké dans la base de données
     * @param username          le nom de l'utilisateur à trouver
     * @return                  l'utilisateur trouvé
     * @throws NoUserException  lorsque l'utilisateur n'existe pas
     */
    public synchronized User getUser(String username) throws NoUserException {
        Connection connection = null;
        String passwordHash = "";

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:messenger.sqlite");
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("SELECT * FROM users WHERE username = '" + username + "'");
            if (!rs.next()) {
                throw new NoUserException("No user named " + username);
            }
            passwordHash = rs.getString("hash");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return new User(username, passwordHash);
    }

    /**
     * Méthode qui permet de récupérer un nombre d'anciens messages dans la base de données
     * @param numberOfMessages le nombre de messages à retourner
     * @return                 les messages
     */
    public synchronized List<Message> getLastMessages(Integer numberOfMessages) {
        Connection connection = null;
        List<Message> messages = new ArrayList<>(numberOfMessages);

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:messenger.sqlite");
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("SELECT * FROM messages ORDER BY message_id DESC LIMIT " +
                    numberOfMessages.toString());

            while (rs.next()) {
                String senderName = rs.getString("sender_name");
                String senderIp = rs.getString("sender_ip");
                Integer senderPort = rs.getInt("sender_port");
                Instant time = Instant.parse(rs.getString("time"));
                String message = rs.getString("message");
                messages.add(new Message(senderName, senderIp, senderPort, time, message));
            }
        } catch (SQLException | MessageSizeException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }
}
