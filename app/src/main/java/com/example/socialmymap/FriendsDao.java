package com.example.socialmymap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class FriendsDao {

    private FriendsDBHelper dbHelper;
    private SQLiteDatabase database;

    public FriendsDao(Context context) {
        dbHelper = new FriendsDBHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // 친구 추가
    public boolean addFriend(String userId, String friendUserId, String friendNickname) {
        ContentValues values = new ContentValues();
        values.put(FriendsDBHelper.COLUMN_USER_ID, userId);
        values.put(FriendsDBHelper.COLUMN_FRIEND_USER_ID, friendUserId);
        values.put(FriendsDBHelper.COLUMN_FRIEND_NICKNAME, friendNickname);
        values.put(FriendsDBHelper.COLUMN_CREATED_AT, System.currentTimeMillis());

        long result = database.insert(FriendsDBHelper.TABLE_FRIENDS, null, values);
        return result != -1;
    }

    // 친구 목록 조회
    public List<Friend> getFriends(String userId) {
        List<Friend> friends = new ArrayList<>();
        Cursor cursor = database.query(
                FriendsDBHelper.TABLE_FRIENDS,
                null,
                FriendsDBHelper.COLUMN_USER_ID + " = ?",
                new String[] { userId },
                null, null,
                FriendsDBHelper.COLUMN_CREATED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Friend friend = new Friend(
                        cursor.getLong(cursor.getColumnIndexOrThrow(FriendsDBHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FriendsDBHelper.COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FriendsDBHelper.COLUMN_FRIEND_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FriendsDBHelper.COLUMN_FRIEND_NICKNAME)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(FriendsDBHelper.COLUMN_CREATED_AT)));
                friends.add(friend);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return friends;
    }

    // 친구 여부 확인
    public boolean isFriend(String userId, String friendUserId) {
        Cursor cursor = database.query(
                FriendsDBHelper.TABLE_FRIENDS,
                null,
                FriendsDBHelper.COLUMN_USER_ID + " = ? AND " + FriendsDBHelper.COLUMN_FRIEND_USER_ID + " = ?",
                new String[] { userId, friendUserId },
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    // 친구 삭제
    public boolean deleteFriend(long friendId) {
        int result = database.delete(
                FriendsDBHelper.TABLE_FRIENDS,
                FriendsDBHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(friendId) });
        return result > 0;
    }

    // 친구 삭제 (userId와 friendUserId로)
    public boolean deleteFriendByUserId(String userId, String friendUserId) {
        int result = database.delete(
                FriendsDBHelper.TABLE_FRIENDS,
                FriendsDBHelper.COLUMN_USER_ID + " = ? AND " + FriendsDBHelper.COLUMN_FRIEND_USER_ID + " = ?",
                new String[] { userId, friendUserId });
        return result > 0;
    }
}
