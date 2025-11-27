package com.example.socialmymap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FriendsDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "friends.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_FRIENDS = "friends_table";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_FRIEND_USER_ID = "friend_user_id";
    public static final String COLUMN_FRIEND_NICKNAME = "friend_nickname";
    public static final String COLUMN_CREATED_AT = "created_at";

    public FriendsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FRIENDS_TABLE = "CREATE TABLE " + TABLE_FRIENDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " TEXT NOT NULL, "
                + COLUMN_FRIEND_USER_ID + " TEXT NOT NULL, "
                + COLUMN_FRIEND_NICKNAME + " TEXT NOT NULL, "
                + COLUMN_CREATED_AT + " INTEGER NOT NULL, "
                + "UNIQUE(" + COLUMN_USER_ID + ", " + COLUMN_FRIEND_USER_ID + ") ON CONFLICT IGNORE"
                + ")";
        db.execSQL(CREATE_FRIENDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        onCreate(db);
    }
}
