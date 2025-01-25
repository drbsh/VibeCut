package com.example.vibecut.ViewModels;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibecut.CustomizeProject.CustomLayoutManager;
import com.example.vibecut.CustomizeProject.CustomMediaLineLayout;
import com.example.vibecut.JSONHelper;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.Adapters.MediaLineAdapter;
import com.example.vibecut.Models.ProjectInfo;
import com.example.vibecut.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class EditerActivity extends AppCompatActivity implements TimePickerDialog.TimePickerDialogListener {
    private static final int PICK_MEDIA_REQUEST = 1;
    private TextView nameProjectTextView;
    private HorizontalScrollView horizontalScrollView;
    private ProjectInfo projectInfo;//текущий  проект
    private List<MediaFile> MediaFiles;
    private MediaLineAdapter adapter;
    public static CustomLayoutManager layoutManager;
    private LinearLayout mediaLineContainer;
    private LinearLayout audioLineContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editer_activity);

        horizontalScrollView = findViewById(R.id.scroll_all_media_line);


        mediaLineContainer = findViewById(R.id.media_line_container);
        audioLineContainer = findViewById(R.id.audio_line_container);



//        recyclerView = findViewById(R.id.recyclerViewVideoTimeline);

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
        layoutManager = new CustomLayoutManager();
        adapter = new MediaLineAdapter(mediaLineContainer, MediaFiles, layoutManager, this); // Создаем адаптер

        // <<<<<<<<<<||||||||||||||||||||||||||||||||||||||||>>>>>>>>
        //MediaLineAdapter adapter1 = new MediaLineAdapter(audioLineContainer, MediaFiles, layoutManager, this);
        // УБЕРИ ЭТУ СТРОКУ ЧТОБЫ СККРЫТЬ НИЖНИЙ РЯД


        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkTheme = preferences.getBoolean("isDarkTheme", false);
        updateTheme(isDarkTheme);
    }

    private void updateTheme(boolean isDarkTheme) {
        LinearLayout headerLayout = findViewById(R.id.headerLayout);
        TextView titleTextView = findViewById(R.id.titleTextView);
        ImageButton saveButton = findViewById(R.id.save_button); // ID кнопки "Сохранить"
        ImageButton exportButton = findViewById((R.id.export_button));

        if (isDarkTheme) {
            headerLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.black2));
            titleTextView.setTextColor(getResources().getColor(R.color.backgroundHeaderMain));
            saveButton.setImageResource(R.drawable.save_dark); // Устанавливаем темную иконку
            exportButton.setImageResource(R.drawable.export_dark); // Устанавливаем tint для кнопки "Экспорт"
        } else {
            headerLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundHeaderMain));
            titleTextView.setTextColor(getResources().getColor(R.color.black));
            saveButton.setImageResource(R.drawable.save); // Устанавливаем светлую иконку
            exportButton.setImageResource(R.drawable.export);
        }
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
        pickMedia();

    }
    private void pickMedia() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*"); // Выбор изображений и видео
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_MEDIA_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Если выбрано несколько файлов
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    ClipData.Item item = data.getClipData().getItemAt(i);
                    processingFile(item);
                }
            } else if (data.getData() != null) {
                // Если выбран только один файл
                processingFile(data.getData());
            }
        }
    }
    // Обработка каждого файла
    private void processingFile(ClipData.Item item) {
        Uri selectedMediaUri = item.getUri(); // Получаем Uri из ClipData.Item
        processUri(selectedMediaUri);
    }

    private void processingFile(Uri selectedMediaUri) {
        processUri(selectedMediaUri);
    }

    private void processUri(Uri selectedMediaUri) {
        // Получаем имя файла из Uri
        String fileName = getFileName(selectedMediaUri);
        String mimeType = getContentResolver().getType(selectedMediaUri);
        Uri preview = getPreview(mimeType, selectedMediaUri);
        String typeMedia = "";
        LocalTime duration = LocalTime.of(0,0, 0, 0);
        if (mimeType.startsWith("image/")) {
            typeMedia = "img";
            duration = LocalTime.of(0, 3);
        } else if (mimeType.startsWith("video/")){
            typeMedia = "video";
            try {
                duration = getVideoDuration(selectedMediaUri);
            } catch (IOException e) {
                e.printStackTrace(); // Логируем ошибку
                Toast.makeText(this, "Не удалось получить длительность видео.", Toast.LENGTH_SHORT).show();
            }
        }
        MediaFile mediaFile = new MediaFile(fileName, preview, selectedMediaUri, duration, typeMedia);
        // Добавляем MediaFile в проект
        MediaFiles.add(mediaFile);// Уведомляем адаптер об изменении данных
        adapter.notifyItemInserted(MediaFiles.size() - 1);
        JSONHelper.exportToJSON(this, projectInfo);
    }


    // Получение предпросмотра
    private Uri getPreview(String mimeType, Uri selectedMediaUri) {
        Uri preview = null;
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                preview = selectedMediaUri;
            } else if (mimeType.startsWith("video/")) {
                try {
                    preview = getVideoThumbnail(selectedMediaUri); // Метод для получения миниатюры видео
                } catch (IOException e) {
                    e.printStackTrace(); // Логируем ошибку
                    Toast.makeText(this, "Не удалось получить миниатюру видео.", Toast.LENGTH_SHORT).show();
                    return preview;
                }
            } else {
                Toast.makeText(this, "Выбранный файл не является изображением или видео.", Toast.LENGTH_SHORT).show();
                return preview;
            }
        }
        return preview;
    }

    // Метод для получения имени файла из Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) { // Проверяем, что индекс не -1
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            // Если имя не найдено, пробуем извлечь его из пути
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    //метод для сохранения миниатюры видео в папку проекта
    private Uri savePreviewToFolderProject(String prName, Bitmap img, String nameFile){
        File folder = new File(this.getFilesDir(), "VibeCutProjects/" + prName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Создаем файл для сохранения миниатюры
        File file = new File(folder, nameFile + ".png"); // Вы можете изменить имя файла по своему усмотрению

        try (FileOutputStream out = new FileOutputStream(file)) {
            img.compress(Bitmap.CompressFormat.PNG, 100, out); // Сохраняем изображение в формате PNG
        } catch (IOException e) {
            e.printStackTrace(); // Обработка ошибок
        }
        return Uri.fromFile(file);
    }

    //Метод для получения миниатюры видео
    private Uri getVideoThumbnail(Uri uri) throws IOException{
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, uri);
        Bitmap bitmap = retriever.getFrameAtTime(0); // Получаем первый кадр
        retriever.release();
        return savePreviewToFolderProject(projectInfo.getName(), bitmap, FilenameUtils.removeExtension(getFileName(uri)));
    }
    private LocalTime getVideoDuration(Uri videoUri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, videoUri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long durationInMillis = Long.parseLong(time);
        retriever.release();

        // Преобразуем длительность в LocalTime
        return LocalTime.ofNanoOfDay(durationInMillis * 1_000_000);
    }
    @Override
    public void onTimeSaved(int hours, int minutes, int seconds, int millis, MediaFile mediaFile) {
        LocalTime newDuration = LocalTime.of(hours, minutes, seconds, millis * 1000 * 1000);
        mediaFile.setDuration(newDuration);
        Toast.makeText(this, "Время изменено: " + mediaFile.getDuration(), Toast.LENGTH_SHORT).show();
    }
}
