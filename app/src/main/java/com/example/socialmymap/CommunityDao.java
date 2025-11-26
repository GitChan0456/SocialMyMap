package com.example.socialmymap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class CommunityDao {
    private final CommunityDBHelper dbHelper;

    public CommunityDao(Context context) {
        dbHelper = new CommunityDBHelper(context);
    }

    public long insertPost(Post post) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", post.title);
        values.put("content", post.content);
        values.put("author", post.author);
        values.put("timestamp", post.timestamp);
        values.put("views", post.views);
        values.put("comment_count", post.commentCount);
        return db.insert(CommunityDBHelper.TABLE_POSTS, null, values);
    }

    public List<Post> getAllPosts() {
        List<Post> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CommunityDBHelper.TABLE_POSTS,
                null, null, null, null, null, "id DESC");
        try {
            while (cursor.moveToNext()) {
                Post post = new Post(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getString(cursor.getColumnIndexOrThrow("author")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("views")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("comment_count"))
                );
                result.add(post);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public Post getPostById(long postId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CommunityDBHelper.TABLE_POSTS,
                null, "id = ?", new String[]{String.valueOf(postId)},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return new Post(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getString(cursor.getColumnIndexOrThrow("author")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("views")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("comment_count"))
                );
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public void incrementViews(long postId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + CommunityDBHelper.TABLE_POSTS + " SET views = views + 1 WHERE id = ?", new Object[]{postId});
    }

    public long insertComment(Comment comment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("post_id", comment.postId);
        values.put("author", comment.author);
        values.put("content", comment.content);
        values.put("timestamp", comment.timestamp);
        long id = db.insert(CommunityDBHelper.TABLE_COMMENTS, null, values);
        db.execSQL("UPDATE " + CommunityDBHelper.TABLE_POSTS + " SET comment_count = comment_count + 1 WHERE id = ?", new Object[]{comment.postId});
        return id;
    }

    public List<Comment> getComments(long postId) {
        List<Comment> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CommunityDBHelper.TABLE_COMMENTS,
                null,
                "post_id = ?",
                new String[]{String.valueOf(postId)},
                null, null,
                "id ASC");
        try {
            while (cursor.moveToNext()) {
                Comment comment = new Comment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("post_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("author")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
                );
                result.add(comment);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public int countPostsByAuthor(String author) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + CommunityDBHelper.TABLE_POSTS + " WHERE author = ?", new String[]{author});
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    public List<Post> getPostsByAuthor(String author) {
        List<Post> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CommunityDBHelper.TABLE_POSTS,
                null,
                "author = ?",
                new String[]{author},
                null,
                null,
                "id DESC");
        try {
            while (cursor.moveToNext()) {
                Post post = new Post(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getString(cursor.getColumnIndexOrThrow("author")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("views")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("comment_count"))
                );
                result.add(post);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public int updatePost(long id, String title, String content) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        return db.update(CommunityDBHelper.TABLE_POSTS, values, "id = ?", new String[]{String.valueOf(id)});
    }

    public int deletePost(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(CommunityDBHelper.TABLE_POSTS, "id = ?", new String[]{String.valueOf(id)});
    }
}
