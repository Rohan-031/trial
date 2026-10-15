package com.croptalk.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.croptalk.model.Post;
import com.croptalk.service.PostService;
import com.croptalk.util.JsonUtil;

/**
 * Port of routes/posts.js (mounted at /api/posts).
 *
 * Note (preserved from the source): the /like and /comment routes are declared without a ":postId" segment,
 * yet their handlers read req.params.postId, which is therefore always undefined.
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(@RequestBody(required = false) Map<String, Object> body) {
        return postService.createPost(JsonUtil.orEmpty(body));
    }

    @GetMapping("/getdata")
    public List<Post> getPosts() {
        return postService.getPosts();
    }

    @GetMapping("/like")
    public Post likePost() {
        return postService.likePost(null);
    }

    @GetMapping("/comment")
    public Post addComment(@RequestBody(required = false) Map<String, Object> body) {
        return postService.addComment(null, JsonUtil.orEmpty(body));
    }
}
