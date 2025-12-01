package com.example.socialmymap;

import java.io.Serializable;

public class Post implements Serializable {
    public long id;
    public String authorId;
    public String title;
    public String content;
    public String author;
    public String timestamp;
    public int views;
    public int commentCount;

    // 위치 정보 필드
    public String region = "미분류";
    public double latitude = 0;
    public double longitude = 0;

    public Post() {
    }

    public Post(long id, String authorId, String title, String content, String author, String timestamp, int views,
            int commentCount) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
        this.views = views;
        this.commentCount = commentCount;
    }

    public Post(String authorId, String title, String content, String author, String timestamp) {
        this(0, authorId, title, content, author, timestamp, 0, 0);
    }
}
