package controller;

import com.sun.net.httpserver.HttpExchange;
import model.Post;
import server.Server;
import service.PostService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PostController implements Server.Controller {
    private final PostService postService;
    private static final String API_POSTS_PATH = "/api/posts";
    private static final Pattern ID_PATTERN = Pattern.compile(API_POSTS_PATH + "/([0-9]+)");

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Override
    public void registerHandlers(com.sun.net.httpserver.HttpServer server) {
        // Register the handler for all /api/posts URLs
        server.createContext(API_POSTS_PATH, this::handleRequest);
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            // Route to the appropriate handler based on the request URL and method
            if (path.equals(API_POSTS_PATH)) {
                switch (method) {
                    case "GET" -> getAllPosts(exchange);
                    case "POST" -> createPost(exchange);
                    default -> sendMethodNotAllowed(exchange);
                }
            } else {
                // Pattern matching for /api/posts/{id}
                Matcher matcher = ID_PATTERN.matcher(path);
                if (matcher.matches()) {
                    Long id = Long.parseLong(matcher.group(1));

                    switch (method) {
                        case "GET" -> getPost(exchange, id);
                        case "PUT" -> updatePost(exchange, id);
                        case "DELETE" -> deletePost(exchange, id);
                        default -> sendMethodNotAllowed(exchange);
                    }
                } else {
                    sendNotFound(exchange);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange, e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private void getAllPosts(HttpExchange exchange) throws IOException {
        List<Post> posts = postService.getAllPosts();
        String postsJson = posts.stream()
                .map(Post::toJson)
                .collect(Collectors.joining(",", "[", "]"));

        sendJsonResponse(exchange, 200, postsJson);
    }

    private void getPost(HttpExchange exchange, Long id) throws IOException {
        Post post = postService.getPostById(id);

        if (post != null) {
            sendJsonResponse(exchange, 200, post.toJson());
        } else {
            sendJsonResponse(exchange, 404, "{\"error\":\"Post not found\"}");
        }
    }

    private void createPost(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        Post post = Post.fromJson(requestBody);

        if (post.getTitle() == null || post.getTitle().isEmpty() ||
                post.getBody() == null || post.getBody().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Title and body are required\"}");
            return;
        }

        Post createdPost = postService.createPost(post);
        sendJsonResponse(exchange, 201, createdPost.toJson());
    }

    private void updatePost(HttpExchange exchange, Long id) throws IOException {
        Post existingPost = postService.getPostById(id);

        if (existingPost == null) {
            sendJsonResponse(exchange, 404, "{\"error\":\"Post not found\"}");
            return;
        }

        String requestBody = readRequestBody(exchange);
        Post updatedPost = Post.fromJson(requestBody);
        updatedPost.setId(id);

        if (updatedPost.getTitle() == null || updatedPost.getTitle().isEmpty() ||
                updatedPost.getBody() == null || updatedPost.getBody().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Title and body are required\"}");
            return;
        }

        Post result = postService.updatePost(updatedPost);
        sendJsonResponse(exchange, 200, result.toJson());
    }

    private void deletePost(HttpExchange exchange, Long id) throws IOException {
        boolean deleted = postService.deletePost(id);

        if (deleted) {
            sendJsonResponse(exchange, 204, "");
        } else {
            sendJsonResponse(exchange, 404, "{\"error\":\"Post not found\"}");
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        if (responseBody.isEmpty()) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            byte[] responseBytes = responseBody.getBytes();
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        }
    }

    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        String response = "{\"error\":\"Method not allowed\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(405, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
    }

    private void sendNotFound(HttpExchange exchange) throws IOException {
        String response = "{\"error\":\"Resource not found\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(404, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
    }

    private void sendServerError(HttpExchange exchange, String message) throws IOException {
        String response = "{\"error\":\"Internal server error\",\"message\":\"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(500, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
    }
}