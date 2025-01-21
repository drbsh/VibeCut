package com.example.vibecut;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class SettingsActivity extends BaseThemes {

    private LinearLayout mainLayout;
    private SwitchCompat changeThemeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        mainLayout = findViewById(R.id.main1);
        changeThemeSwitch = findViewById(R.id.change_theme);

        // Загрузка сохраненного состояния темы
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkTheme = preferences.getBoolean("isDarkTheme", false);
        changeThemeSwitch.setChecked(isDarkTheme);
        updateTheme(isDarkTheme);

        // Установка слушателя для переключателя
        changeThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Сохранение состояния темы
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isDarkTheme", isChecked);
            editor.apply();

            // Обновление темы
            updateTheme(isChecked);
            Intent intent = new Intent("themeChanged");
            intent.putExtra("isDarkTheme", isChecked);
            LocalBroadcastManager.getInstance(SettingsActivity.this).sendBroadcast(intent);

        });
    }

    private void updateTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            mainLayout.setBackgroundResource(R.drawable.gradient_black);
            // Установите цвет хедера для темной темы
            findViewById(R.id.headerLayout).setBackgroundColor(getResources().getColor(R.color.black2));
            // Измените цвет текста на backgroundHeaderMain
            ((TextView) findViewById(R.id.titleTextView)).setTextColor(getResources().getColor(R.color.backgroundHeaderMain));
        } else {
            mainLayout.setBackgroundResource(R.drawable.gradient_main);
            // Установите цвет хедера для светлой темы
            findViewById(R.id.headerLayout).setBackgroundColor(getResources().getColor(R.color.backgroundHeaderMain));
            // Измените цвет текста на черный
            ((TextView) findViewById(R.id.titleTextView)).setTextColor(getResources().getColor(R.color.black));
        }
    }
    // Метод для обработки нажатия кнопки "Назад"
    public void backClick1(View view) {
        finish();
    }
}
