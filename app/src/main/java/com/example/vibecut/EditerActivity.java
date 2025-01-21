package com.example.vibecut;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EditerActivity extends AppCompatActivity {
    private static final int PICK_MEDIA_REQUEST = 1;
    private TextView nameProjectTextView;
    private RecyclerView recyclerView;
    private ProjectInfo projectInfo;//текущий  проект
    private List<MediaFile> MediaFiles;
    private MediaLineAdapter adapter;
    private CustomLayoutManager layoutManager;
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
        layoutManager = new CustomLayoutManager(MediaFiles.size());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MediaLineAdapter(this, MediaFiles);
        recyclerView.setAdapter(adapter);


    }

    public void backClick(View view) {
        finish();
    }

    public void showProjectFiles(View view) {
        PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        for (MediaFile file: MediaFiles) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView textView = new TextView(this);
            textView.setText(file.getNameFile());
            textView.setPadding(16, 8, 16, 8);
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

            ImageButton button = new ImageButton(this);
            button.setBackground(null);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.trash);
            button.setImageBitmap(bitmap);
            button.setLayoutParams(new LinearLayout.LayoutParams(135, 135));
            button.setScaleType(ImageView.ScaleType.FIT_CENTER);

            button.setOnClickListener(v -> {
                int index = MediaFiles.indexOf(file); // Находим индекс файла
                if (index != -1) {
                    MediaFiles.remove(index);

                    adapter.notifyItemRemoved(index); // Уведомляем адаптер об удалении
                    boolean success = JSONHelper.exportToJSON(this, projectInfo); // Сохраняем изменения
                    if (success) {
                        Toast.makeText(this, "Файл " + file.getNameFile() + " успешно удален из проекта", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка сохранения изменений!", Toast.LENGTH_SHORT).show();
                    }
                }
                popupWindow.dismiss();
            });

            rowLayout.addView(textView);
            rowLayout.addView(button);
            layout.addView(rowLayout);
        }

        popupWindow.setContentView(layout);
        popupWindow.setHeight(WRAP_CONTENT);
        popupWindow.setWidth(WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(view, 0, 0);
    }

    public void addProjectFiles(View view){

    }
}

