package com.croptalk.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.croptalk.model.Post;

public interface PostRepository extends MongoRepository<Post, ObjectId> {
}
