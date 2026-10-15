package com.croptalk.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.croptalk.exception.ApiException;
import com.croptalk.model.Chat;
import com.croptalk.service.ChatService;
import com.croptalk.util.JsonUtil;
import com.croptalk.util.Responses;

/**
 * Port of routes/chat.js + controllers/chatcontroller.js.
 *
 * In the source these routes are never mounted in server.js (and the controller imports a non-existent
 * "../models/chatModel.js"), so they were never reachable. They are therefore disabled by default to keep the
 * runtime API identical; set croptalk.chat-rest.enabled=true to expose them under /api/chat.
 */
@RestController
@RequestMapping("/api/chat")
@ConditionalOnProperty(name = "croptalk.chat-rest.enabled", havingValue = "true")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> saveChat(@RequestBody(required = false) Map<String, Object> rawBody) {
        Map<String, Object> body = JsonUtil.orEmpty(rawBody);
        Object senderId = body.get("senderId");
        Object receiverId = body.get("receiverId");
        Object message = body.get("message");

        if (JsonUtil.isFalsy(senderId) || JsonUtil.isFalsy(receiverId) || JsonUtil.isFalsy(message)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Responses.error("All fields are required."));
        }

        try {
            Chat chat = chatService.saveChat(JsonUtil.asString(senderId), JsonUtil.asString(receiverId), JsonUtil.asString(message));
            Map<String, Object> response = Responses.message("Chat saved successfully.");
            response.put("chat", chat);
            return response;
        } catch (Exception error) {
            log.error("Error saving chat:", error);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, Responses.error("Server error while saving chat."));
        }
    }

    @GetMapping("/getChats/{senderId}/{receiverId}")
    public Map<String, Object> getChatsBetweenUsers(@PathVariable String senderId, @PathVariable String receiverId) {
        try {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("chats", chatService.getChatsBetweenUsers(senderId, receiverId));
            return response;
        } catch (Exception error) {
            log.error("Error fetching chats:", error);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, Responses.error("Server error while fetching chats."));
        }
    }
}
