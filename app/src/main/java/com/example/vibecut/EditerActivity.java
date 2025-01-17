package com.example.vibecut;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;

public class EditerActivity extends AppCompatActivity {
    private TextView nameProjectTextView;
    private RecyclerView recyclerView;
    private ProjectInfo projectInfo;//текущий  проект
    private List<MediaFile> MediaFiles;
    //private MediaLineAdapter adapter;
    private LinearLayout linearLayoutContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editer_activity);
        recyclerView = findViewById(R.id.recyclerViewTimeline);
        if (getIntent() != null && getIntent().hasExtra("project_info")) {
            projectInfo = (ProjectInfo) getIntent().getSerializableExtra("project_info");

            // Инициализируем TextView
            nameProjectTextView = findViewById(R.id.nameProject);

            // Устанавливаем имя проекта в TextView
            if (projectInfo != null) {
                nameProjectTextView.setText(projectInfo.getName());
            }
        }

        MediaFiles = projectInfo.getProjectFiles();
        CustomLayoutManager layoutManager = new CustomLayoutManager(MediaFiles.size());
        recyclerView.setLayoutManager(layoutManager);
        CustomLayoutManager.ItemTouchListener touchListener = new CustomLayoutManager.ItemTouchListener(layoutManager);
        recyclerView.addOnItemTouchListener(touchListener);
        MediaLineAdapter adapter = new MediaLineAdapter(this, MediaFiles);
        recyclerView.setAdapter(adapter);
    }

    public void backClick(View view) {
        finish();
    }
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
