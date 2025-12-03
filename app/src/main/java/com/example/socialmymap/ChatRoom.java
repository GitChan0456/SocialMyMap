package com.example.socialmymap;

import java.util.HashMap;
import java.util.Map;

public class ChatRoom {
    // SQLite 필드
    public long id;
    public String friendUserId;
    public String friendNickname;
    public String lastMessage;
    public long lastMessageTime;
    public int unreadCount;

    // Firebase 필드
    public String roomId;
    public Map<String, Boolean> users;

    public ChatRoom() {
    }

    // SQLite용 생성자
    public ChatRoom(long id, String friendUserId, String friendNickname, String lastMessage, long lastMessageTime,
            int unreadCount) {
        this.id = id;
        this.friendUserId = friendUserId;
        this.friendNickname = friendNickname;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    // Firebase용 생성자
    public ChatRoom(String roomId, Map<String, Boolean> users) {
        this.roomId = roomId;
        this.users = users;
        this.lastMessage = "";
        this.lastMessageTime = System.currentTimeMillis();
    }
}
