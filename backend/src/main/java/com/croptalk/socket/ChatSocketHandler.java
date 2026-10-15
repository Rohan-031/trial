package com.croptalk.socket;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.croptalk.service.ChatService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Port of the active Socket.IO block in server.js: private rooms per user id, in-memory online-user tracking,
 * offline message queue, and persistence of every chat message.
 */
@Component
public class ChatSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatSocketHandler.class);

    // Date#toLocaleTimeString() in an en-US locale, e.g. "3:45:12 PM"
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.US);

    private final SocketIOServer server;
    private final ChatService chatService;

    /** onlineUsers: userId -> socket session id */
    private final Map<String, UUID> onlineUsers = new ConcurrentHashMap<>();
    /** offlineMessages: receiverId -> queued messages */
    private final Map<String, List<Map<String, Object>>> offlineMessages = new ConcurrentHashMap<>();

    public ChatSocketHandler(SocketIOServer server, ChatService chatService) {
        this.server = server;
        this.chatService = chatService;
    }

    @PostConstruct
    public void start() {
        server.addConnectListener(client -> log.info("User connected: {}", client.getSessionId()));

        // Join a private room based on user ID (receiver and sender)
        server.addEventListener("join", String.class, (client, userId, ack) -> onJoin(client, userId));

        // Handle message sending
        server.addEventListener("send_message", ChatMessagePayload.class, (client, data, ack) -> onSendMessage(data));

        // Handle user disconnection
        server.addDisconnectListener(this::onDisconnect);

        server.start();
        log.info("Socket.IO server running on port {}", server.getConfiguration().getPort());
    }

    @PreDestroy
    public void stop() {
        server.stop();
    }

    private void onJoin(SocketIOClient client, String userId) {
        client.joinRoom(userId);
        onlineUsers.put(userId, client.getSessionId());     // Mark user as online
        log.info("{} joined their private room with socketId: {}", userId, client.getSessionId());

        // Deliver (and clear) any messages stored while the user was offline
        List<Map<String, Object>> pending = offlineMessages.remove(userId);
        if (pending != null) {
            log.info("Sending offline messages to {}", userId);
            pending.forEach(message -> client.sendEvent("receive_message", message));
        }

        client.sendEvent("joined", "You have joined the room: " + userId);
    }

    private void onSendMessage(ChatMessagePayload data) {
        if (data == null) return;
        String senderId = data.senderId;
        String receiverId = data.receiverId;
        String message = data.message;
        log.info("Message from {} to {}: {}", senderId, receiverId, message);

        // Save the chat to the database
        try {
            chatService.saveChat(senderId, receiverId, message);
            log.info("Chat saved between {} and {}", senderId, receiverId);
        } catch (Exception error) {
            log.error("Error saving chat:", error);
        }

        // Now send to the receiver
        String time = LocalTime.now().format(TIME_FORMAT);
        String receiverKey = String.valueOf(receiverId);
        if (onlineUsers.containsKey(receiverKey)) {
            server.getRoomOperations(receiverKey).sendEvent("receive_message", receivedPayload(senderId, message, time));
            log.info("Message sent to online user {}", receiverId);
        } else {
            offlineMessages.computeIfAbsent(receiverKey, k -> new CopyOnWriteArrayList<>())
                    .add(receivedPayload(senderId, message, time));
            log.info("Stored message for offline user {}", receiverId);
        }

        // Confirm back to sender
        Map<String, Object> confirmation = new LinkedHashMap<>();
        confirmation.put("message", message);
        confirmation.put("time", time);
        server.getRoomOperations(String.valueOf(senderId)).sendEvent("message_sent", confirmation);
    }

    private void onDisconnect(SocketIOClient client) {
        log.info("User disconnected: {}", client.getSessionId());
        for (Map.Entry<String, UUID> entry : new ArrayList<>(onlineUsers.entrySet())) {
            if (entry.getValue().equals(client.getSessionId())) {
                onlineUsers.remove(entry.getKey(), entry.getValue());
                log.info("User {} removed from online list", entry.getKey());
                break;
            }
        }
    }

    private static Map<String, Object> receivedPayload(String senderId, String message, String time) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("senderId", senderId);
        payload.put("message", message);
        payload.put("time", time);
        payload.put("isOwn", false);
        return payload;
    }
}
