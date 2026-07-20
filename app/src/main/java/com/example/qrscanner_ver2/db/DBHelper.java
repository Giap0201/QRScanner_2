package com.example.qrscanner_ver2.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.qrscanner_ver2.model.HistoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database helper.
 * Table: history (id INTEGER PK, content TEXT, type TEXT, time INTEGER)
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "qrscanner.db";
    private static final int DB_VERSION = 1;

    // Table
    public static final String TABLE_HISTORY = "history";
    public static final String COL_ID = "id";
    public static final String COL_CONTENT = "content";
    public static final String COL_TYPE = "type";
    public static final String COL_TIME = "time";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_HISTORY + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_CONTENT + " TEXT NOT NULL, "
                    + COL_TYPE + " TEXT NOT NULL, "
                    + COL_TIME + " INTEGER NOT NULL"
                    + ");";

    // Singleton
    private static DBHelper instance;

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    // ─── Insert ────────────────────────────────────────────────────────────────

    /**
     * Insert a new history item. Returns the row ID or -1 on failure.
     */
    public long insertHistory(HistoryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTENT, item.getContent());
        cv.put(COL_TYPE, item.getType());
        cv.put(COL_TIME, item.getTimestamp());
        long rowId = db.insert(TABLE_HISTORY, null, cv);
        db.close();
        return rowId;
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns all history items sorted newest-first.
     */
    public List<HistoryItem> getAllHistory() {
        List<HistoryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_HISTORY,
                null,
                null, null, null, null,
                COL_TIME + " DESC"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                HistoryItem item = new HistoryItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                item.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)));
                item.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)));
                item.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME)));
                items.add(item);
            }
            cursor.close();
        }
        db.close();
        return items;
    }

    // ─── Delete ────────────────────────────────────────────────────────────────

    /**
     * Delete a history item by its row ID.
     */
    public void deleteById(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_HISTORY, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * Delete all history.
     */
    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
        db.close();
    }
}
