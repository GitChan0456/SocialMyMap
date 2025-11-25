package com.example.socialmymap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "socialmap.db";
    private static final int DATABASE_VERSION = 5; // 즐겨찾기 확장 버전

    // User Table
    public static final String TABLE_USERS = "user_table";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_EMAIL = "email";

    // Favorites Table
    public static final String TABLE_FAVORITES = "favorite_places_table";
    public static final String COLUMN_FAVORITE_ID = "fav_id";
    public static final String COLUMN_FAVORITE_USER_ID_FK = "user_id"; // userId (TEXT) FK
    public static final String COLUMN_PLACE_NAME = "place_name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_CREATED_AT = "created_at";

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_NICKNAME + " TEXT, "
                + COLUMN_EMAIL + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);

        createFavoritesTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }

    private void createFavoritesTable(SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FAVORITE_USER_ID_FK + " TEXT NOT NULL, "
                + COLUMN_PLACE_NAME + " TEXT NOT NULL, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_LAT + " REAL, "
                + COLUMN_LNG + " REAL, "
                + COLUMN_CREATED_AT + " INTEGER DEFAULT (strftime('%s','now')), "
                + "UNIQUE(" + COLUMN_FAVORITE_USER_ID_FK + ", " + COLUMN_PLACE_NAME + ", " + COLUMN_ADDRESS + ") ON CONFLICT IGNORE, "
                + "FOREIGN KEY(" + COLUMN_FAVORITE_USER_ID_FK + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_fav_user ON " + TABLE_FAVORITES + "(" + COLUMN_FAVORITE_USER_ID_FK + ")");
    }
}
