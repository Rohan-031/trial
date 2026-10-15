package com.croptalk.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croptalk.exception.ApiException;
import com.croptalk.model.Post;
import com.croptalk.repository.PostRepository;
import com.croptalk.util.JsonUtil;
import com.croptalk.util.Responses;

/**
 * Port of controllers/postcontroller.js. As in the original (express-async-handler), unexpected errors
 * propagate to the global handler and produce a 500.
 */
@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /** createPost */
    public Post createPost(Map<String, Object> body) {
        Object username = body.get("username");
        Object content = body.get("content");
        Object createdAt = body.get("createdAt");

        if (JsonUtil.isFalsy(username)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("No username"));
        }
        if (JsonUtil.isFalsy(content)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("Content is required"));
        }

        Post post = new Post();
        post.username = JsonUtil.asString(username);
        post.content = JsonUtil.asString(content).trim();           // schema: trim: true
        if (post.content.isEmpty()) {
            throw new IllegalArgumentException("Path `content` is required.");   // Mongoose ValidationError
        }
        post.createdAt = JsonUtil.isFalsy(createdAt) ? new Date() : toDate(createdAt);

        return postRepository.save(post);
    }

    /** getPosts - newest first */
    public List<Post> getPosts() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /** likePost */
    public Post likePost(String postId) {
        Post post = findPost(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, Responses.message("Post not found"));
        }
        post.likes = post.likes + 1;
        return postRepository.save(post);
    }

    /** addComment */
    public Post addComment(String postId, Map<String, Object> body) {
        Object comment = body.get("comment");
        Object userId = body.get("userId");

        if (JsonUtil.isFalsy(comment)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("Comment is required"));
        }

        Post post = findPost(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, Responses.message("Post not found"));
        }

        Post.Comment newComment = new Post.Comment();
        // userId is a required ObjectId in the schema: null/invalid values fail validation (-> 500)
        if (JsonUtil.isFalsy(userId)) {
            throw new IllegalArgumentException("Path `userId` is required.");
        }
        newComment.userId = new ObjectId(JsonUtil.asString(userId));
        newComment.comment = JsonUtil.asString(comment).trim();
        if (newComment.comment.isEmpty()) {
            throw new IllegalArgumentException("Path `comment` is required.");
        }
        newComment.createdAt = new Date();
        post.comments.add(newComment);

        return postRepository.save(post);
    }

    /** Model.findById(id): an undefined id resolves to null; a malformed id is a CastError (-> 500). */
    private Post findPost(String postId) {
        if (postId == null) return null;
        return postRepository.findById(new ObjectId(postId)).orElse(null);
    }

    /** Mongoose casts the request's createdAt (ISO string / epoch millis) to a Date. */
    private Date toDate(Object value) {
        if (value instanceof Number n) return new Date(n.longValue());
        String s = JsonUtil.asString(value);
        try {
            return Date.from(Instant.parse(s));
        } catch (Exception ignored) {
            try {
                return Date.from(OffsetDateTime.parse(s).toInstant());
            } catch (Exception e) {
                throw new IllegalArgumentException("Cast to date failed for value \"" + s + "\"");
            }
        }
    }
}
