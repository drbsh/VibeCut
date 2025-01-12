package com.example.vibecut;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ProjectDialog.ProjectDialogListener {
    private RecyclerView listProjectsView;
    private ProjectAdapter projectAdapter;
    private static List<ProjectInfo> projectList;
    private File projectFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        createProjectFolder();

        listProjectsView = findViewById(R.id.listProjectsView);
        projectList = new ArrayList<>();

        updateProjectList();


        projectAdapter = new ProjectAdapter(this, projectList);
        listProjectsView.setAdapter(projectAdapter);
        listProjectsView.setLayoutManager(new LinearLayoutManager(this));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    //обновление списка при возврате с другой активности
    @Override
    protected void onResume() {
        super.onResume();
        updateProjectList();
        projectAdapter.notifyDataSetChanged();
    }

    //создание папки для проектов
    public void createProjectFolder() {
        // Создаем папку для проектов
        projectFolder = new File(getFilesDir(), "VibeCutProjects");

        // Проверяем, существует ли папка, если нет, создаем ее
        if (!projectFolder.exists()) {
            if (projectFolder.mkdirs()) {
                Toast.makeText(this, "Папка для проектов успешно создана.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка создания папки.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //переход на страницу избранного
    public void openFavouritesClick(View view) {
        if (projectList != null) {
            Intent intent = new Intent(this, FavouritesActivity.class);
            intent.putExtra("projectList", (Serializable) projectList);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Список проектов пуст.", Toast.LENGTH_SHORT).show();
        }
    }


    public void openSettingsClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void createNewProjectClick(View view) {
        //показываем dialog и пользователь заполняет название проекта/видео/и избранное по желанию
        ProjectInfo newProjectInfo = new ProjectInfo();
        ProjectDialog dialog = new ProjectDialog(projectFolder, newProjectInfo);
        dialog.show(getSupportFragmentManager(), "ProjectDialog");
    }


    @Override
    public void onProjectSaved(ProjectInfo projectInfo) {
        JSONHelper.exportToJSON(this, projectInfo);
        projectList.add(projectInfo); // Добавляем новый проект в список
        projectAdapter.notifyDataSetChanged(); // Уведомляем адаптер об изменениях
        Toast.makeText(this, "Проект сохранен: " + projectInfo.getName(), Toast.LENGTH_SHORT).show();
    }

    private void updateProjectList() {
        projectList.clear(); // Очищаем текущий список проектов
        searchFilesInDirectory(projectFolder); // Начинаем поиск файлов
    }

    private void searchFilesInDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Если это директория, рекурсивно ищем в ней
                    searchFilesInDirectory(file);
                } else {
                    String fileName = file.getName();
                    if (fileName.endsWith(".json")) { // Проверяем, что файл имеет расширение .json
                        ProjectInfo savedProject = JSONHelper.importFromJSON(this, file.getAbsolutePath()); // Передаем полный путь к файлу
                        if (savedProject != null) {
                            projectList.add(savedProject);
                        }
                    }
                }
            }
        }
    }

    public static List<ProjectInfo> getProjectList(){
        return projectList;
    }
}