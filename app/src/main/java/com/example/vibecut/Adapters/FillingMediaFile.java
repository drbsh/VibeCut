package com.example.vibecut.Adapters;

import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.example.vibecut.JSONHelper;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.Models.ProjectInfo;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class FillingMediaFile
{
    private final Context context;
    private List<MediaFile> MediaFiles;
    private MediaFile mediaFile;
    private MediaLineAdapter adapter;
    private MediaAdapter mediaAdapter;
    private ProjectInfo projectInfo;
    private CountTimeAndWidth countTimeAndWidth;
    public FillingMediaFile(Context context, MediaLineAdapter adapter, ProjectInfo projectInfo, List<MediaFile> MediaFiles){
        this.context = context;
        this.MediaFiles = MediaFiles;
        this.adapter = adapter;
        this.projectInfo = projectInfo;
        countTimeAndWidth = new CountTimeAndWidth(context);
    }
    public FillingMediaFile(Context context, ProjectInfo projectInfo, List<MediaFile> MediaFiles, MediaAdapter mediaAdapter){
        this.context = context;
        this.MediaFiles = MediaFiles;
        this.mediaAdapter = mediaAdapter;
        this.projectInfo = projectInfo;
        countTimeAndWidth = new CountTimeAndWidth(context);
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    public void processingFile(Uri selectedMediaUri) {
        processUri(selectedMediaUri);
    }
    public void processingFile(ClipData.Item item) {
        Uri selectedMediaUri = item.getUri(); // Получаем Uri из ClipData.Item
        processUri(selectedMediaUri);
    }

    private void processUri(Uri selectedMediaUri) {
        // Получаем имя файла из Uri
        int width = 0;
        String fileName = getFileName(selectedMediaUri);
        String mimeType = context.getContentResolver().getType(selectedMediaUri);
        Uri preview = getPreview(mimeType, selectedMediaUri);
        String typeMedia = "";
        Duration duration = Duration.ZERO, maxDuration = Duration.ZERO;
        if (mimeType.startsWith("image/")) {
            typeMedia = "img";
            duration = Duration.ofSeconds(3);
            maxDuration = Duration.ofHours(3);
            width = countTimeAndWidth.WidthByTimeChanged(duration);
        } else if (mimeType.startsWith("video/")){
            typeMedia = "video";
            try {
                duration = getVideoDuration(selectedMediaUri);
                maxDuration = duration;
                width = countTimeAndWidth.WidthByTimeChanged(duration);
            } catch (IOException e) {
                e.printStackTrace(); // Логируем ошибку
                Toast.makeText(context, "Не удалось получить длительность видео.", Toast.LENGTH_SHORT).show();
            }
        }
        if(adapter != null){
            addFileToCurrentProject(fileName, preview, selectedMediaUri, duration, typeMedia, width, maxDuration);
        }
        else{
            addFileToNewProject(fileName, preview, selectedMediaUri, duration, typeMedia, width, MediaFiles, maxDuration);
        }
    }
    private void addFileToCurrentProject(String fileName, Uri preview, Uri selectedMediaUri, Duration duration, String typeMedia, int width, Duration maxDuration){
        MediaFile mediaFile = new MediaFile(fileName, preview, selectedMediaUri, duration, typeMedia, width);
        mediaFile.setMaxDuration(maxDuration);
        // Добавляем MediaFile в проект
        MediaFiles.add(mediaFile);// Уведомляем адаптер об изменении данных
        adapter.notifyItemInserted();
        JSONHelper.exportToJSON(context, projectInfo);
    }
    private void addFileToNewProject(String fileName, Uri preview, Uri selectedMediaUri, Duration duration, String typeMedia, int width, List<MediaFile> mediaFiles, Duration maxDuration){
        MediaFile mediaFile = new MediaFile(fileName, preview, selectedMediaUri, duration, typeMedia, width);
        mediaFile.setMaxDuration(maxDuration);
        // Добавляем MediaFile в проект
        projectInfo.addMediaFile(mediaFile);
        // Добавляем MediaFile в список
        mediaFiles.add(mediaFile);
        // Уведомляем адаптер об изменении данных
        mediaAdapter.notifyItemInserted(mediaFiles.size() - 1);
    }
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
                    Toast.makeText(context, "Не удалось получить миниатюру видео.", Toast.LENGTH_SHORT).show();
                    return preview;
                }
            } else {
                Toast.makeText(context, "Выбранный файл не является изображением или видео.", Toast.LENGTH_SHORT).show();
                return preview;
            }
        }
        return preview;
    }
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
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
        File folder = new File(context.getFilesDir(), "VibeCutProjects/" + prName);
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
        retriever.setDataSource(context, uri);
        Bitmap bitmap = retriever.getFrameAtTime(0); // Получаем первый кадр
        retriever.release();
        return savePreviewToFolderProject(projectInfo.getName(), bitmap, FilenameUtils.removeExtension(getFileName(uri)));
    }
    private Duration getVideoDuration(Uri videoUri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationInMillis = Long.parseLong(time);

            return Duration.ofMillis(durationInMillis);
        } finally {
            retriever.release();
        }
    }
}
