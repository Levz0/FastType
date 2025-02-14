package com.example.fasttype;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String login;
    private String password;
    private Map<String, Object> test_results;
    public  User(String login, String password, Map<String, Object> tests)
    {
        this.login = login;
        this.password = password;
        this.test_results = tests;
    }
    public Map<String, Object> getResults()
    {
        return test_results;
    }

    public void setResults(String formattedDate, double accuracy, double wpm, double duration) {
        if (test_results == null) {
            test_results = new HashMap<>();
        }
        String testId = "test_" + System.currentTimeMillis();

        Map<String, Object> testData = new HashMap<>();
        testData.put("date", formattedDate);
        testData.put("accuracy", accuracy);
        testData.put("wpm", wpm);
        testData.put("duration", Math.round(duration * 100.0) / 100.0); // Округление до 2 знаков

        test_results.put(testId, testData);
    }


    public User(String login, String password)
    {
        this(login, password, new HashMap<>());
    }
    public String getLogin(){
        return login;
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", test_results=" + test_results +
                '}';
    }

}
