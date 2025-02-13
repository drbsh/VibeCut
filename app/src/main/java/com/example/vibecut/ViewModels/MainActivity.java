    package com.example.vibecut.ViewModels;

    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.graphics.PorterDuff;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.ImageButton;
    import android.widget.TextView;
    import android.widget.Toast;

    import java.io.File;

    import androidx.activity.EdgeToEdge;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.localbroadcastmanager.content.LocalBroadcastManager;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import java.io.Serializable;
    import java.util.ArrayList;
    import java.util.List;
    import android.content.SharedPreferences;
    import android.widget.LinearLayout;

    import com.example.vibecut.CustomizeProject.BaseThemes;
    import com.example.vibecut.JSONHelper;
    import com.example.vibecut.Adapters.ProjectAdapter;
    import com.example.vibecut.Models.ProjectInfo;
    import com.example.vibecut.R;


    public class MainActivity extends BaseThemes implements ProjectDialog.ProjectDialogListener {
        private RecyclerView listProjectsView;
        private ProjectAdapter projectAdapter;
        private static List<ProjectInfo> projectList;
        private File projectFolder;
        private LinearLayout mainLayout;
        private boolean isDarkTheme;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);

            mainLayout = findViewById(R.id.mainLayout); // Убедитесь, что у вас есть LinearLayout с этим ID в activity_main.xml

            // Инициализация списка проектов и адаптера
            listProjectsView = findViewById(R.id.listProjectsView);
            projectList = new ArrayList<>();
            projectAdapter = new ProjectAdapter(this, projectList,  isDarkTheme);
            listProjectsView.setAdapter(projectAdapter);
            listProjectsView.setLayoutManager(new LinearLayoutManager(this));

            // Загрузка сохраненного состояния темы
            SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
            isDarkTheme = preferences.getBoolean("isDarkTheme", false);
            updateTheme(isDarkTheme); // Теперь projectAdapter уже инициализирован

            createProjectFolder();
            updateProjectList();
        }

        private BroadcastReceiver themeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isDarkTheme = intent.getBooleanExtra("isDarkTheme", false);
                updateTheme(isDarkTheme);
                projectAdapter.setDarkTheme(isDarkTheme); // Обновите адаптер
                projectAdapter.notifyDataSetChanged(); // Убедитесь, что адаптер обновляет отображение
            }
        };

        @Override
        protected void onStart() {
            super.onStart();
            // Регистрация приемника
            LocalBroadcastManager.getInstance(this).registerReceiver(themeChangeReceiver, new IntentFilter("themeChanged"));
        }

        private void updateTheme(boolean isDarkTheme) {
            this.isDarkTheme = isDarkTheme;
            if (isDarkTheme) {
                mainLayout.setBackgroundResource(R.drawable.gradient_black);
                findViewById(R.id.headerLayout).setBackgroundColor(getResources().getColor(R.color.black2));
                ((TextView) findViewById(R.id.titleTextView)).setTextColor(getResources().getColor(R.color.backgroundHeaderMain));

                // Измените tint для кнопок на backgroundHeaderMain
                ImageButton favouritesButton = findViewById(R.id.button_favourites);
                ImageButton settingsButton = findViewById(R.id.button_settings);
                favouritesButton.setColorFilter(getResources().getColor(R.color.backgroundHeaderMain), PorterDuff.Mode.SRC_IN);
                settingsButton.setColorFilter(getResources().getColor(R.color.backgroundHeaderMain), PorterDuff.Mode.SRC_IN);
            } else {
                mainLayout.setBackgroundResource(R.drawable.gradient_main);
                findViewById(R.id.headerLayout).setBackgroundColor(getResources().getColor(R.color.backgroundHeaderMain));
                ((TextView) findViewById(R.id.titleTextView)).setTextColor(getResources().getColor(R.color.black));

                // Измените tint для кнопок на черный
                ImageButton favouritesButton = findViewById(R.id.button_favourites);
                ImageButton settingsButton = findViewById(R.id.button_settings);
                favouritesButton.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
                settingsButton.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            }

            listProjectsView = findViewById(R.id.listProjectsView);
            projectList = new ArrayList<>();

            createProjectFolder();
            updateProjectList();


            projectAdapter = new ProjectAdapter(this, projectList, isDarkTheme);
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
            // Проверка состояния темы и обновление, если необходимо
            SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
            boolean isDarkTheme = preferences.getBoolean("isDarkTheme", false);
            setTheme(isDarkTheme ? R.style.DarkBackground : R.style.LightBackground);
        }

        //создание папок для проектов
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
            ProjectDialog dialog = new ProjectDialog(projectFolder, newProjectInfo, isDarkTheme);
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