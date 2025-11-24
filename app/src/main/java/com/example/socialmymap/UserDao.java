package com.example.socialmymap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDao {

    private SQLiteDatabase db;
    private UserDBHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new UserDBHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean insertUser(String userId, String password, String nickname, String email) {
        ContentValues values = new ContentValues();
        values.put(UserDBHelper.COLUMN_USER_ID, userId);
        values.put(UserDBHelper.COLUMN_PASSWORD, password);
        values.put(UserDBHelper.COLUMN_NICKNAME, nickname);
        values.put(UserDBHelper.COLUMN_EMAIL, email);
        long result = db.insertWithOnConflict(UserDBHelper.TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public boolean loginUser(String userId, String password) {
        String[] columns = {UserDBHelper.COLUMN_ID};
        String selection = UserDBHelper.COLUMN_USER_ID + " = ?" + " AND " + UserDBHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {userId, password};

        Cursor cursor = db.query(UserDBHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public UserData getUserData(String userId) {
        String[] columns = {UserDBHelper.COLUMN_NICKNAME, UserDBHelper.COLUMN_EMAIL};
        String selection = UserDBHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId};

        Cursor cursor = db.query(UserDBHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        UserData userData = null;
        if (cursor.moveToFirst()) {
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_NICKNAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_EMAIL));
            userData = new UserData(nickname, email);
        }
        cursor.close();
        return userData;
    }

    public void deleteUser(String userId) {
        String selection = UserDBHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId};
        db.delete(UserDBHelper.TABLE_USERS, selection, selectionArgs);
    }
}

// 사용자 정보를 담을 간단한 데이터 클래스
class UserData {
    String nickname;
    String email;

    public UserData(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
}
