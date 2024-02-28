package com.example.engwordsapp;

public class Word {
    private int id;
    private String word;
    private String translation;
    private int state;

    // Constructor
    public Word() {
    }

    // Getter ve Setter metotlarý
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
