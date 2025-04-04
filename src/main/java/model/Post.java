package model;

public class Post {
    private Long id;
    private String title;
    private String body;

    public Post() {}

    public Post(Long id, String title, String body) {
        this.id = id;
        this.title = title;
        this.body = body;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    // Convert Post to JSON string
    public String toJson() {
        return "{" +
                "\"id\":" + (id != null ? id : "null") + "," +
                "\"title\":\"" + escape(title) + "\"," +
                "\"body\":\"" + escape(body) + "\"" +
                "}";
    }

    // Simple JSON escaping
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Parse a JSON string to create a Post object
    public static Post fromJson(String json) {
        Post post = new Post();

        // Simple JSON parsing (not robust, but sufficient for this example)
        if (json != null && !json.isEmpty()) {
            String idPattern = "\"id\":(\\d+|null)";
            String titlePattern = "\"title\":\"([^\"]*)\"";
            String bodyPattern = "\"body\":\"([^\"]*)\"";

            var idMatcher = java.util.regex.Pattern.compile(idPattern).matcher(json);
            var titleMatcher = java.util.regex.Pattern.compile(titlePattern).matcher(json);
            var bodyMatcher = java.util.regex.Pattern.compile(bodyPattern).matcher(json);

            if (idMatcher.find()) {
                String idStr = idMatcher.group(1);
                if (!"null".equals(idStr)) {
                    post.setId(Long.parseLong(idStr));
                }
            }

            if (titleMatcher.find()) {
                post.setTitle(titleMatcher.group(1));
            }

            if (bodyMatcher.find()) {
                post.setBody(bodyMatcher.group(1));
            }
        }

        return post;
    }
}
