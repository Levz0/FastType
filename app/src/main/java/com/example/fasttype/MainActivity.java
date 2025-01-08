package com.example.fasttype;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createUser();
    }

    private void createUser(){
        User user = (User) getIntent().getSerializableExtra("user");

        if (user != null) {
            // Используем данные пользователя
            Toast.makeText(MainActivity.this, "Добро пожаловать, " + user.getLogin(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Ошибка при получении данных пользователя.", Toast.LENGTH_SHORT).show();
        }

        Log.d("UserDetails", user.toString());
    }
}