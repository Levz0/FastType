package com.example.fasttype;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    public User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createUser();
        createWidget();
    }
    private void createUser(){
        user = (User)getIntent().getSerializableExtra("user");

        if (user != null) {
            // Используем данные пользователя
            Toast.makeText(MainActivity.this, "Добро пожаловать, " + user.getLogin(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Ошибка при получении данных пользователя.", Toast.LENGTH_SHORT).show();
        }

        Log.d("UserDetails", user.toString());
    }

    private void createWidget() {
        ViewPager2 viewPager2 = findViewById(R.id.viewPager2);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);
        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tabLayout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.type);
                    break;
                case 1:
                    tab.setIcon(R.drawable.record);
                    break;
                case 2:
                    tab.setIcon(R.drawable.profile);
                    break;

            }
        });
        tabLayoutMediator.attach();
    }
}