package com.example.photoblog.JavaClass;

import com.google.firebase.firestore.Exclude;

import androidx.annotation.NonNull;

public class CommentPostId {

    @Exclude
    public String CommentPostId;

    public <T extends CommentPostId> T withCommentId(@NonNull final String id) {
        this.CommentPostId = id;
        return (T) this;
    }

}
