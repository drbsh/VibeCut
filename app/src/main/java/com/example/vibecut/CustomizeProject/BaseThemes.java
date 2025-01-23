package com.example.vibecut.CustomizeProject;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vibecut.R;

public class BaseThemes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Получаем состояние темы из SharedPreferences
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkTheme = preferences.getBoolean("isDarkTheme", false);

        // Устанавливаем тему в зависимости от сохраненного состояния
        if (isDarkTheme) {
            setTheme(R.style.DarkBackground);
        } else {
            setTheme(R.style.LightBackground);
        }

        super.onCreate(savedInstanceState);
    }
}

