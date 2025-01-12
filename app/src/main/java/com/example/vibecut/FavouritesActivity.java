package com.example.vibecut;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {
    private RecyclerView listFavouritesProjectsView;
    private ProjectAdapter projectAdapter;
    private List<ProjectInfo> projectList;
    private List<ProjectInfo> projectFavouritesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourites_activity);

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
        projectAdapter = new ProjectAdapter(this, projectFavouritesList);
        listFavouritesProjectsView.setAdapter(projectAdapter);
        listFavouritesProjectsView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void backClick(View view) {
        finish();
    }
}

