package com.example.fasttype;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        Regform_Slide();

    }
    public void onClickCheckValidity(View view){
        EditText loginTxt = findViewById(R.id.loginInput);
        EditText passwordTxt = findViewById(R.id.passwordInput);

        String login = loginTxt.getText().toString();
        String password = passwordTxt.getText().toString();

        if (login.isEmpty()){
            Toast.makeText(LoginActivity.this, "Не заполнен логин", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty())
        {
            Toast.makeText(LoginActivity.this, "Не заполнен пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            Button button = findViewById(R.id.Btn_auth);
            button.setEnabled(false);
            Authorization(login, password);
            button.setEnabled(true);
        }
    };
    private void Authorization(String inputLogin, String inputPassword) {
        Log.d("LoginActivity", "Начало авторизации. Введенный логин: " + inputLogin + ", пароль: " + inputPassword);

        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = database.child("users");



        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                Log.d("LoginActivity", "get() успешно: " + snapshot.getValue());
            } else {
                Log.e("LoginActivity", "Ошибка get(): ", task.getException());
            }
        });


        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Логируем полный ответ от сервера
                Log.d("LoginActivity", "Полный ответ сервера: " + snapshot.getValue());
                Log.d("LoginActivity", "Количество пользователей: " + snapshot.getChildrenCount());

                boolean authorized = false;
                Map<String, Object> tests = new HashMap<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String login = userSnapshot.getKey();
                    String password = userSnapshot.child("password").getValue(String.class);
                    Log.d("LoginActivity", "Найден пользователь: " + login + ", пароль: " + password);

                    // Сравниваем введённые данные с данными из базы
                    if (checkAuthValidity(inputLogin.trim(), inputPassword.trim(), login.trim(), password != null ? password.trim() : "")) {
                        Log.d("LoginActivity", "Авторизация успешна для пользователя: " + login);

                        // Логируем данные тестов
                        for (DataSnapshot testSnapshot : userSnapshot.child("test_results").getChildren()) {
                            String key = testSnapshot.getKey();
                            Object value = testSnapshot.getValue();
                            Log.d("LoginActivity", "Результат теста - ключ: " + key + ", значение: " + value);
                            if (value != null && !value.equals("")) {
                                tests.put(key, value);
                            }
                        }
                        authorized = true;
                        break;
                    }
                }

                if (authorized) {
                    Log.d("LoginActivity", "Пользователь авторизован. Переход в MainActivity.");
                    User user = new User(inputLogin, inputPassword, tests);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("user", user);
                    Toast.makeText(LoginActivity.this, "Авторизация прошла успешно", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                } else {
                    Log.d("LoginActivity", "Авторизация не пройдена. Неверный логин или пароль.");
                    Toast.makeText(LoginActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LoginActivity", "Ошибка базы данных: " + error.getMessage());
                Toast.makeText(LoginActivity.this, "Ошибка чтения данных", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private boolean checkAuthValidity(String inputLogin, String inputPassword, String login, String password){
       if (inputLogin.equals(login) && inputPassword.equals(password))
       {
           return true;
       }
       else{
           return false;
       }

    }

    private void Regform_Slide() {
        TextView registration = findViewById(R.id.registration);
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegformActivity.class);
                startActivity(intent);
            }
        });
    }
}
