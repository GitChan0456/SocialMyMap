package com.example.socialmymap;

public class MyCommentItem {
    public long commentId;
    public long postId;
    public String postTitle;
    public String content;
    public String timestamp;

    public MyCommentItem(long commentId, long postId, String postTitle, String content, String timestamp) {
        this.commentId = commentId;
        this.postId = postId;
        this.postTitle = postTitle;
        this.content = content;
        this.timestamp = timestamp;
    }
}
