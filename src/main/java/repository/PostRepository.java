package repository;

import database.DatabaseConnection;
import model.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostRepository {

    public PostRepository() {
        initializeTable();
    }

    private void initializeTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS posts (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                body TEXT NOT NULL
            )
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error initializing database table: " + e.getMessage());
        }
    }

    public Post save(Post post) {
        String sql;

        if (post.getId() == null) {
            // Insert new post
            sql = "INSERT INTO posts (title, body) VALUES (?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, post.getTitle());
                pstmt.setString(2, post.getBody());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            post.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error saving post: " + e.getMessage());
            }
        } else {
            // Update existing post
            sql = "UPDATE posts SET title = ?, body = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, post.getTitle());
                pstmt.setString(2, post.getBody());
                pstmt.setLong(3, post.getId());

                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error updating post: " + e.getMessage());
            }
        }

        return post;
    }

    public Post findById(Long id) {
        String sql = "SELECT * FROM posts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPost(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding post by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Post> findAll() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM posts";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                posts.add(mapResultSetToPost(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all posts: " + e.getMessage());
        }

        return posts;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM posts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting post: " + e.getMessage());
        }
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setBody(rs.getString("body"));
        return post;
    }
}
