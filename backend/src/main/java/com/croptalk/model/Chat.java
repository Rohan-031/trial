package com.croptalk.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Mongoose model "Chat" (models/chatdata.js) -> collection "chats". */
@Document(collection = "chats")
public class Chat {
    @Id
    @JsonProperty("_id")
    public ObjectId id;

    public String senderId;
    public String receiverId;
    public String message;
    public Date timestamp = new Date();

    @Field("__v")
    @JsonProperty("__v")
    public int v = 0;
}
