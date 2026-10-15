package com.croptalk.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.croptalk.model.Chat;
import com.croptalk.repository.ChatRepository;
import com.croptalk.util.JsonUtil;

/** Chat persistence: models/chatdata.js + controllers/chatcontroller.js. */
@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    /** new Chat({senderId, receiverId, message}).save() - all three fields are required by the schema. */
    public Chat saveChat(String senderId, String receiverId, String message) {
        if (JsonUtil.isFalsy(senderId) || JsonUtil.isFalsy(receiverId) || JsonUtil.isFalsy(message)) {
            throw new IllegalArgumentException("Chat validation failed: senderId, receiverId and message are required");
        }
        Chat chat = new Chat();
        chat.senderId = senderId;
        chat.receiverId = receiverId;
        chat.message = message;
        chat.timestamp = new Date();
        return chatRepository.save(chat);
    }

    /** getChatsBetweenUsers - original sorted by `createdAt`, a field the schema does not have. */
    public List<Chat> getChatsBetweenUsers(String senderId, String receiverId) {
        return chatRepository.findBetweenUsers(senderId, receiverId, Sort.by(Sort.Direction.ASC, "createdAt"));
    }
}
