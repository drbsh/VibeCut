package com.example.vibecut.Adapters;

import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.example.vibecut.Adapters.WorkWithVideo.MediaCodecConverter;
import com.example.vibecut.JSONHelper;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.Models.ProjectInfo;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class FillingMediaFile
{
    private final Context context;
    private List<MediaFile> MediaFiles;
    private MediaFile mediaFile;
    private MediaLineAdapter adapter;
    private MediaAdapter mediaAdapter;
    private ProjectInfo projectInfo;
    private static final String folderImage = "images";
    private static final String folderVideo = "video";
    private static final String folderAudio = "audio";
    private static final String folderOriginals = "originals";

    public FillingMediaFile(Context context, MediaLineAdapter adapter, ProjectInfo projectInfo, List<MediaFile> MediaFiles){
        this.context = context;
        this.MediaFiles = MediaFiles;
        this.adapter = adapter;
        this.projectInfo = projectInfo;

    }
    public FillingMediaFile(Context context, ProjectInfo projectInfo, List<MediaFile> MediaFiles, MediaAdapter mediaAdapter){
        this.context = context;
        this.MediaFiles = MediaFiles;
        this.mediaAdapter = mediaAdapter;
        this.projectInfo = projectInfo;
    }

    public void processingFile(Uri selectedMediaUri) {
        processUri(selectedMediaUri);
    }
    public void processingFile(ClipData.Item item) {
        Uri selectedMediaUri = item.getUri(); // Получаем Uri из ClipData.Item
        processUri(selectedMediaUri);
    }

    private Uri startCopyFile(Uri selectedMediaUri, String type)
    {
        try {
            File sourceFile = new File(getFilePathFromUri(selectedMediaUri)); // Получаем путь к файлу из Uri
            String destDirectoryName = "VibeCutProjects/" +  projectInfo.getIdProj() + "/" + type; // Папка проекта
            return copyFileToDirectory(context, sourceFile, destDirectoryName); // Копируем файл
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Не удалось скопировать файл.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void processUri(Uri selectedMediaUri) {
        // Получаем имя файла из Uri
        int width = 0;
        String fileName = getFileName(selectedMediaUri);
        String mimeType = context.getContentResolver().getType(selectedMediaUri);
        Uri preview = getPreview(mimeType, selectedMediaUri);
        String typeMedia = "";
        Duration duration = Duration.ZERO, maxDuration = Duration.ZERO;
        Uri originalPathToFile = startCopyFile(selectedMediaUri, folderOriginals); // копирование исходника при создании файла
        if (mimeType.startsWith("image/")) {
            selectedMediaUri = startCopyFile(selectedMediaUri, folderImage);// можно подумать чтобы убрать (Refactor)
            typeMedia = "img";
            duration = Duration.ofSeconds(3);
            maxDuration = Duration.ofHours(3);
            width = CountTimeAndWidth.WidthByTimeChanged(duration);

            MediaCodecConverter mediaCodecConverter = new MediaCodecConverter();
            MediaCodecConverter.Paths paths;
            paths = mediaCodecConverter.convertImageToVideoMediaCodec(originalPathToFile, selectedMediaUri, 3);
            originalPathToFile = Uri.parse(paths.originPath);
            selectedMediaUri = Uri.parse(paths.outputPath);
        } else if (mimeType.startsWith("video/")){
            typeMedia = "video";
            selectedMediaUri = startCopyFile(selectedMediaUri, folderVideo);// можно подумать чтобы убрать (Refactor)
            try {
                duration = getVideoDuration(selectedMediaUri);// можно подумать чтобы заменить (Refactor)
                maxDuration = duration;
                width = CountTimeAndWidth.WidthByTimeChanged(duration);
            } catch (IOException e) {
                e.printStackTrace(); // Логируем ошибку
                Toast.makeText(context, "Не удалось получить длительность видео.", Toast.LENGTH_SHORT).show();
            }
        }

        if(adapter != null){
            addFileToCurrentProject(fileName, preview, selectedMediaUri, originalPathToFile, duration, typeMedia, width, maxDuration);
        }
        else{
            addFileToNewProject(fileName, preview, selectedMediaUri, originalPathToFile, duration, typeMedia, width, MediaFiles, maxDuration);
        }
    }
    private void addFileToCurrentProject(String fileName, Uri preview, Uri selectedMediaUri, Uri originalPathToFile, Duration duration, String typeMedia, int width, Duration maxDuration) {
        // Обновляем Uri на путь к скопированному файлу


        MediaFile mediaFile;
        mediaFile = new MediaFile(fileName, preview, selectedMediaUri, originalPathToFile, duration, typeMedia, width);

        mediaFile.setMaxDuration(maxDuration);
        MediaFiles.add(mediaFile);
        adapter.notifyItemInserted();
        JSONHelper.exportToJSON(context, projectInfo);
    }

    private void addFileToNewProject(String fileName, Uri preview, Uri selectedMediaUri, Uri originalPathToFile, Duration duration, String typeMedia, int width, List<MediaFile> mediaFiles, Duration maxDuration) {
        // Обновляем Uri на путь к скопированному файлу


        MediaFile mediaFile;
        mediaFile = new MediaFile(fileName, preview, selectedMediaUri, originalPathToFile, duration, typeMedia, width);


        mediaFile.setMaxDuration(maxDuration);
        projectInfo.addMediaFile(mediaFile);
        mediaFiles.add(mediaFile);
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
        File folder = new File(context.getFilesDir(), "VibeCutProjects/" + prName + "/previews");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Создаем файл для сохранения миниатюры
        File file = new File(folder, nameFile + ".png"); // Вы можете изменить имя файла по своему усмотрению

        try (FileOutputStream out = new FileOutputStream(file)) {
            img.compress(Bitmap.CompressFormat.PNG, 100, out); // Сохраняем изображение в формате PNG
        } catch (IOException e) {
            Log.e("Error compressing image: ", e.toString());
        }
        return Uri.fromFile(file);
    }

    //Метод для получения миниатюры видео
    private Uri getVideoThumbnail(Uri uri) throws IOException{
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        Bitmap bitmap = retriever.getFrameAtTime(0); // Получаем первый кадр
        retriever.release();
        return savePreviewToFolderProject(projectInfo.getIdProj(), bitmap, FilenameUtils.removeExtension(getFileName(uri)));
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

    //методы копирования
    public static Uri moveFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            throw new IOException("Source file does not exist: " + sourceFile.getAbsolutePath());
        }

        try (FileInputStream inStream = new FileInputStream(sourceFile);
             FileOutputStream outStream = new FileOutputStream(destFile);
             FileChannel inChannel = inStream.getChannel();
             FileChannel outChannel = outStream.getChannel()) {

            inChannel.transferTo(0, inChannel.size(), outChannel);
        }

        sourceFile.delete();
        return Uri.fromFile(destFile);
    }
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            throw new IOException("Source file does not exist: " + sourceFile.getAbsolutePath());
        }

        try (FileInputStream inStream = new FileInputStream(sourceFile);
             FileOutputStream outStream = new FileOutputStream(destFile);
             FileChannel inChannel = inStream.getChannel();
             FileChannel outChannel = outStream.getChannel()) {

            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    public static Uri copyFileToDirectory(Context context, File sourceFile, String destDirectoryName) throws IOException {
        // Создаем целевую директорию, если она не существует
        File destDir = new File(context.getFilesDir(), destDirectoryName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // Получаем имя файла и его расширение
        String fileName = sourceFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);

        // Создаем целевой файл
        File destFileMP4 = new File(destDir, baseName + "." + "mp4");
        File destFile = new File(destDir, fileName);

        // Проверяем, существует ли файл с таким именем
        int counter = 1;
        while (destFile.exists() || destFileMP4.exists()) {
            // Если файл существует, добавляем суффикс (например, "_1", "_2" и т.д.)
            String newFileNameMP4 = baseName + "_" + counter + "." + "mp4";
            String newFileName = baseName + "_" + counter + "." + extension;

            destFile = new File(destDir, newFileName);
            destFileMP4 = new File(destDir, newFileNameMP4);
            counter++;
        }

        // Копируем файл
        return moveFile(sourceFile, destFile);
    }
    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(columnIndex);
                    File file = new File(context.getCacheDir(), fileName);
                    try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                         FileOutputStream outputStream = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        filePath = file.getAbsolutePath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
            }
        } else if (Objects.equals(uri.getScheme(), "file")) {
            filePath = uri.getPath();
        }
        return filePath;
    }
}
