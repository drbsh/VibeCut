package com.example.vibecut;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class JSONHelper {

    private static final String DIRECTORY_NAME = "VibeCutProjects";

    static boolean exportToJSON(Context context, ProjectInfo projectInfo) {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Uri.class, new UriAdapter())
                .create();
        String jsonString = gson.toJson(projectInfo);

        File directory = new File(context.getFilesDir(), DIRECTORY_NAME + "/" + projectInfo.getIdProj());
        File file = new File(directory, projectInfo.getIdProj() + ".json");

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(jsonString.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    static ProjectInfo importFromJSON(Context context, String filePath) {
        File file = new File(filePath);

        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader streamReader = new InputStreamReader(fileInputStream)) {

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(Uri.class, new UriAdapter())
                    .create();
            return gson.fromJson(streamReader, ProjectInfo.class);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static class ProjectItems {
        private List<ProjectInfo> projects;

        List<ProjectInfo> getProjects() {
            return projects;
        }
        void setProjects(List<ProjectInfo> projects) {
            this.projects = projects;
        }
    }
}
