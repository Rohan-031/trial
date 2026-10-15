package com.croptalk.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.croptalk.model.Chat;

public interface ChatRepository extends MongoRepository<Chat, ObjectId> {
    @Query("{ $or: [ { senderId: ?0, receiverId: ?1 }, { senderId: ?1, receiverId: ?0 } ] }")
    List<Chat> findBetweenUsers(String senderId, String receiverId, Sort sort);
}
