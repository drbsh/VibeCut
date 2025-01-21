package com.example.vibecut;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class JSONHelper {

    private static final String DIRECTORY_NAME = "VibeCutProjects";
    private static final String TAG = "JSONHelper"; // Добавили тег для логов

    static boolean exportToJSON(Context context, ProjectInfo projectInfo) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Uri.class, new UriAdapter())
                .create();
        String jsonString = gson.toJson(projectInfo);

        File directory = new File(context.getFilesDir(), DIRECTORY_NAME + "/" + projectInfo.getIdProj());
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Не удалось создать директорию: " + directory.getAbsolutePath());
            return false;
        }
        File file = new File(directory, projectInfo.getIdProj() + ".json");

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
            writer.write(jsonString);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Ошибка записи в файл: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    static ProjectInfo importFromJSON(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "Файл не существует: " + filePath);
            return null;
        }

        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader streamReader = new InputStreamReader(fileInputStream, "UTF-8")) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(Uri.class, new UriAdapter())
                    .create();
            return gson.fromJson(streamReader, ProjectInfo.class);
        } catch (IOException e) {
            Log.e(TAG, "Ошибка чтения файла: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
