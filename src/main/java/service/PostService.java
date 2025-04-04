package service;

import model.Post;
import repository.PostRepository;

import java.util.List;

public class PostService {
    private final PostRepository postRepository;

    public PostService() {
        this.postRepository = new PostRepository();
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post updatePost(Post post) {
        return postRepository.save(post);
    }

    public boolean deletePost(Long id) {
        Post post = postRepository.findById(id);
        if (post != null) {
            postRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
