package com.example.vibecut.ViewModels;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.vibecut.CustomizeProject.BaseThemes;
import com.example.vibecut.R;


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
            mainLayout.setBackgroundResource(R.drawable.gradient_black); // Установите фон для темной темы
            findViewById(R.id.headerLayout).setBackgroundColor(getResources().getColor(R.color.black2)); // Цвет хедера для темной темы
            ((TextView) findViewById(R.id.titleTextView)).setTextColor(getResources().getColor(R.color.backgroundHeaderMain)); // Цвет текста для темной темы
            ((TextView) findViewById(R.id.black_theme)).setTextColor(getResources().getColor(R.color.white));
            // Установите фон для блока настроек
            findViewById(R.id.settings_switch).setBackgroundResource(R.drawable.rounded_button_background_dark);
        } else {
            mainLayout.setBackgroundResource(R.drawable.gradient_main); // Установите фон для светлой темы
            findViewById(R.id.headerLayout).setBackgroundColor(getResources().getColor(R.color.backgroundHeaderMain)); // Цвет хедера для светлой темы
            ((TextView) findViewById(R.id.titleTextView)).setTextColor(getResources().getColor(R.color.black)); // Цвет текста для светлой темы
            ((TextView) findViewById(R.id.black_theme)).setTextColor(getResources().getColor(R.color.black));
            // Установите фон для блока настроек
            findViewById(R.id.settings_switch).setBackgroundResource(R.drawable.rounded_button_background);
        }
    }
    // Метод для обработки нажатия кнопки "Назад"
    public void backClick1(View view) {
        finish();
    }
}
