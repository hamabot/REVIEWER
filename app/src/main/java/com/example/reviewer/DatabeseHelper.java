package com.example.reviewer;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by takeshi on 2017/08/25.
 */

public class DatabeseHelper extends SQLiteOpenHelper {
  static final private String DBNAME = "reviewer.sqlite";
  static final private int VERSION = 1;

  public DatabeseHelper(Context context) {
    super(context, DBNAME, null, VERSION);
  }

  public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
  }

  // データベース作成
  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("DROP TABLE IF EXISTS favorite");
    Log.i("Drop", "Success");
  }

  public void CreateTable(SQLiteDatabase db) {
    try {
      // テーブルの作成
      db.execSQL("CREATE TABLE IF NOT EXISTS favorite (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT," +
        " review TEXT, price TEXT, reviewUrl TEXT, proUrl TEXT)");
    } catch (SQLException e) {
      Log.i("SQL Erro", "Create Error");
    }

  }

  public void InsertData(SQLiteDatabase db, String name, String proUrl, String price, double review, String reviewUrl) {
    // データの挿入
    try {
      db.execSQL("INSERT INTO favorite (name, review, price, reviewUrl, proUrl)" +
        " VALUES ('" + name + "', '"+ review + "', '" + price + "', '" + reviewUrl + "', '" + proUrl + "')");
    } catch (SQLException e) {
      Log.i("SQL Error", "Insert Error");
    }
  }

  // データベースがバージョンアップした時、テーブルを再作成
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS favorite");
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
    db.execSQL("DROP TABLE IF EXISTS favorite");
    onCreate(db);
  }

}
