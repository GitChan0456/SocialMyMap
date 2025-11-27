package com.example.socialmymap;

public class Friend {
    public long id;
    public String userId; // 내 사용자 ID
    public String friendUserId; // 친구 사용자 ID
    public String friendNickname; // 친구 닉네임
    public long createdAt; // 친구 추가 시간 (timestamp)

    public Friend() {
    }

    public Friend(String userId, String friendUserId, String friendNickname, long createdAt) {
        this.userId = userId;
        this.friendUserId = friendUserId;
        this.friendNickname = friendNickname;
        this.createdAt = createdAt;
    }

    public Friend(long id, String userId, String friendUserId, String friendNickname, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.friendUserId = friendUserId;
        this.friendNickname = friendNickname;
        this.createdAt = createdAt;
    }
}
