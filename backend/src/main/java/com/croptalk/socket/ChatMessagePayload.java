package com.croptalk.socket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Payload of the "send_message" event: { senderId, receiverId, message }. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessagePayload {
    public String senderId;
    public String receiverId;
    public String message;
}
