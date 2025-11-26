package com.example.socialmymap;

import java.io.Serializable;

public class Comment implements Serializable {
    public long id;
    public long postId;
    public String author;
    public String content;
    public String timestamp;

    public Comment() {
    }

    public Comment(long id, long postId, String author, String content, String timestamp) {
        this.id = id;
        this.postId = postId;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Comment(long postId, String author, String content, String timestamp) {
        this(0, postId, author, content, timestamp);
    }
}
