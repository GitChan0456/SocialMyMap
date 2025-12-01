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
        values.put("author_id", post.authorId);
        values.put("author_nickname", post.author);
        values.put("title", post.title);
        values.put("content", post.content);
        values.put("timestamp", post.timestamp);
        values.put("views", post.views);
        values.put("comment_count", post.commentCount);
        return db.insert(CommunityDBHelper.TABLE_POSTS, null, values);
    }

    private Post cursorToPost(Cursor cursor) {
        return new Post(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("author_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getString(cursor.getColumnIndexOrThrow("content")),
                cursor.getString(cursor.getColumnIndexOrThrow("author_nickname")),
                cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                cursor.getInt(cursor.getColumnIndexOrThrow("views")),
                cursor.getInt(cursor.getColumnIndexOrThrow("comment_count")));
    }

    public List<Post> getAllPosts() {
        List<Post> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CommunityDBHelper.TABLE_POSTS,
                null, null, null, null, null, "id DESC");
        try {
            while (cursor.moveToNext()) {
                result.add(cursorToPost(cursor));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public Post getPostById(long postId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CommunityDBHelper.TABLE_POSTS,
                null, "id = ?", new String[] { String.valueOf(postId) },
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursorToPost(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public void incrementViews(long postId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + CommunityDBHelper.TABLE_POSTS + " SET views = views + 1 WHERE id = ?",
                new Object[] { postId });
    }

    public long insertComment(Comment comment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("post_id", comment.postId);
        values.put("author_id", comment.authorId);
        values.put("author_nickname", comment.author);
        values.put("content", comment.content);
        values.put("timestamp", comment.timestamp);
        long id = db.insert(CommunityDBHelper.TABLE_COMMENTS, null, values);
        db.execSQL("UPDATE " + CommunityDBHelper.TABLE_POSTS + " SET comment_count = comment_count + 1 WHERE id = ?",
                new Object[] { comment.postId });
        return id;
    }

    public List<Comment> getComments(long postId) {
        List<Comment> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CommunityDBHelper.TABLE_COMMENTS,
                null,
                "post_id = ?",
                new String[] { String.valueOf(postId) },
                null, null,
                "id ASC");
        try {
            while (cursor.moveToNext()) {
                Comment comment = new Comment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("post_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("author_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("author_nickname")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp")));
                result.add(comment);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public int countPostsByAuthor(String authorId, String authorNickname) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + CommunityDBHelper.TABLE_POSTS + " WHERE author_id = ? OR author_nickname = ?",
                new String[] { authorId, authorNickname });
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    public List<Post> getPostsByAuthor(String authorId, String authorNickname) {
        List<Post> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CommunityDBHelper.TABLE_POSTS,
                null,
                "author_id = ? OR author_nickname = ?",
                new String[] { authorId, authorNickname },
                null,
                null,
                "id DESC");
        try {
            while (cursor.moveToNext()) {
                result.add(cursorToPost(cursor));
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
        return db.update(CommunityDBHelper.TABLE_POSTS, values, "id = ?", new String[] { String.valueOf(id) });
    }

    public int deletePost(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(CommunityDBHelper.TABLE_POSTS, "id = ?", new String[] { String.valueOf(id) });
    }

    public int countCommentsByAuthor(String authorId, String authorNickname) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + CommunityDBHelper.TABLE_COMMENTS
                + " WHERE author_id = ? OR author_nickname = ?", new String[] { authorId, authorNickname });
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    public List<MyCommentItem> getCommentsByAuthor(String authorId, String authorNickname) {
        List<MyCommentItem> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT c.id, c.post_id, c.content, c.timestamp, p.title " +
                        "FROM " + CommunityDBHelper.TABLE_COMMENTS + " c " +
                        "JOIN " + CommunityDBHelper.TABLE_POSTS + " p ON c.post_id = p.id " +
                        "WHERE c.author_id = ? OR c.author_nickname = ? " +
                        "ORDER BY c.id DESC",
                new String[] { authorId, authorNickname });
        try {
            while (cursor.moveToNext()) {
                MyCommentItem item = new MyCommentItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("post_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp")));
                result.add(item);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    /**
     * 회원 탈퇴 시 해당 사용자의 모든 게시글 삭제
     */
    public void deletePostsByUser(String authorId, String authorNickname) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CommunityDBHelper.TABLE_POSTS,
                "author_id = ? OR author_nickname = ?",
                new String[] { authorId, authorNickname });
    }

    /**
     * 회원 탈퇴 시 해당 사용자의 모든 댓글을 익명화
     */
    public void anonymizeCommentsByUser(String authorId, String authorNickname) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("author_id", "deleted_user");
        values.put("author_nickname", "(알 수 없음)");

        db.update(CommunityDBHelper.TABLE_COMMENTS,
                values,
                "author_id = ? OR author_nickname = ?",
                new String[] { authorId, authorNickname });
    }
}
