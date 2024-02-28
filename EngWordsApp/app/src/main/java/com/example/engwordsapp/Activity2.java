package com.example.engwordsapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.sqlite.core.DB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class Activity2 extends AppCompatActivity {

    private TextView textViewWord;
    private ImageButton buttonNext;
    private ImageButton buttonCheckMark;
    private ImageButton buttonCross;
    private TextView textViewTranslation;
    private ImageButton buttonExit;
    private ImageButton buttonMusic;
    private MediaPlayer mediaPlayer;

    private ImageButton buttonPronunciation;
    private TextToSpeech textToSpeech;

    private String translation = null;

    private static final String FILE_NAME = "kelime_id.txt";


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        textViewWord = findViewById(R.id.textView_Word);
        buttonNext = findViewById(R.id.buttonNext);
        buttonCheckMark = findViewById(R.id.buttonCheckMark);
        buttonCross = findViewById(R.id.buttonCross);
        textViewTranslation = findViewById(R.id.textViewTranslation);
        buttonExit = findViewById(R.id.buttonExit);
        // Müzik butonunu al
        buttonMusic = findViewById(R.id.musicButton);
        buttonMusic.setVisibility(View.INVISIBLE);
        buttonPronunciation = findViewById(R.id.pronunciationButton);

        // MediaPlayer nesnesini oluştur
        mediaPlayer = MediaPlayer.create(this, R.raw.metin2);

        //ikinci sayfa açıldığında state değeri sıfır olan ilk kelimeyi göster
        showFirstWord();


        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper dbHelper = new DBHelper(Activity2.this);
                dbHelper.open();

                //ekrana kelimenin çevirisi gelmişse mark veya cross butonuna basılmış demektir
                //oyleyse bir sonraki kelimeye geç
                if(translation != null) {
                    DBControl();  //bir sonraki kelimeyi göster
                    textViewTranslation.setVisibility(View.INVISIBLE);
                }

                translation = null;



            }

        });


        buttonCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String wordToConfirm = textViewWord.getText().toString();

                DBHelper dbHelper = new DBHelper(Activity2.this);
                dbHelper.open();

                // Doğru bilinen kelimenin state değerini 1 olarak güncelle
                dbHelper.updateWordState(wordToConfirm, 1);
                Log.d("debug", "mark block, state changed as 1");

                dbHelper.close();

                showTranslation();
            }
        });


        buttonCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String wordToConfirm = textViewWord.getText().toString();

                DBHelper dbHelper = new DBHelper(Activity2.this);
                dbHelper.open();

                // Doğru bilinen kelimenin state değerini 1 olarak güncelle
                dbHelper.updateWordState(wordToConfirm, 0);
                Log.d("debug", "cross block, state changed as 0");

                dbHelper.close();

                showTranslation();
            }
        });


        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //çıkış yapmadan hemen önceki kelimenin id'si dosyaya kaydediliyor
                //giriş yapıldığında kullanıcı bu kelimeden başlayacak
                //DBHelper dbHelper = new DBHelper(Activity2.this);
                //dbHelper.open();
                //int wordId = dbHelper.getIdFromWord(textViewWord.getText().toString());
                String word = textViewWord.getText().toString();
                saveWordIdToFile(word); // Kelimeyi dosyada kaydedin

                // Exit butonuna tıklandığında Main Activity'yi aç
                openMainActivity();
            }
        });


        // Müzik butonuna tıklama işlemini ekle
        buttonMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    // Müzik çalıyorsa durdur
                    mediaPlayer.pause();
                    // buttonMusic.setText("Play Music");
                } else {
                    // Müzik çalmıyorsa başlat
                    mediaPlayer.start();
                    //buttonMusic.setText("Pause Music");
                }
            }
        });


        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Dil desteklenmiyor veya eksik veri
                        // Burada uygun bir geri bildirim yapabilirsiniz.
                    }
                } else {
                    // TextToSpeech başlatılamadı, hata durumuna göre geri bildirim yapabilirsiniz.
                }
            }
        });


        buttonPronunciation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = textViewWord.getText().toString(); // Sesli olarak okunmasını istediğiniz kelime
                float pitch = 1.0f; // Ses tonu (0.0f ile 2.0f arasında değer, 1.0f varsayılan)
                float speed = 1.0f; // Konuşma hızı (0.0f ile 2.0f arasında değer, 1.0f varsayılan)

                textToSpeech.setPitch(pitch);
                textToSpeech.setSpeechRate(speed);
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        // Aktivite arka plana geçtiğinde müziği durdur
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }

        //Aktvite arka plana geçtiğinde o anki kelimeyi dosyaya kaydet
        String word = textViewWord.getText().toString();
        saveWordIdToFile(word); // Kelimenin ID'sini dosyada kaydedin
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        // Aktiviteye geri döndüğünde müziği başlat
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }*/


    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        //uygulama kapatıldığında o an ekranda olan kelimeyi dosyaya kaydet
        String word = textViewWord.getText().toString();
        saveWordIdToFile(word); // Kelimenin ID'sini dosyada kaydedin

        super.onDestroy();
    }


    //private void showWord(String word) {

    //    textViewWord.setText(word);

    //}


    private void showTranslation() {

        // Kelimenin doğru çevirisini veritabanından alıp TextView'da göster
        String word = textViewWord.getText().toString();//.substring(12); // "İlk kelime: " kısmını kaldırıyoruz
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.open();

        String translation1 = dbHelper.getWordTranslation(word);

        Log.d("translation:", translation1);
        textViewTranslation.setText(translation1);
        textViewTranslation.setVisibility(View.VISIBLE); // Translation TextView'ını göster

        translation = translation1;

        dbHelper.close();
    }


    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Activity2'yi kapat, böylece kullanıcı geri tuşuna bastığında direkt Main Activity'e dönmez
    }


    private void DBControl() {
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.open();

        String currentWord = textViewWord.getText().toString();

        int currentWordId = dbHelper.getIdFromWord(currentWord);

      if ( dbHelper.getFirstWordWithStateZero() != null  && !dbHelper.hasFollowingWordWithStateZero(currentWordId)) {
          new AlertDialog.Builder(this)
                  .setTitle("")
                  .setMessage("Tum kelimeleri gordunuz! Bilmediginiz kelimeler tekrar gosteriliyor...")
                  .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          String word = dbHelper.getFirstWordWithStateZeroAgain(currentWord);
                          textViewWord.setText(word);
                      }
                  })
                  .show();
      }

      // Veritabanında state değeri 0 olan kelime yoksa
      else if(dbHelper.getFirstWordWithStateZero() == null ){
          new AlertDialog.Builder(this)
                  .setTitle("Tebrikler!")
                  .setMessage("Tum kelimeleri bildiniz!")
                  .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          openMainActivity();
                      }
                  })
                  .show();
      }

      else if(dbHelper.getFirstWordWithStateZero() != null  && dbHelper.hasFollowingWordWithStateZero(currentWordId)){
          textViewWord.setText(dbHelper.getNextWordAgain(dbHelper.getIdFromWord(currentWord)));

      }

   }




    private void saveWordIdToFile(String word) {
        try {
            File file = new File(getFilesDir(), FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(word.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showFirstWord() {
        try {
            FileInputStream fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String word = br.readLine(); // Dosyadan veriyi oku ve word değişkenine ata
            br.close();

            //eğer resetlenirse db'deki ilk kelime ekrana gelir
            if(MainActivity.isReset == true){
                DBHelper dbHelper = new DBHelper(this);
                dbHelper.open();
                word = dbHelper.getFirstWordWithStateZero();
                textViewWord.setText(word);
                MainActivity.isReset =false;
            }
            //resetlenmezse dosyadaki kelime ekrana gelir
            else{
                textViewWord.setText(word);
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}


/*
    public void GetTextFromSql(View v){

        DatabaseHelper db= new DatabaseHelper(this);
        //SQLiteDatabase db = this.getReadableDatabase();

        TextView textView = findViewById(R.id.textView_Word);

        try {
            Connection connect = ConnectionHelper.connectionclass();

            if (connect != null) {
                String query = "Select * from Word";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    textView.setText(rs.getString(1));
                }

                rs.close();
                st.close();
                connect.close();
            } else {
                // Bağlantı hatası durumunda bir mesaj gösterebilirsiniz
                textView.setText("Check Connection");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }*/
