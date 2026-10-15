package com.croptalk.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croptalk.exception.ApiException;
import com.croptalk.model.User;
import com.croptalk.repository.UserRepository;
import com.croptalk.util.JsonUtil;
import com.croptalk.util.Responses;

/** Port of controllers/connectioncontroller.js. */
@Service
public class ConnectionService {
    private static final Logger log = LoggerFactory.getLogger(ConnectionService.class);

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public ConnectionService(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /** POST /sendrequest/:clerkid  (body: { targetClerkId }) */
    public Map<String, Object> sendConnectionRequest(String clerkid, Map<String, Object> body) {
        Object targetClerkId = body.get("targetClerkId");

        log.info("Received clerkid: {}", clerkid);
        log.info("Received targetClerkId: {}", targetClerkId);

        if (clerkid.equals(targetClerkId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("You cannot send a connection request to yourself."));
        }
        if (!(targetClerkId instanceof String target)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("Invalid user ID(s)."));
        }

        try {
            User user = userRepository.findByClerkid(clerkid).orElse(null);
            User targetUser = userRepository.findByClerkid(target).orElse(null);

            if (user == null || targetUser == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, Responses.message("User or target user not found."));
            }

            // Has this sender already requested the target?
            boolean alreadySent = targetUser.connectionRequests.stream().anyMatch(r -> clerkid.equals(r.senderId));
            if (alreadySent) {
                throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("Connection request already sent."));
            }

            // Add the sender to the target user's connectionRequests (stored as a schema-conformant request entry)
            targetUser.connectionRequests.add(new User.ConnectionRequest(clerkid, user.username));
            userRepository.save(targetUser);

