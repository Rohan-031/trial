package com.croptalk.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.croptalk.model.User;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByClerkid(String clerkid);

    Optional<User> findByUsername(String username);

    List<User> findByClerkidIn(Collection<String> clerkids);
}
