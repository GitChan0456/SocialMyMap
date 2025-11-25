package com.example.socialmymap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class FavoritesDao {

    private SQLiteDatabase db;
    private final UserDBHelper dbHelper;

    public FavoritesDao(Context context) {
        dbHelper = new UserDBHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addFavorite(String userId, String placeName, String address, Double lat, Double lng) {
        ContentValues values = new ContentValues();
        values.put(UserDBHelper.COLUMN_FAVORITE_USER_ID_FK, userId);
        values.put(UserDBHelper.COLUMN_PLACE_NAME, placeName);
        values.put(UserDBHelper.COLUMN_ADDRESS, address);
        if (lat != null) values.put(UserDBHelper.COLUMN_LAT, lat);
        if (lng != null) values.put(UserDBHelper.COLUMN_LNG, lng);
        return db.insertWithOnConflict(UserDBHelper.TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<FavoriteItem> getFavoritesByUser(String userId) {
        List<FavoriteItem> result = new ArrayList<>();
        String[] columns = {
                UserDBHelper.COLUMN_FAVORITE_ID,
                UserDBHelper.COLUMN_PLACE_NAME,
                UserDBHelper.COLUMN_ADDRESS,
                UserDBHelper.COLUMN_LAT,
                UserDBHelper.COLUMN_LNG
        };
        String selection = UserDBHelper.COLUMN_FAVORITE_USER_ID_FK + " = ?";
        String[] args = {userId};
        Cursor cursor = db.query(UserDBHelper.TABLE_FAVORITES, columns, selection, args, null, null, UserDBHelper.COLUMN_CREATED_AT + " DESC");
        while (cursor.moveToNext()) {
            FavoriteItem item = new FavoriteItem();
            item.id = cursor.getLong(cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_FAVORITE_ID));
            item.placeName = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_PLACE_NAME));
            item.address = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_ADDRESS));
            int latIdx = cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_LAT);
            int lngIdx = cursor.getColumnIndexOrThrow(UserDBHelper.COLUMN_LNG);
            if (!cursor.isNull(latIdx)) item.lat = cursor.getDouble(latIdx);
            if (!cursor.isNull(lngIdx)) item.lng = cursor.getDouble(lngIdx);
            result.add(item);
        }
        cursor.close();
        return result;
    }

    public int deleteFavorite(long favId) {
        String where = UserDBHelper.COLUMN_FAVORITE_ID + " = ?";
        String[] args = {String.valueOf(favId)};
        return db.delete(UserDBHelper.TABLE_FAVORITES, where, args);
    }

    public static class FavoriteItem {
        public long id;
        public String placeName;
        public String address;
        public Double lat;
        public Double lng;
    }
}
