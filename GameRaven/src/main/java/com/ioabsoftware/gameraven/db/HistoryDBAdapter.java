package com.ioabsoftware.gameraven.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;

public class HistoryDBAdapter {

    private static final String DATABASE_NAME = "history.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_HISTORY = "history";
    public static final String COLUMN_HIST_ID = "_id";
    public static final String COLUMN_HIST_PATH = "path";
    public static final String COLUMN_HIST_DESC = "desc";
    public static final String COLUMN_HIST_SRC = "src";
    public static final String COLUMN_HIST_VLOC_FIRSTVIS = "vlocfirstvis";
    public static final String COLUMN_HIST_VLOC_OFFSET = "vlocoffset";

    private static final String CREATE_TABLE_HISTORY =
            "create table " + TABLE_HISTORY + "(" +
                    COLUMN_HIST_ID + " integer primary key autoincrement, " +
                    COLUMN_HIST_PATH + " text not null, " +
                    COLUMN_HIST_DESC + " text not null, " +
                    COLUMN_HIST_SRC + " blob not null, " +
                    COLUMN_HIST_VLOC_FIRSTVIS + " integer not null, " +
                    COLUMN_HIST_VLOC_OFFSET + " integer not null);";

    private boolean hasHistory = false;
    private String lastAddedPath = AllInOneV2.EMPTY_STRING;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private final AllInOneV2 aio;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_HISTORY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
            onCreate(db);
        }
    }

    public HistoryDBAdapter(AllInOneV2 aioIn) {
        aio = aioIn;
    }

    public HistoryDBAdapter open() {
        dbHelper = new DatabaseHelper(aio);
        db = dbHelper.getWritableDatabase();
        clearTable();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void clearTable() {
        db.delete(TABLE_HISTORY, null, null);
        updateHasHistory();
    }

    public void insertHistory(String pathIn, String descIn, byte[] srcIn, int vLocFirstVisIn, int vLocOffsetIn) {
        if (!lastAddedPath.equals(pathIn)) {
            aio.wtl("starting insert history method");
            lastAddedPath = pathIn;
            aio.wtl("creating content vals obj");
            ContentValues vals = new ContentValues();
            aio.wtl("putting content vals");
            vals.put(COLUMN_HIST_PATH, pathIn);
            vals.put(COLUMN_HIST_DESC, descIn);
            vals.put(COLUMN_HIST_SRC, srcIn);
            vals.put(COLUMN_HIST_VLOC_FIRSTVIS, vLocFirstVisIn);
            vals.put(COLUMN_HIST_VLOC_OFFSET, vLocOffsetIn);
            aio.wtl("inserting row");
            db.insert(TABLE_HISTORY, null, vals);
            aio.wtl("updating hasHistory");
            updateHasHistory();
            aio.wtl("insert history method completing");
        }
    }

    public History pullHistory() {
        Cursor cur = db.query(TABLE_HISTORY, null, null, null, null, null, COLUMN_HIST_ID + " DESC", "2");

        cur.moveToFirst();
        long id = cur.getLong(0);
        String path = cur.getString(1);
        History h = new History(path,
                NetDesc.valueOf(cur.getString(2)),
                cur.getBlob(3),
                new int[]{cur.getInt(4), cur.getInt(5)});

        if (cur.moveToNext())
            lastAddedPath = cur.getString(1);
        else
            lastAddedPath = AllInOneV2.EMPTY_STRING;

        cur.close();

        db.delete(TABLE_HISTORY, COLUMN_HIST_ID + " = " + id, null);
        updateHasHistory();

        return h;
    }

    private void updateHasHistory() {
        hasHistory = DatabaseUtils.queryNumEntries(db, TABLE_HISTORY) > 0;
    }

    public boolean hasHistory() {
        return hasHistory;
    }

}
