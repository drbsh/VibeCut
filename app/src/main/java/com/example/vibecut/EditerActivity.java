package com.example.vibecut;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class EditerActivity extends AppCompatActivity {
    private TextView nameProjectTextView;

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

    }

    public void backClick(View view) {
        finish();
    }
}
