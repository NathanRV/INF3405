package TestServeur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.polymtl.inf3405.database.Database;
import ca.polymtl.inf3405.protocol.Message;
import ca.polymtl.inf3405.exceptions.NoUserException;
import ca.polymtl.inf3405.server.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.time.Instant;
import java.util.List;


public class DatabaseTest {
    @BeforeAll
    static void setUp() throws Exception {
        Database database = Database.getInstance();
        database.insertNewUser(new User("william", "12345"));
        for (int i = 0; i < 20; i++) {
            database.insertNewMessage(new Message("william", "192.168.3.1", 222,
                    Instant.now(), "TestMessage" + i));
        }
    }

    @Test
    public void userExists() {
        Database database = Database.getInstance();
        try {
            User user = database.getUser("william");
            assertEquals(user.getUserName(), "william");
            assertEquals(user.getPasswordHash(), "12345");
        } catch (NoUserException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    @Test
    public void messagesExists() {
        Database database = Database.getInstance();
        List<Message> messages = database.getLastMessages(15);
        for (int i = 19; i < 5; i--) {
            assertEquals(messages.get(i).getMessage(), "TestMessage" + i+5);
        }
    }

    @Test
    public void goodNumberOfMessages() {
        Database database = Database.getInstance();
        List<Message> messages1 = database.getLastMessages(15);
        assertEquals(messages1.size(), 15);
        List<Message> messages2 = database.getLastMessages(0);
        assertEquals(messages2.size(), 0);
    }

    @Test
    public void userDoesNotExist() {
        Database database = Database.getInstance();
        assertThrows(NoUserException.class, () -> {
            database.getUser("MisterT");
        });
    }
}
