package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int port;
    private final com.sun.net.httpserver.HttpServer server;
    private final List<Controller> controllers = new ArrayList<>();

    // Interface for controllers
    public interface Controller {
        void registerHandlers(com.sun.net.httpserver.HttpServer server);
    }

    public Server(int port) throws IOException {
        this.port = port;
        this.server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
    }

    public void registerController(Controller controller) {
        controllers.add(controller);
        controller.registerHandlers(server);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}