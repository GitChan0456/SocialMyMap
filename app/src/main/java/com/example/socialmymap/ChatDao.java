package com.example.socialmymap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatDao {

    private final ChatDBHelper dbHelper;
    private SQLiteDatabase database;

    public ChatDao(Context context) {
        dbHelper = new ChatDBHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // 채팅방 가져오기 또는 생성
    public ChatRoom getOrCreateChatRoom(String currentUserId, String friendUserId, String friendNickname) {
        // 기존 채팅방 찾기
        Cursor cursor = database.query(
                ChatDBHelper.TABLE_CHAT_ROOMS,
                null,
                ChatDBHelper.COLUMN_FRIEND_USER_ID + " = ?",
                new String[] { friendUserId },
                null, null, null);

        if (cursor.moveToFirst()) {
            ChatRoom chatRoom = cursorToChatRoom(cursor);
            cursor.close();
            return chatRoom;
        }
        cursor.close();

        // 새 채팅방 생성
        ContentValues values = new ContentValues();
        values.put(ChatDBHelper.COLUMN_FRIEND_USER_ID, friendUserId);
        values.put(ChatDBHelper.COLUMN_FRIEND_NICKNAME, friendNickname);
        values.put(ChatDBHelper.COLUMN_LAST_MESSAGE, "");
        values.put(ChatDBHelper.COLUMN_LAST_MESSAGE_TIME, System.currentTimeMillis());
        values.put(ChatDBHelper.COLUMN_UNREAD_COUNT, 0);

        long id = database.insert(ChatDBHelper.TABLE_CHAT_ROOMS, null, values);
        return new ChatRoom(id, friendUserId, friendNickname, "", System.currentTimeMillis(), 0);
    }

    // 모든 채팅방 목록 가져오기
    public List<ChatRoom> getAllChatRooms(String currentUserId) {
        List<ChatRoom> chatRooms = new ArrayList<>();
        Cursor cursor = database.query(
                ChatDBHelper.TABLE_CHAT_ROOMS,
                null, null, null, null, null,
                ChatDBHelper.COLUMN_LAST_MESSAGE_TIME + " DESC");

        while (cursor.moveToNext()) {
            chatRooms.add(cursorToChatRoom(cursor));
        }
        cursor.close();
        return chatRooms;
    }

    // 마지막 메시지 업데이트
    public void updateChatRoomLastMessage(long chatRoomId, String lastMessage, long timestamp) {
        ContentValues values = new ContentValues();
        values.put(ChatDBHelper.COLUMN_LAST_MESSAGE, lastMessage);
        values.put(ChatDBHelper.COLUMN_LAST_MESSAGE_TIME, timestamp);

        database.update(
                ChatDBHelper.TABLE_CHAT_ROOMS,
                values,
                ChatDBHelper.COLUMN_ROOM_ID + " = ?",
                new String[] { String.valueOf(chatRoomId) });
    }

    // 메시지 저장
    public long insertMessage(Message message) {
        ContentValues values = new ContentValues();
        values.put(ChatDBHelper.COLUMN_CHAT_ROOM_ID, message.chatRoomId);
        values.put(ChatDBHelper.COLUMN_SENDER_ID, message.senderId);
        values.put(ChatDBHelper.COLUMN_SENDER_NICKNAME, message.senderNickname);
        values.put(ChatDBHelper.COLUMN_MESSAGE_TEXT, message.messageText);
        values.put(ChatDBHelper.COLUMN_TIMESTAMP, message.timestamp);

        return database.insert(ChatDBHelper.TABLE_MESSAGES, null, values);
    }

    // 특정 채팅방의 모든 메시지 가져오기
    public List<Message> getMessages(long chatRoomId, String currentUserId) {
        List<Message> messages = new ArrayList<>();
        Cursor cursor = database.query(
                ChatDBHelper.TABLE_MESSAGES,
                null,
                ChatDBHelper.COLUMN_CHAT_ROOM_ID + " = ?",
                new String[] { String.valueOf(chatRoomId) },
                null, null,
                ChatDBHelper.COLUMN_TIMESTAMP + " ASC");

        while (cursor.moveToNext()) {
            Message message = cursorToMessage(cursor);
            message.isMine = message.senderId.equals(currentUserId);
            messages.add(message);
        }
        cursor.close();
        return messages;
    }

    // 채팅방 삭제
    public void deleteChatRoom(long chatRoomId) {
        // CASCADE로 메시지도 자동 삭제됨
        database.delete(
                ChatDBHelper.TABLE_CHAT_ROOMS,
                ChatDBHelper.COLUMN_ROOM_ID + " = ?",
                new String[] { String.valueOf(chatRoomId) });
    }

    private ChatRoom cursorToChatRoom(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_ROOM_ID);
        int friendUserIdIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_FRIEND_USER_ID);
        int friendNicknameIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_FRIEND_NICKNAME);
        int lastMessageIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_LAST_MESSAGE);
        int lastMessageTimeIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_LAST_MESSAGE_TIME);
        int unreadCountIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_UNREAD_COUNT);

        return new ChatRoom(
                cursor.getLong(idIndex),
                cursor.getString(friendUserIdIndex),
                cursor.getString(friendNicknameIndex),
                cursor.getString(lastMessageIndex),
                cursor.getLong(lastMessageTimeIndex),
                cursor.getInt(unreadCountIndex));
    }

    private Message cursorToMessage(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_MESSAGE_ID);
        int chatRoomIdIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_CHAT_ROOM_ID);
        int senderIdIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_SENDER_ID);
        int senderNicknameIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_SENDER_NICKNAME);
        int messageTextIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_MESSAGE_TEXT);
        int timestampIndex = cursor.getColumnIndex(ChatDBHelper.COLUMN_TIMESTAMP);

        return new Message(
                cursor.getLong(idIndex),
                cursor.getLong(chatRoomIdIndex),
                cursor.getString(senderIdIndex),
                cursor.getString(senderNicknameIndex),
                cursor.getString(messageTextIndex),
                cursor.getLong(timestampIndex),
                false // isMine은 나중에 설정
        );
    }
}
