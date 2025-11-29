package com.example.socialmymap;

public class ChatRoom {
    public long id;
    public String friendUserId;
    public String friendNickname;
    public String lastMessage;
    public long lastMessageTime;
    public int unreadCount;

    public ChatRoom() {
    }

    public ChatRoom(long id, String friendUserId, String friendNickname, String lastMessage, long lastMessageTime,
            int unreadCount) {
        this.id = id;
        this.friendUserId = friendUserId;
        this.friendNickname = friendNickname;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }
}
