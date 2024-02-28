package com.example.engwordsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "EnglishWords.db";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for our table name.
    public static final String TABLE_NAME = "Word";

    // below variable is for our id column.
    public static final String ID_COL = "id";

    // below variable is for our course name column
    public static final String WORD_COL = "word";

    // below variable id for our course duration column.
    public static final String TRANSLATION_COL = "translation";

    // below variable for our course description column.
    public static final String STATE_COL = "state";


    // creating a constructor for our database handler.
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }



    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types.
      /*  String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + WORD_COL + " TEXT,"
                + TRANSLATION_COL + " TEXT,"
                + STATE_COL + " INTEGER);";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query);
*/
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
      /*  db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);*/
    }





    // Yeni bir metot ekleyerek veritabanını açabilirsiniz.
   // public SQLiteDatabase openDatabase() {
     //   return this.getWritableDatabase();
    //}
}