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
        String[] columns = { UserDBHelper.COLUMN_ID };
        String selection = UserDBHelper.COLUMN_USER_ID + " = ?" + " AND " + UserDBHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { userId, password };

        Cursor cursor = db.query(UserDBHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public UserData getUserData(String userId) {
        String[] columns = { UserDBHelper.COLUMN_NICKNAME, UserDBHelper.COLUMN_EMAIL };
        String selection = UserDBHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { userId };

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
        String[] selectionArgs = { userId };
        db.delete(UserDBHelper.TABLE_USERS, selection, selectionArgs);
    }

    public int updateUser(String userId, String newNickname, String newEmail, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(UserDBHelper.COLUMN_NICKNAME, newNickname);
        values.put(UserDBHelper.COLUMN_EMAIL, newEmail);

        // 새 비밀번호가 입력된 경우에만 업데이트
        if (newPassword != null && !newPassword.isEmpty()) {
            values.put(UserDBHelper.COLUMN_PASSWORD, newPassword);
        }

        String selection = UserDBHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { userId };

        return db.update(UserDBHelper.TABLE_USERS, values, selection, selectionArgs);
    }

    // 아이디 중복 체크
    public boolean isUserIdExists(String userId) {
        String[] columns = { UserDBHelper.COLUMN_ID };
        String selection = UserDBHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { userId };

        Cursor cursor = db.query(UserDBHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // 닉네임 중복 체크
    public boolean isNicknameExists(String nickname) {
        String[] columns = { UserDBHelper.COLUMN_ID };
        String selection = UserDBHelper.COLUMN_NICKNAME + " = ?";
        String[] selectionArgs = { nickname };

        Cursor cursor = db.query(UserDBHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    /**
     * 우리 동네 정보 업데이트
     */
    public boolean updateHomeRegion(String userId, String homeRegion, double lat, double lng) {
        ContentValues values = new ContentValues();
        values.put(UserDBHelper.COLUMN_HOME_REGION, homeRegion);
        values.put(UserDBHelper.COLUMN_HOME_LAT, lat);
        values.put(UserDBHelper.COLUMN_HOME_LNG, lng);

        String selection = UserDBHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { userId };

        int result = db.update(UserDBHelper.TABLE_USERS, values, selection, selectionArgs);
        return result > 0;
    }

    /**
     * 우리 동네 정보 조회
     */
    public HomeRegionData getHomeRegion(String userId) {
        String[] columns = {
            UserDBHelper.COLUMN_HOME_REGION,
            UserDBHelper.COLUMN_HOME_LAT,
            UserDBHelper.COLUMN_HOME_LNG
        };
        String selection = UserDBHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { userId };

        Cursor cursor = db.query(UserDBHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        HomeRegionData data = null;
        if (cursor.moveToFirst()) {
            String region = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_HOME_REGION));
            double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_HOME_LAT));
            double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_HOME_LNG));

            if (region != null && !region.isEmpty()) {
                data = new HomeRegionData(region, lat, lng);
            }
        }
        cursor.close();
        return data;
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