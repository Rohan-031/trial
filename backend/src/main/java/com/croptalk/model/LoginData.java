package com.croptalk.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mongoose model "logindata" (models/logindata.js) -> collection "logindatas".
 * Present in the source but not referenced by any active route or controller (login/registration moved to Clerk).
 */
@Document(collection = "logindatas")
public class LoginData {
    @Id
    @JsonProperty("_id")
    public ObjectId id;

    public String uname;
    public String email;
    public String password;

    @Field("__v")
    @JsonProperty("__v")
    public int v = 0;
}
