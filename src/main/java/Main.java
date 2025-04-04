import controller.PostController;
import database.DatabaseConnection;
import server.Server;
import service.PostService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        int port = 8080;

        // Initialize services
        PostService postService = new PostService();
        PostController postController = new PostController(postService);

        // Create and start the HTTP server
        try {
            Server server = new Server(port);

            // Register controllers
            server.registerController(postController);

            // Start the server
            server.start();
            System.out.println("Server started on port " + port);

            // Add shutdown hook to close resources gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.stop();
                DatabaseConnection.closeConnection();
                System.out.println("Server stopped");
            }));
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
