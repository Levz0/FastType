package com.example.fasttype;

public class UserRecord {
    private String username;
    private String date;
    private double accuracy;
    private double wpm;

    public UserRecord(String username, String date, double accuracy, double wpm) {
        this.username = username;
        this.date = date;
        this.accuracy = accuracy;
        this.wpm = wpm;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getWpm() {
        return wpm;
    }
}
