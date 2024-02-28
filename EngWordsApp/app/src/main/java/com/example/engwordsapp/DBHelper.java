package com.example.engwordsapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DBHelper {
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public DBHelper(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = databaseHelper.getWritableDatabase();
    }

    public void close() {
        databaseHelper.close();
    }



    // State değeri 0 olan ilk kelimeyi getirir.
    @SuppressLint("Range")
    public String getFirstWordWithStateZero() {
        String result = null;

        String[] columns = {DatabaseHelper.WORD_COL};
        String selection = DatabaseHelper.STATE_COL + " = ?";
        String[] selectionArgs = {"0"};
        String orderBy = DatabaseHelper.ID_COL + " ASC";
        String limit = "1";

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy,
                limit
        );

        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WORD_COL));
        }

        cursor.close();

        return result;
    }



    // kelime bilindiğinde durumu 1 olacak
    public void updateWordState(String word, int newState) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.STATE_COL, newState);

        String whereClause = DatabaseHelper.WORD_COL + " = ?";
        String[] whereArgs = {word};

        database.update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);
    }



    // kelime çevirisi
    @SuppressLint("Range")
    public String getWordTranslation(String word) {
        String translation = null;

        String[] columns = {DatabaseHelper.TRANSLATION_COL};
        String selection = DatabaseHelper.WORD_COL + "=?";
        String[] selectionArgs = {word};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            translation = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANSLATION_COL));
        }

        cursor.close();

        return translation;
    }

    // State değeri 0 olan bir sonraki kelimeyi getirir
    @SuppressLint("Range")
    public String getNextWord(String currentWord) {
        String result = null;

        // SQLite sorgusunu oluşturalım, state=0 ve ID'si currentWord kelimesinin ID'sinden büyük olan en küçük ID'ye sahip kelimeyi getirelim
        String query = "SELECT " + DatabaseHelper.WORD_COL +
                " FROM " + DatabaseHelper.TABLE_NAME +
                " WHERE " + DatabaseHelper.STATE_COL + " = 0 AND " + DatabaseHelper.ID_COL + " > " + getIdFromWord(currentWord) +
                " ORDER BY " + DatabaseHelper.ID_COL +
                " LIMIT 1";

        Cursor cursor = database.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WORD_COL));
            cursor.close();
        }

        return result;
    }


    // ID_COL'a göre kelimenin ID değerini getiren yardımcı fonksiyon
    @SuppressLint("Range")
    public int getIdFromWord(String word) {
        int id = -1;

        String[] columns = {DatabaseHelper.ID_COL};
        String selection = DatabaseHelper.WORD_COL + " = ?";
        String[] selectionArgs = {word};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ID_COL));
        }

        cursor.close();

        return id;
    }

    //o anki kelimeden sonra state değeri 0 olan kelime var mı diye kontrol edilir
    public boolean hasFollowingWordWithStateZero(int currentWordId) {

        // Veritabanında state değeri 0 olan bir sonraki kelimeyi sorgulayın
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NAME + " WHERE " +
                DatabaseHelper.ID_COL + " > " + currentWordId + " AND " +
                DatabaseHelper.STATE_COL + " = 0 LIMIT 1";

        Cursor cursor = database.rawQuery(query, null);
        boolean result = cursor.moveToNext();

        // Cursor ve veritabanını temizleyin
        cursor.close();


        return result;
    }

    // state değeri 0 olan ilk kelimeyi tekrar getirir
    @SuppressLint("Range")
    public String getFirstWordWithStateZeroAgain(String currentWord) {
        String result = null;

        // SQLite sorgusunu oluşturalım, state=0 ve ID'si currentWord kelimesinin ID'sinden büyük olan en küçük ID'ye sahip kelimeyi getirelim
        String query = "SELECT " + DatabaseHelper.WORD_COL +
                " FROM " + DatabaseHelper.TABLE_NAME +
                " WHERE " + DatabaseHelper.STATE_COL + " = 0 "  +
                " ORDER BY " + DatabaseHelper.ID_COL +
                " LIMIT 1";

        Cursor cursor = database.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WORD_COL));
            cursor.close();
        }

        return result;
    }

    // State değeri 0 olan bir sonraki kelimeyi tekrar getirir
    @SuppressLint("Range")
    public String getNextWordAgain(int currentWordId) {
        String result = null;
        // Veritabanında state değeri 0 olan bir sonraki kelimeyi sorgulayın
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NAME + " WHERE " +
                DatabaseHelper.ID_COL + " > " + currentWordId + " AND " +
                DatabaseHelper.STATE_COL + " = 0 LIMIT 1";

        Cursor cursor = database.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WORD_COL));
            cursor.close();
        }

        return result;
    }



    // Veritabanındaki kelimeleri rastgele sıralayacak metod
    public void shuffleDatabase() {
        List<Word> allWords = getAllWords(); // Veritabanından tüm kelimeleri çekin (Word sınıfı bir örnek, verileri temsil eder)

        // Verileri rastgele karıştırmak için koleksiyonu kullanabilirsiniz
        long seed = System.nanoTime(); // Rastgelelik için seed değeri alınır
        Collections.shuffle(allWords, new Random(seed));

        // Rastgele sıralanmış verileri veritabanına geri kaydedin
        updateAllWordsInDatabase(allWords);
    }




    // reset butonuna basıldığında kelimeelr irandom olarak karıştırmak için
    // tüm kelimeleri getiren metod
    public List<Word> getAllWords() {
        List<Word> allWords = new ArrayList<>();

        String[] columns = {
                DatabaseHelper.ID_COL,
                DatabaseHelper.WORD_COL,
                DatabaseHelper.TRANSLATION_COL,
                DatabaseHelper.STATE_COL
        };

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ID_COL));
            @SuppressLint("Range") String word = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WORD_COL));
            @SuppressLint("Range") String translation = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANSLATION_COL));
            @SuppressLint("Range") int state = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.STATE_COL));

            Word wordObj = new Word();
            wordObj.setId(id);
            wordObj.setWord(word);
            wordObj.setTranslation(translation);
            wordObj.setState(state);

            allWords.add(wordObj);
        }

        cursor.close();

        return allWords;
    }




    // Veritabanındaki tüm kelimeleri güncelleyen metod
    private void updateAllWordsInDatabase(List<Word> words) {

        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.WORD_COL, word.getWord());
            values.put(DatabaseHelper.TRANSLATION_COL, word.getTranslation());
            values.put(DatabaseHelper.STATE_COL, word.getState());

            database.update(
                    DatabaseHelper.TABLE_NAME,
                    values,
                    DatabaseHelper.ID_COL + " = ?",
                    new String[]{String.valueOf(i + 1)} // Burada id'ler 1'den başladığı varsayılarak güncelleniyor
            );
        }

        database.close();
    }



    // ID_COL'a göre kelimenin ID değerini getiren yardımcı fonksiyon
    @SuppressLint("Range")
    public String getWordFromId(int id) {
       String word = null;

        String[] columns = {DatabaseHelper.WORD_COL};
        String selection = DatabaseHelper.ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            word = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WORD_COL));
        }

        cursor.close();

        return word;
    }





}
