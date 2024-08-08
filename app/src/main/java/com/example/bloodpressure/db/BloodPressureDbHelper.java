package com.example.bloodpressure.db;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BloodPressureDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bloodpressure.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "readings";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SYSTOLIC = "systolic";
    public static final String COLUMN_DIASTOLIC = "diastolic";
    public static final String COLUMN_PULSE = "pulse";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SYSTOLIC + " REAL, " +
                    COLUMN_DIASTOLIC + " REAL, " +
                    COLUMN_PULSE + " REAL, " +
                    COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    public BloodPressureDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
