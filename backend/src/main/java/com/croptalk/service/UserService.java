package com.croptalk.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croptalk.exception.ApiException;
import com.croptalk.model.User;
import com.croptalk.repository.UserRepository;
import com.croptalk.util.JsonUtil;
import com.croptalk.util.Responses;
import com.fasterxml.jackson.databind.JsonNode;

/** Port of controllers/usercontroller.js. */
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ClerkClient clerkClient;

    public UserService(UserRepository userRepository, ClerkClient clerkClient) {
        this.userRepository = userRepository;
        this.clerkClient = clerkClient;
    }

    /** getAllUsers */
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            log.error("Error fetching users:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, Responses.messageWithEmptyError("Error fetching users"));
        }
    }

    /** finduser - fetch a single user by (exact) username */
    public User findUser(String username) {
        Optional<User> user;
        try {
            user = userRepository.findByUsername(username);
        } catch (Exception e) {
            log.error("Error fetching user:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, Responses.messageWithEmptyError("Error fetching user"));
        }
        return user.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, Responses.message("User not found")));
    }

    /** userinfo - fetch a user by Clerk ID */
    public User userInfo(String clerkid) {
        Optional<User> user;
        try {
            user = userRepository.findByClerkid(clerkid);
        } catch (Exception e) {
            log.error("Error fetching user:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, Responses.messageWithEmptyError("Error fetching user"));
        }
        return user.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, Responses.message("User not found")));
    }

    /** userlogin - look the username up in the Clerk user list */
    public Map<String, Object> userLogin(String uname) {
        try {
            JsonNode userList = clerkClient.getUserList();
            JsonNode found = null;
            if (userList != null && userList.isArray()) {
                for (JsonNode u : userList) {
                    if (u.hasNonNull("username") && u.get("username").asText().equals(uname)) {
                        found = u;
                        break;
                    }
                }
            }
            if (found == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, Responses.message("User not found...!"));
            }

            // user.emailAddresses[0]?.emailAddress || "No email found"
            String email = null;
            JsonNode emails = found.get("email_addresses");
            if (emails != null && emails.isArray() && emails.size() > 0 && emails.get(0).hasNonNull("email_address")) {
                email = emails.get(0).get("email_address").asText();
            }
            if (email == null || email.isEmpty()) email = "No email found";

            // SDK User.profileImageUrl (Clerk may only return image_url on newer API versions)
            String imageUrl = null;
            if (found.hasNonNull("profile_image_url")) imageUrl = found.get("profile_image_url").asText();
            else if (found.hasNonNull("image_url")) imageUrl = found.get("image_url").asText();

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("message", "User found! You are logged in!");
            body.put("username", found.get("username").asText());
            body.put("email", email);
            if (imageUrl != null) body.put("imageUrl", imageUrl);
            return body;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error logging in:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, Responses.messageWithEmptyError("Error logging in"));
        }
    }

    /** userregister - registration is handled by Clerk */
    public void userRegister() {
        throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("User registration is managed by Clerk"));
    }

    /** insertUser */
    public ResponseHolder insertUser(Map<String, Object> body) {
        Object clerkid = body.get("clerkid");
        Object email = body.get("email");
        Object username = body.get("username");

        if (JsonUtil.isFalsy(clerkid) || JsonUtil.isFalsy(email) || JsonUtil.isFalsy(username)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("Missing user information"));
        }

        String clerkidStr = JsonUtil.asString(clerkid);

        // Check if user with the same clerkid already exists
        if (userRepository.findByClerkid(clerkidStr).isPresent()) {
            return new ResponseHolder(HttpStatus.OK, Responses.message("User already exists"));
        }

        User newUser = new User();
        newUser.clerkid = clerkidStr;
        newUser.email = JsonUtil.asString(email);
        newUser.username = JsonUtil.asString(username);
        if (body.containsKey("bio")) {
            newUser.bio = JsonUtil.asString(body.get("bio"));   // explicit null is stored as null; absent -> default ""
        }

        try {
            userRepository.save(newUser);
            Map<String, Object> response = Responses.message("User inserted successfully");
            response.put("user", newUser);
            return new ResponseHolder(HttpStatus.CREATED, response);
        } catch (Exception e) {
            log.error("Error inserting user:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    Responses.messageWithError("Error inserting user", rootCauseMessage(e)));
        }
    }

    /** Small status + body holder for endpoints that return different success codes. */
    public record ResponseHolder(HttpStatus status, Object body) {}

    /** The MongoDB driver message (e.g. E11000 duplicate key ...), as Mongoose's error.message exposed it. */
    private static String rootCauseMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur.getMessage();
    }
}
