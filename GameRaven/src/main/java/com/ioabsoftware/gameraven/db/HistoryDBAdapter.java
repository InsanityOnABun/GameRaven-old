package com.ioabsoftware.gameraven.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.BuildConfig;
import com.ioabsoftware.gameraven.networking.NetDesc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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
            if (BuildConfig.DEBUG) AllInOneV2.wtl("starting insert history method");
            lastAddedPath = pathIn;
            if (BuildConfig.DEBUG) AllInOneV2.wtl("creating content vals obj");
            ContentValues vals = new ContentValues();
            if (BuildConfig.DEBUG) AllInOneV2.wtl("putting content vals");
            vals.put(COLUMN_HIST_PATH, pathIn);
            vals.put(COLUMN_HIST_DESC, descIn);
            vals.put(COLUMN_HIST_SRC, compress(srcIn));
            vals.put(COLUMN_HIST_VLOC_FIRSTVIS, vLocFirstVisIn);
            vals.put(COLUMN_HIST_VLOC_OFFSET, vLocOffsetIn);
            if (BuildConfig.DEBUG) AllInOneV2.wtl("inserting row");
            db.insert(TABLE_HISTORY, null, vals);
            if (BuildConfig.DEBUG) AllInOneV2.wtl("updating hasHistory");
            updateHasHistory();
            if (BuildConfig.DEBUG) AllInOneV2.wtl("insert history method completing");
        }
    }

    public History pullHistory() {
        Cursor cur = db.query(TABLE_HISTORY, null, null, null, null, null, COLUMN_HIST_ID + " DESC", "2");

        cur.moveToFirst();
        long id = cur.getLong(0);
        String path = cur.getString(1);
        History h = new History(path,
                NetDesc.valueOf(cur.getString(2)),
                decompress(cur.getBlob(3)),
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

    public static byte[] compress(byte[] data) {
        if (BuildConfig.DEBUG) AllInOneV2.wtl("starting history compression");
        try {
            Deflater deflater = new Deflater(Deflater.BEST_SPEED);
            deflater.setInput(data);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

            deflater.finish();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer); // returns the generated code... index
                outputStream.write(buffer, 0, count);
            }

            deflater.end();
            outputStream.close();
            byte[] output = outputStream.toByteArray();

            if (BuildConfig.DEBUG) AllInOneV2.wtl("CMPRSSN - Original: " + data.length / 1024 + " Kb");
            if (BuildConfig.DEBUG) AllInOneV2.wtl("CMPRSSN - Compressed: " + output.length / 1024 + " Kb");
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            return data;
        }
    }

    public static byte[] decompress(byte[] data) {
        if (BuildConfig.DEBUG) AllInOneV2.wtl("starting history decompression");
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            inflater.end();
            outputStream.close();
            byte[] output = outputStream.toByteArray();

            if (BuildConfig.DEBUG) AllInOneV2.wtl("CMPRSSN - Original: " + data.length / 1024 + " Kb");
            if (BuildConfig.DEBUG) AllInOneV2.wtl("CMPRSSN - Decompressed: " + output.length / 1024 + " Kb");
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
}