            return Responses.message("Connection request sent.");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error sending connection request:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    Responses.messageWithError("Error sending connection request.", e.getMessage()));
        }
    }

    /** POST /sendname/:receiverUsername  (body: { senderUsername }) */
    public Map<String, Object> sendConnectionRequestByUsername(String receiverUsername, Map<String, Object> body) {
        Object senderUsername = body.get("senderUsername");

        try {
            User sender = findByUsernameIgnoreCase(senderUsername);
            User receiver = findByUsernameIgnoreCase(receiverUsername);

            if (sender == null || receiver == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, Responses.message("User not found"));
            }

            boolean alreadySent = receiver.connectionRequests.stream().anyMatch(r -> Objects.equals(r.senderId, sender.clerkid));
            if (alreadySent) {
                throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("Request already sent"));
            }

            receiver.connectionRequests.add(new User.ConnectionRequest(sender.clerkid, sender.username));
            userRepository.save(receiver);

            return Responses.message("Connection request sent successfully!");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error sending connection request:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    Responses.messageWithError("Error sending connection request", e.getMessage()));
        }
    }

    /** POST /acceptrequest/:userclerkid  (body: { requesterClerkId }) */
    public Map<String, Object> acceptConnectionRequest(String userclerkid, Map<String, Object> body) {
        try {
            Object requester = body.get("requesterClerkId");
            String requesterClerkId = requester == null ? null : JsonUtil.asString(requester);

            log.info("Accepting connection request userclerkid={}, requesterClerkId={}", userclerkid, requesterClerkId);

            User receiver = userRepository.findByClerkid(userclerkid).orElse(null);
            User sender = requesterClerkId == null ? null : userRepository.findByClerkid(requesterClerkId).orElse(null);

            if (receiver == null || sender == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, Responses.error("User not found"));
            }

            boolean requestExists = receiver.connectionRequests.stream().anyMatch(r -> requesterClerkId.equals(r.senderId));
            if (!requestExists) {
                throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("No pending request found"));
            }

            // Add each user to the other's connections if not already connected
            if (receiver.connections.stream().noneMatch(c -> requesterClerkId.equals(c.userId))) {
                receiver.connections.add(new User.Connection(requesterClerkId, "accepted"));
            }
            if (sender.connections.stream().noneMatch(c -> userclerkid.equals(c.userId))) {
                sender.connections.add(new User.Connection(userclerkid, "accepted"));
            }

            // Remove the accepted request from the connectionRequests array
            receiver.connectionRequests.removeIf(r -> requesterClerkId.equals(r.senderId));

            userRepository.save(receiver);
            userRepository.save(sender);

            return Responses.message("Connection request accepted successfully");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error accepting connection request", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, Responses.error("Error accepting connection request"));
        }
    }

    /** POST /rejectrequest/:senderid  (body: { receiverClerkId }) */
    public Map<String, Object> rejectConnectionRequest(String senderid, Map<String, Object> body) {
        String trimmedSenderId = senderid.trim();
        Object receiverClerkIdRaw = body.get("receiverClerkId");

        log.info("Rejecting connection request trimmedSenderId={}, receiverClerkId={}", trimmedSenderId, receiverClerkIdRaw);

        try {
            String receiverClerkId = receiverClerkIdRaw == null ? null : JsonUtil.asString(receiverClerkIdRaw);
            User receiverUser = receiverClerkId == null ? null : userRepository.findByClerkid(receiverClerkId).orElse(null);

            if (receiverUser == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, Responses.message("Receiver user not found."));
            }

            boolean requestExists = receiverUser.connectionRequests.stream().anyMatch(r -> trimmedSenderId.equals(r.senderId));
            if (!requestExists) {
                throw new ApiException(HttpStatus.BAD_REQUEST, Responses.message("No pending request found."));
            }

            // $pull { connectionRequests: { senderId: trimmedSenderId } }
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("clerkid").is(receiverClerkId)),
                    new Update()
                            .pull("connectionRequests", new Document("senderId", trimmedSenderId))
                            .set("updatedAt", new Date()),
                    User.class);

            return Responses.message("Connection request rejected successfully.");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error rejecting request:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, Responses.error("Server error"));
        }
    }

    /** GET /fetchrequests/:clerkid */
    public List<Map<String, Object>> fetchConnectionRequests(String clerkid) {
        log.info("Received clerkid: {}", clerkid);

        try {
            User user = userRepository.findByClerkid(clerkid).orElse(null);
            if (user == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, Responses.message("User not found"));
            }

            List<String> senderIds = user.connectionRequests.stream().map(r -> r.senderId).collect(Collectors.toList());
            List<User> requestSenders = userRepository.findByClerkidIn(senderIds);

            List<Map<String, Object>> formatted = new ArrayList<>();
            for (User sender : requestSenders) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("senderId", sender.clerkid);
                item.put("senderName", sender.username);
                item.put("bio", sender.bio);
                item.put("timestamp", user.connectionRequests.stream()
                        .filter(r -> Objects.equals(r.senderId, sender.clerkid))
                        .findFirst().get().timestamp);
                formatted.add(item);
            }
            return formatted;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching connection requests:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    Responses.messageWithError("Internal server error", e.getMessage()));
        }
    }

    /** GET /fetchrequestsbyname/:username */
    public Map<String, Object> fetchAllRequestsByUsername(String username) {
        try {
            User user = findByUsernameIgnoreCase(username);
            if (user == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, Responses.message("User not found"));
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("connectionRequests", user.connectionRequests);
            return body;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching connection requests:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    Responses.messageWithError("Error fetching connection requests", e.getMessage()));
        }
    }

    /** GET /getAcceptedConnections/:userclerkid */
    public Map<String, Object> fetchAcceptedConnections(String userclerkid) {
        log.info("Received clerkid: {}", userclerkid);

        try {
            String trimmedClerkid = userclerkid.trim();

            User user = userRepository.findByClerkid(trimmedClerkid).orElse(null);
            if (user == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, Responses.message("User not found."));
            }

            List<Map<String, Object>> connectionsWithUsernames = new ArrayList<>();
            for (User.Connection conn : user.connections) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("userId", conn.userId);
                item.put("status", conn.status);
                item.put("connectedAt", conn.connectedAt);
                item.put("_id", conn.id);
                try {
                    Optional<User> connectedUser = userRepository.findByClerkid(conn.userId);
                    item.put("username", connectedUser.map(u -> u.username).orElse("Unknown User"));
                } catch (Exception err) {
                    log.error("Error fetching connected user:", err);
                    item.put("username", "Unknown User");
                }
                connectionsWithUsernames.add(item);
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("connections", connectionsWithUsernames);
            return body;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching connections:", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, Responses.message("Server Error"));
        }
    }

    /**
     * findOne({ username: new RegExp(`^${username}$`, 'i') }) - the pattern is intentionally not escaped,
     * matching the original behaviour.
     */
    private User findByUsernameIgnoreCase(Object username) {
        String pattern = "^" + (username == null ? "undefined" : username) + "$";
        return mongoTemplate.findOne(Query.query(Criteria.where("username").regex(pattern, "i")), User.class);
    }
}
