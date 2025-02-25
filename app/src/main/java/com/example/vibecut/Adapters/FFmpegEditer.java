package com.example.vibecut.Adapters;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;

public class FFmpegEditer {
    String[] stringOriginalPathToFile;
    String[] stringUriEditedFile;

    public FFmpegEditer(String[] stringOriginalPathToFile, String[] stringUriEditedFile) {
        this.stringOriginalPathToFile = stringOriginalPathToFile;
        this.stringUriEditedFile = stringUriEditedFile;
    }

    public void extendRegularVideo(String millisecondsToExtendLeft, String millisecondsToExtendRight, String pathTempFileToAppendRemains, String fileToReduce, FFmpegCallback callback) {
        String command = String.format("-y -ss %s -i %s -ss 0 -c copy -to %s -avoid_negative_ts make_zero %s",
                millisecondsToExtendLeft, fileToReduce, millisecondsToExtendRight, pathTempFileToAppendRemains);

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.i("FFmpeg", "Video extended successfully: " + pathTempFileToAppendRemains);
                callback.onSuccess(); // Уведомляем об успешном завершении
            } else {
                String errorMessage = session.getFailStackTrace();
                Log.e("FFmpeg", "Error extending video: " + errorMessage);
            }
        });
    }
    void extendRegularVideo(String millisecondsToExtendLeft, String millisecondsToExtendRight) {
        // Команда для FFmpeg: растягиваем видео с указанной стороны
        String command;
        command = String.format("-y -ss %s -i %s -ss 0 -c copy -to %s -avoid_negative_ts make_zero %s",
                millisecondsToExtendLeft, stringOriginalPathToFile[0], millisecondsToExtendRight, stringUriEditedFile[0]);

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.i("FFmpeg", "Video extended successfully: " + stringUriEditedFile[0]);
            }

        });
    }
    void returnTimeVideo(String uriFile) {
        // Команда для ffprobe: извлекаем длительность видео
        String command = String.format("file %s", uriFile);

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.d("FFmpeg", "Длина получена!: "+ session.toString());
            }
            else{
                Log.e("FFmpeg", "Failed to retrieve video duration: " + session.getFailStackTrace());
            }
        });
    }
}
