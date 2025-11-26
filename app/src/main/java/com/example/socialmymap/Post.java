package com.example.socialmymap;

import java.io.Serializable;

public class Post implements Serializable {
    public long id;
    public String title;
    public String content;
    public String author;
    public String timestamp;
    public int views;
    public int commentCount;

    public Post() {
    }

    public Post(long id, String title, String content, String author, String timestamp, int views, int commentCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
        this.views = views;
        this.commentCount = commentCount;
    }

    public Post(String title, String content, String author, String timestamp) {
        this(0, title, content, author, timestamp, 0, 0);
    }
}
