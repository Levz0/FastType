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

public class RegformActivity extends AppCompatActivity {
    private DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regform);
        LoginForm_slide();
        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = database.child("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren())
                {
                    String login = userSnapshot.getKey();
                    String password = userSnapshot.child("password").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void onClickCheckClickValidity(View view){
        EditText loginTxt = findViewById(R.id.loginInput);
        EditText passwordTxt = findViewById(R.id.passwordInput);
        EditText confirmPasswordTxt = findViewById(R.id.passwordConfirm);
        String login = loginTxt.getText().toString();
        String password = passwordTxt.getText().toString();
        String confirmPassword = confirmPasswordTxt.getText().toString();

        if (login.isEmpty()){
            Toast.makeText(RegformActivity.this, "Не заполнен логин", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (login.length() < 4)
        {
            Toast.makeText(RegformActivity.this, "Длина логина должна быть больше 3 символов!", Toast.LENGTH_SHORT).show();
        }
        else if (password.isEmpty())
        {
            Toast.makeText(RegformActivity.this, "Не заполнен пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (password.length() < 6)
        {
            Toast.makeText(RegformActivity.this, "Длина пароля должна быть больше 5 символов!", Toast.LENGTH_SHORT).show();
        }
        else if (confirmPassword.isEmpty())
        {
            Toast.makeText(RegformActivity.this, "Не подтвержден пароль", Toast.LENGTH_SHORT).show();
        }
        else if (!confirmPassword.equals(password))
        {
            Toast.makeText(RegformActivity.this, "Пароли не совпадают!", Toast.LENGTH_SHORT).show();
        }
        else{
            Button button = findViewById(R.id.Btn_reg);
            button.setEnabled(false);
            Registration(login, password);
            button.setEnabled(true);
        }
    }

    private void Registration(String inputLogin, String inputPassword)
    {
        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = database.child("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean registered = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String login = userSnapshot.getKey();
                    if (inputLogin.equals(login)) {
                        registered = true;
                        break;
                    }
                }
                if (!registered)
                {
                    Map<String, Object> userData = new HashMap<>();
                    Map<String, Object> testResults = new HashMap<>();
                    testResults.put("placeholder", "");
                    userData.put("password", inputPassword);
                    userData.put("test_results", testResults);

                    usersRef.child(inputLogin).setValue(userData)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    User user = new User(inputLogin, inputPassword);
                                    Intent intent = new Intent(RegformActivity.this, MainActivity.class);
                                    intent.putExtra("user", user);
                                    Toast.makeText(RegformActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Toast.makeText(RegformActivity.this, "Ошибка регистрации " + inputLogin, Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(RegformActivity.this, "Пользователь уже зарегистрирован", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegformActivity.this, "Ошибка чтении данных", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void LoginForm_slide() {
        TextView authorize = findViewById(R.id.authorize);
        authorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegformActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });
    }
}
