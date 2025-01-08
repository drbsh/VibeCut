package com.example.vibecut;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        Intent i = getIntent();
        projectList = (List<ProjectInfo>) i.getSerializableExtra("projectList");
        listFavouritesProjectsView = findViewById(R.id.listFavouritesProjectsView);
        for (ProjectInfo pr: projectList) {
            if(pr.getFavourite()){
                projectFavouritesList.add(pr);
            }
        }
        projectAdapter = new ProjectAdapter(this, projectFavouritesList);
        listFavouritesProjectsView.setAdapter(projectAdapter);
        listFavouritesProjectsView.setLayoutManager(new LinearLayoutManager(this));

    }


    public void backClick(View view) {
        finish();
    }
}
