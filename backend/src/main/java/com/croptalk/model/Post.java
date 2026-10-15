package com.croptalk.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Mongoose model "postdata" (models/postdata.js) -> collection "postdatas". */
@Document(collection = "postdatas")
public class Post {
    @Id
    @JsonProperty("_id")
    public ObjectId id;

    public String username;

    public String content;

    public List<String> tags = new ArrayList<>();

    public int likes = 0;

    public List<Comment> comments = new ArrayList<>();

    public Date createdAt = new Date();

    @Field("__v")
    @JsonProperty("__v")
    public int v = 0;

    /** comments[] sub-document. */
    public static class Comment {
        @Id
        @JsonProperty("_id")
        public ObjectId id = new ObjectId();

        public ObjectId userId;                 // ref: 'users', required
        public String comment;                  // required, trimmed
        public Date createdAt = new Date();
    }
}
