package com.example.engwordsapp;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private Button buttonReset;
    private Button buttonStart;

    private SQLiteDatabase database;
    private DatabaseHelper db;
    public static boolean isReset = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DatabaseHelper sınıfından bir nesne oluştur
        db = new DatabaseHelper(this);
        // Veritabanına erişim sağlamak için SQLiteDatabase nesnesini al
        database = db.getWritableDatabase();

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.open();


        buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Reset");
                builder.setMessage("Are you sure you want to reset?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isReset = true;
                        // Kullanıcı "Evet" dediğinde, tüm kelimelerin state değerini 0 yapmak için aşağıdaki metodu çağır
                        updateAllWordStatesToZero();
                        // ayrıca veritabaındaki kelimeler random olarak karıştırılsın
                        dbHelper.shuffleDatabase();
                        openActivity2();

                    }
                });
                builder.setNegativeButton("No", null); // Kullanıcı "Hayır" dediğinde, herhangi bir işlem yapma
                builder.show();
            }
        });

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();
            }
        });


// creating a constructor for our database handler.
        db= new DatabaseHelper(this);

            SQLiteOpenHelper helper = new DatabaseHelper(this); // 'this' kullanımı burada geçerli olacaktır.
            helper.getWritableDatabase(); // Veritabanını yazılabilir modda açar
           // db.getWritableDatabase();

        //dosyadaki verileri db'ye atama
       // readWeatherData( db);
       // Log.d("DEBUG", "burdaaa");

        printTableData(db);

    }


    public void openActivity2() {
        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }



    private boolean isDataInserted() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean("DATA_INSERTED", false);
    }



    private void readWeatherData(DatabaseHelper db) {

        SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();

        if (!isDataInserted()) {

       int readWeatherDataCount = 0;
        Log.d("DEBUG", "readWeatherData() called " + ++readWeatherDataCount + " times.");

       InputStream is = getResources().openRawResource(R.raw.kelimeler_a1);
       BufferedReader reader = new BufferedReader(
               new InputStreamReader(is, Charset.forName("UTF-8"))
       );

       String line = "";
       sqLiteDatabase.beginTransaction();

       try {
           reader.readLine();

       while ((line = reader.readLine()) != null) {
         //  Log.d("MyActivity","Line: "+line);
           String[] tokens = line.split(";");
           ContentValues cv = new ContentValues();
           cv.put("word",tokens[0]);
           cv.put("translation",tokens[1]);
           cv.put("state",tokens[2]);
          sqLiteDatabase.insert("Word", null, cv);

           //Log.d("MyActivity", "just created: ");
       }
       } catch (IOException e) {
          // Log.wtf("MyActivity","Error reading data file on line " +line, e);
           e.printStackTrace();
       }

        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();

            // Verilerin veritabanına eklendiğini gösteren bayrağı (flag) işaretle
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putBoolean("DATA_INSERTED", true).apply();
        }
        printTableData(db);
    }


    public void printTableData(DatabaseHelper db ) {
        Log.d("YOUR_TAG", "buraya1 kere");
        SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM Word", null);
        try {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String word = cursor.getString(cursor.getColumnIndex("word"));
                @SuppressLint("Range") String translation = cursor.getString(cursor.getColumnIndex("translation"));
                @SuppressLint("Range") int state = cursor.getInt(cursor.getColumnIndex("state"));

                Log.d("YOUR_TAG", "ID: " + id + ", Word: " + word + ", Translation: " + translation + ", State: " + state);
            }
        } finally {
            cursor.close();
        }
    }


    private void updateAllWordStatesToZero() {
        // Veritabanından tüm kelimeleri seçin
        Cursor cursor = database.rawQuery("SELECT * FROM Word", null);

        // Eğer veritabanında bir şeyler varsa
        if (cursor.moveToFirst()) {
            do {
                // state değerini 0 yapmak için ContentValues oluşturun
                ContentValues values = new ContentValues();
                values.put("state", 0);

                // Güncellemeyi yapın
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                database.update("Word", values, "id=?", new String[]{String.valueOf(id)});
            } while (cursor.moveToNext());
        }

        // Cursor'ı kapatın
        cursor.close();
    }
}


           /*
           Word word = new Word();
           word.setWord(tokens[0]);
           word.setTranslation(tokens[1]);
           word.setState(Integer.parseInt(tokens[2]));
           words.add(word);
            */