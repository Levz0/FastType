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
