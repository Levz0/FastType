package com.example.fasttype;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Regform_Slide();

    }

    public void onClickCheckClickValidity(View view){
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
        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = database.child("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean authorized = false;
                Map<String, Object> tests = new HashMap<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren())
                {
                    String login = userSnapshot.getKey();
                    String password = userSnapshot.child("password").getValue(String.class);
                    if (checkAuthValidity(inputLogin, inputPassword, login, password))
                    {
                        for (DataSnapshot testSnapshot : userSnapshot.child("test_results").getChildren())
                        {
                            String key = testSnapshot.getKey();
                            Object value = testSnapshot.getValue();
                            if (value != "" && value != null)
                            {
                                tests.put(key, value);
                            }
                        }
                        authorized = true;
                        break;
                    }
                }
                if (authorized)
                {
                    User user = new User(inputLogin, inputPassword, tests);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("user", user);
                    Toast.makeText(LoginActivity.this, "Авторизация прошла успешно", Toast.LENGTH_SHORT).show();
                    Toast.makeText(LoginActivity.this, "Добро пожаловать " + inputLogin, Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Ошибка чтении данных", Toast.LENGTH_SHORT).show();
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
