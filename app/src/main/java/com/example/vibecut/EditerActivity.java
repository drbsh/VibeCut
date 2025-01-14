package com.example.vibecut;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EditerActivity extends AppCompatActivity {
    private TextView nameProjectTextView;
    private RecyclerView recyclerView;
    private ProjectInfo projectInfo;//текущий  проект
    private List<MediaFile> MediaFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editer_activity);

        if (getIntent() != null && getIntent().hasExtra("project_info")) {
            ProjectInfo projectInfo = (ProjectInfo) getIntent().getSerializableExtra("project_info");

            // Инициализируем TextView
            nameProjectTextView = findViewById(R.id.nameProject);

            // Устанавливаем имя проекта в TextView
            if (projectInfo != null) {
                nameProjectTextView.setText(projectInfo.getName());
            }
        }

        MediaFiles = projectInfo.getProjectFiles();

        if (MediaFiles != null && !MediaFiles.isEmpty()) {
            MediaLineAdapter adapter = new MediaLineAdapter(this, MediaFiles);
            recyclerView.setAdapter(adapter);
        } else {
            // Обработка случая, когда список пуст
            // Например, можно скрыть RecyclerView и показать текст "Нет медиафайлов"
            recyclerView.setVisibility(View.GONE);
            // Здесь можно добавить TextView для отображения сообщения
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager); // Смена ориентации на горизонтальную
    }

    public void backClick(View view) {
        finish();
    }
}
