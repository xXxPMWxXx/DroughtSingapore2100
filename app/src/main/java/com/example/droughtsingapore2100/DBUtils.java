package com.example.droughtsingapore2100;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DBUtils extends SQLiteOpenHelper {

    public static final String databaseName = "scores.db";
    public DBUtils(@Nullable Context context) {
        super(context, "scores.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE scores(id INTEGER PRIMARY KEY AUTOINCREMENT, score INTEGER, date TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop Table if exists scores");
    }

    public void insertScore(int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("score", score);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());
        contentValues.put("date", currentDateTime);

        db.insert("scores", null, contentValues);
        db.close();
        // Insert the new record and check if it is in top 3
        ensureTopScores(3);
    }

    // Method to only keep TOP 3 score record
    private void ensureTopScores(int limit) {
        SQLiteDatabase db = this.getWritableDatabase();
        String DELETE_EXCESS = "DELETE FROM scores WHERE id NOT IN (SELECT id FROM scores ORDER BY score DESC LIMIT " + limit + ")";
        db.execSQL(DELETE_EXCESS);
        db.close();
    }

    @SuppressLint("Range")
    public List<ScoreEntry> getTopScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT score, date FROM scores ORDER BY score DESC, date ASC LIMIT 3", null);
        if (cursor.moveToFirst()) {
            do {
                int score = cursor.getInt(cursor.getColumnIndex("score"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                scores.add(new ScoreEntry(score, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return scores;
    }

    public static class ScoreEntry {
        private final int score;
        private final String date;

        public ScoreEntry(int score, String date) {
            this.score = score;
            this.date = date;
        }

        public int getScore() {
            return score;
        }

        public String getDate() {
            return date;
        }
    }

}
