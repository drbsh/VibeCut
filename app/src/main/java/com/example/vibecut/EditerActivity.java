package com.example.vibecut;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;


public class EditerActivity extends AppCompatActivity {
    private TextView nameProjectTextView;
    private RecyclerView recyclerViewTimeline;
    private TimelineAdapter timelineAdapter;
    private ProjectInfo projectInfo;
    private List<MediaFile> mediaFiles; // Список медиафайлов
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editer_activity);

        if (getIntent() != null && getIntent().hasExtra("project_info")) {
            projectInfo = (ProjectInfo) getIntent().getSerializableExtra("project_info");

            // Инициализируем TextView
            nameProjectTextView = findViewById(R.id.nameProject);

            // Устанавливаем имя проекта в TextView
            if (projectInfo != null) {
                nameProjectTextView.setText(projectInfo.getName());
            }
        }
        // Инициализация RecyclerView
        recyclerViewTimeline = findViewById(R.id.recyclerViewTimeline);

        // Инициализация списка медиафайлов
        mediaFiles = new ArrayList<>();
        mediaFiles.addAll(projectInfo.getProjectFiles());
        // Инициализация адаптера
        timelineAdapter = new TimelineAdapter(this, mediaFiles);

        // Установка адаптера для RecyclerView
        recyclerViewTimeline.setAdapter(timelineAdapter);

        recyclerViewTimeline.setLayoutManager(new LinearLayoutManager(this));
    }

    public void backClick(View view) {
        finish();
    }

}
