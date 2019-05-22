package com.example.photoblog.JavaClass;

import java.util.Date;

public class Comments extends CommentPostId{

    private String message, currentUserId;
    private Date post_time;

    public Comments() {
    }

    public Comments(String message, String currentUserId) {
        this.message = message;
        this.currentUserId = currentUserId;
    }

    public Comments(String message, String currentUserId, Date post_time) {
        this.message = message;
        this.currentUserId = currentUserId;
        this.post_time = post_time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public Date getPost_time() {
        return post_time;
    }

    public void setPost_time(Date post_time) {
        this.post_time = post_time;
    }

}
