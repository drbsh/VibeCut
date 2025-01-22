package com.example.vibecut;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends BaseThemes {
    private RecyclerView listFavouritesProjectsView;
    private ProjectAdapter projectAdapter;
    private List<ProjectInfo> projectList;
    private List<ProjectInfo> projectFavouritesList;
    private LinearLayout mainLayout1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourites_activity);

        // Получаем состояние темы из SharedPreferences
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkTheme = preferences.getBoolean("isDarkTheme", false); // Инициализация переменной

        // Инициализация списка избранных проектов
        projectFavouritesList = new ArrayList<>();

        Intent i = getIntent();
        projectList = (List<ProjectInfo>) i.getSerializableExtra("projectList");
        if (projectList != null) {
            for (ProjectInfo pr : projectList) {
                if (pr.getFavourite()) {
                    projectFavouritesList.add(pr);
                }
            }
        }

        listFavouritesProjectsView = findViewById(R.id.listFavouritesProjectsView);
        projectAdapter = new ProjectAdapter(this, projectFavouritesList, isDarkTheme);
        listFavouritesProjectsView.setAdapter(projectAdapter);
        listFavouritesProjectsView.setLayoutManager(new LinearLayoutManager(this));

        mainLayout1 = findViewById(R.id.mainLayout1); // Убедитесь, что у вас есть LinearLayout с этим ID в favourites_activity.xml

        // Передаем состояние темы в метод updateTheme
        updateTheme(isDarkTheme);
    }

    private void updateTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            mainLayout1.setBackgroundResource(R.drawable.gradient_black);
            // Установите цвет хедера для темной темы
            findViewById(R.id.headerLayout).setBackgroundColor(getResources().getColor(R.color.black2));
            // Измените цвет текста на backgroundHeaderMain
            ((TextView) findViewById(R.id.titleTextView)).setTextColor(getResources().getColor(R.color.backgroundHeaderMain));
        } else {
            mainLayout1.setBackgroundResource(R.drawable.gradient_main);
            // Установите цвет хедера для светлой темы
            findViewById(R.id.headerLayout).setBackgroundColor(getResources().getColor(R.color.backgroundHeaderMain));
            // Измените цвет текста на черный
            ((TextView) findViewById(R.id.titleTextView)).setTextColor(getResources().getColor(R.color.black));
        }
    }

    public void backClick(View view) {
        finish();
    }
}
