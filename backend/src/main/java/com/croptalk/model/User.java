package com.croptalk.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Mongoose model "Users" (models/userModel.js) -> collection "users". */
@Document(collection = "users")
public class User {
    @Id
    @JsonProperty("_id")
    public ObjectId id;

    // Index names match the ones Mongoose creates so an existing database does not raise an index-name conflict.
    @Indexed(unique = true, name = "clerkid_1")
    public String clerkid;          // Clerk user ID

    public String email;

    @Indexed(unique = true, name = "username_1")
    public String username;

    public String bio = "";

    public List<Connection> connections = new ArrayList<>();

    public List<ConnectionRequest> connectionRequests = new ArrayList<>();

    @CreatedDate
    public Date createdAt;          // { timestamps: true }

    @LastModifiedDate
    public Date updatedAt;

    @Field("__v")
    @JsonProperty("__v")
    public int v = 0;

    /** connections[] sub-document. */
    public static class Connection {
        @Id
        @JsonProperty("_id")
        public ObjectId id = new ObjectId();

        public String userId;                   // ID of the connected user
        public String status = "pending";       // enum: pending | accepted
        public Date connectedAt = new Date();

        public Connection() {}

        public Connection(String userId, String status) {
            this.userId = userId;
            this.status = status;
        }
    }

    /** connectionRequests[] sub-document. */
    public static class ConnectionRequest {
        @Id
        @JsonProperty("_id")
        public ObjectId id = new ObjectId();

        public String senderId;                 // ID of the person who sent the request
        public String senderName;               // Name of the sender
        public Date timestamp = new Date();     // When the request was sent

        public ConnectionRequest() {}

        public ConnectionRequest(String senderId, String senderName) {
            this.senderId = senderId;
            this.senderName = senderName;
        }
    }
}
