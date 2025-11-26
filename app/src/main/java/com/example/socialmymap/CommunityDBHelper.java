package com.example.socialmymap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CommunityDBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "community.db";
    private static final int DB_VERSION = 2;

    public static final String TABLE_POSTS = "posts";
    public static final String TABLE_COMMENTS = "comments";

    public CommunityDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_POSTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "author_id TEXT NOT NULL," +
                        "author_nickname TEXT NOT NULL," +
                        "title TEXT NOT NULL," +
                        "content TEXT NOT NULL," +
                        "timestamp TEXT NOT NULL," +
                        "views INTEGER DEFAULT 0," +
                        "comment_count INTEGER DEFAULT 0" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_COMMENTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "post_id INTEGER NOT NULL," +
                        "author_id TEXT NOT NULL," +
                        "author_nickname TEXT NOT NULL," +
                        "content TEXT NOT NULL," +
                        "timestamp TEXT NOT NULL," +
                        "FOREIGN KEY(post_id) REFERENCES " + TABLE_POSTS + "(id) ON DELETE CASCADE" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
            onCreate(db);
        }
    }
}
