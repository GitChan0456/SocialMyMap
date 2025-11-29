package com.example.socialmymap;

public class Message {
    public long id;
    public long chatRoomId;
    public String senderId;
    public String senderNickname;
    public String messageText;
    public long timestamp;
    public boolean isMine;

    public Message() {
    }

    public Message(long id, long chatRoomId, String senderId, String senderNickname, String messageText, long timestamp,
            boolean isMine) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.isMine = isMine;
    }
}
