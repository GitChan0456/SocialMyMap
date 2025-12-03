package com.example.socialmymap;

public class ChatMessage {
    public String messageId;
    public String senderId;
    public String senderName;
    public String message;
    public long timestamp;

    public ChatMessage() {
        // Default constructor required for calls to
        // DataSnapshot.getValue(ChatMessage.class)
    }

    public ChatMessage(String senderId, String senderName, String message) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
