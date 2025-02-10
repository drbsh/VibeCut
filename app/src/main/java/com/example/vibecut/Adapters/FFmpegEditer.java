package com.example.vibecut.Adapters;

import android.net.Uri;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.example.vibecut.Models.MediaFile;

import java.io.File;
import java.time.Duration;

public class FFmpegEditer {
    public Duration maxDuration;
    public String stringUriFile;
    public MediaFile mediaFile;
    public Duration differenceLeftBorderFromLeftSide;
    public Duration differenceRightBorderFromRightSide;
    public FFmpegEditer(MediaFile mediaFile){
        this.mediaFile = mediaFile;
        maxDuration = mediaFile.getMaxDuration();
        stringUriFile = mediaFile.getPathToFile().getPath();
    }
    public static Uri FromImgToMp4(Uri pathToFile){
        String stringPath = pathToFile.getPath();
        String outputPath = stringPath.substring(0, stringPath.lastIndexOf('.')) + ".mp4"; // Создаем имя выходного файла

        File outputFile = new File(outputPath);
        File outputDir = outputFile.getParentFile();
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Создаем директорию, если она не существует
        }

        // Команда для FFmpeg
        String command = String.format("-loop 1 -i %s -c:v libx264 -t %d -pix_fmt yuv420p -r 1 %s", stringPath, 3, outputPath);

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.i("FFmpeg", "Conversion successful: " + outputPath);
                // Возвращаем Uri для выходного файла
                Uri outputUri = Uri.fromFile(outputFile);
                // Здесь вы можете использовать outputUri по вашему усмотрению
                File file = new File(stringPath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        // Файл успешно удален
                        System.out.println("Файл успешно удален: " + pathToFile);
                    } else {
                        // Не удалось удалить файл
                        System.out.println("Не удалось удалить файл: " + pathToFile);
                    }
                } else {
                    // Файл не существует
                    System.out.println("Файл не существует: " + pathToFile);
                }
            } else {
                Log.e("FFmpeg", "Conversion failed: " + session.getFailStackTrace());
            }
        });


        return Uri.parse(outputPath);
    }
}
