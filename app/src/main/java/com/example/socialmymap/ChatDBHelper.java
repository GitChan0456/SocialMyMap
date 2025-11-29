package com.example.socialmymap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 1;

    // 채팅방 테이블
    public static final String TABLE_CHAT_ROOMS = "chat_rooms";
    public static final String COLUMN_ROOM_ID = "id";
    public static final String COLUMN_FRIEND_USER_ID = "friend_user_id";
    public static final String COLUMN_FRIEND_NICKNAME = "friend_nickname";
    public static final String COLUMN_LAST_MESSAGE = "last_message";
    public static final String COLUMN_LAST_MESSAGE_TIME = "last_message_time";
    public static final String COLUMN_UNREAD_COUNT = "unread_count";

    // 메시지 테이블
    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_MESSAGE_ID = "id";
    public static final String COLUMN_CHAT_ROOM_ID = "chat_room_id";
    public static final String COLUMN_SENDER_ID = "sender_id";
    public static final String COLUMN_SENDER_NICKNAME = "sender_nickname";
    public static final String COLUMN_MESSAGE_TEXT = "message_text";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_CHAT_ROOMS_TABLE = "CREATE TABLE " + TABLE_CHAT_ROOMS + " (" +
            COLUMN_ROOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_FRIEND_USER_ID + " TEXT NOT NULL, " +
            COLUMN_FRIEND_NICKNAME + " TEXT NOT NULL, " +
            COLUMN_LAST_MESSAGE + " TEXT, " +
            COLUMN_LAST_MESSAGE_TIME + " INTEGER DEFAULT 0, " +
            COLUMN_UNREAD_COUNT + " INTEGER DEFAULT 0);";

    private static final String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + " (" +
            COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CHAT_ROOM_ID + " INTEGER NOT NULL, " +
            COLUMN_SENDER_ID + " TEXT NOT NULL, " +
            COLUMN_SENDER_NICKNAME + " TEXT NOT NULL, " +
            COLUMN_MESSAGE_TEXT + " TEXT NOT NULL, " +
            COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COLUMN_CHAT_ROOM_ID + ") REFERENCES " +
            TABLE_CHAT_ROOMS + "(" + COLUMN_ROOM_ID + ") ON DELETE CASCADE);";

    public ChatDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CHAT_ROOMS_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_ROOMS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
