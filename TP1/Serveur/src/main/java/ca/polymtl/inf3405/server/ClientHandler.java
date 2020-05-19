package ca.polymtl.inf3405.server;

public class ClientHandler implements Runnable {
    private volatile boolean running = true;

    public ClientHandler() {}

    @Override
    public void run() {
    }

    public void stop() {
        running = false;
    }
}
