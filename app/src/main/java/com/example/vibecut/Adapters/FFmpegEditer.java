package com.example.vibecut.Adapters;

import static com.example.vibecut.Adapters.CountTimeAndWidth.formatDurationToString;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.example.vibecut.Models.MediaFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FFmpegEditer {
    private MediaFile mediaFile;
    private long previousDurationOfVideoFromPhoto = 3000;
    private String stringUriEditedFile;
    private String stringOriginalPathToFile;

    public FFmpegEditer(MediaFile mediaFile){
        this.mediaFile = mediaFile;
        stringUriEditedFile = mediaFile.getPathToEditedFile().getPath();
        stringOriginalPathToFile = mediaFile.getPathToOriginalFile().getPath();
    }
    public void ChangeLengthByBorders(Duration difDurationLeft, Duration difDurationRight) {
        if (!new File(stringUriEditedFile).exists()) {
            Log.e("FFmpeg", "File not found: " + stringUriEditedFile);
            return;
        }
        if (mediaFile.getTypeMedia().equals("img")) {
            exchangeSizeVideoFromFoto();
        } else {
            // Если это видео, растягиваем его с указанной стороны
            difDurationRight = CountTimeAndWidth.TimeByWidthChanged(Math.abs(CountTimeAndWidth.WidthByTimeChanged(difDurationRight)));
            difDurationLeft = CountTimeAndWidth.TimeByWidthChanged(Math.abs(CountTimeAndWidth.WidthByTimeChanged(difDurationLeft)));

            difDurationRight = CountTimeAndWidth.subtractDurations(mediaFile.getMaxDuration(), difDurationRight);
            extendRegularVideo(formatDurationToString(difDurationLeft), formatDurationToString(difDurationRight));
        }
        returnTimeVideo(stringUriEditedFile);
    }
    private void extendRegularVideo(String millisecondsToExtendLeft, String millisecondsToExtendRight, String pathTempFileToAppendRemains, FFmpegCallback callback) {
        String command = String.format("-y -ss %s -i %s -ss 0 -c copy -to %s -avoid_negative_ts make_zero %s",
                millisecondsToExtendLeft, stringOriginalPathToFile, millisecondsToExtendRight, pathTempFileToAppendRemains);

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
    private void extendRegularVideo(String millisecondsToExtendLeft, String millisecondsToExtendRight) {
        // Команда для FFmpeg: растягиваем видео с указанной стороны
        String command;
        command = String.format("-y -ss %s -i %s -ss 0 -c copy -to %s -avoid_negative_ts make_zero %s",
                millisecondsToExtendLeft, stringOriginalPathToFile, millisecondsToExtendRight, stringUriEditedFile);

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.i("FFmpeg", "Video extended successfully: " + stringUriEditedFile);
            }

        });
    }

    private void exchangeSizeVideoFromFoto()  {
        long durationInMillis =  mediaFile.getDuration().toMillis();
        if(durationInMillis > previousDurationOfVideoFromPhoto) {
            concatenateVideos(durationInMillis);
        }
        else{

        }
    }
    private void concatenateVideos(long durationInMillis){
        String pathToTempFile = stringUriEditedFile.substring(0, stringUriEditedFile.lastIndexOf(".")) + "_TEMP.mp4";
        String pathTempFileToAppendRemains = stringUriEditedFile.substring(0, stringUriEditedFile.lastIndexOf(".")) + "_TEMP_REMAINS.mp4";
        File tempFile = new File(pathToTempFile);
        File tempFileToAppendRemains = new File(pathToTempFile);
        try{
            if(!tempFile.exists()) {
                tempFile.createNewFile();
            }
            if(!tempFileToAppendRemains.exists()) {
                tempFileToAppendRemains.createNewFile();
            }
        } catch (IOException e) {
            Log.e("Error Create File", "Error create" + pathToTempFile);
        }


        long i;
        long difDurations = durationInMillis - previousDurationOfVideoFromPhoto;
        for (i = 3000; i < difDurations; i += 3000) {
            concatenateVideosCommand(stringUriEditedFile, stringOriginalPathToFile, pathToTempFile);
            try {
                FillingMediaFile.copyFile(new File(pathToTempFile), new File(stringUriEditedFile));
            } catch (IOException e) {
                Log.e("CopyFileError", "Error Copying File: " + e);
            }
        }
        if (i - difDurations != 0) {
            String left = formatDurationToString(Duration.ofMillis(0));
            String right = formatDurationToString(Duration.ofMillis((difDurations % 3000)));

            extendRegularVideo(left, right, pathTempFileToAppendRemains, () -> {
                // Обрезка завершена, начинаем склейку
                concatenateVideosCommand(stringUriEditedFile, pathTempFileToAppendRemains, pathToTempFile);
                try {
                    FillingMediaFile.copyFile(new File(pathToTempFile), new File(stringUriEditedFile));
                } catch (IOException e) {
                    Log.e("CopyFileError", "Error Copying File: " + e);
                }

            });

        }

        if (tempFile.exists()) {
            if (!tempFile.delete()) {
                Log.e(pathToTempFile, "Failed to delete existing output file.");
            }
        }
        if (tempFileToAppendRemains.exists()) {
            if (!tempFileToAppendRemains.delete()) {
                Log.e(pathToTempFile, "Failed to delete existing output file.");
            }
        }
        previousDurationOfVideoFromPhoto = durationInMillis;
    }
    private void concatenateVideosCommand(String inputVideo1, String inputVideo2, String outputVideo){
        List<String> inputPaths = new ArrayList<>();
        inputPaths.add(inputVideo1);
        inputPaths.add(inputVideo2);
        try {
            MediaCodecConcatenator.concatenateVideos(inputPaths, outputVideo);
        } catch (IOException e) {
            Log.e("Concatenate Error", "Error:" + e);
        }

    }
//    private void concatenateVideosCommand(String inputVideo1, String inputVideo2, String outputVideo) {
//        // Шаг 0: Создаем временные пути для оптимизированных видео
//        String optimizedInput1 = inputVideo1.substring(0, inputVideo1.lastIndexOf(".")) + "_optimized.mp4";
//        String optimizedInput2 = inputVideo2.substring(0, inputVideo2.lastIndexOf(".")) + "_optimized.mp4";
//        try {
//            new File(optimizedInput1).createNewFile();
//            new File(optimizedInput2).createNewFile();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Шаг 1: Оптимизируем первое видео (movflags faststart)
//        String optimizeCommand1 = String.format("-i %s -c:v libx264 -c:a aac -movflags faststart %s", inputVideo1, optimizedInput1);
//        FFmpegKit.executeAsync(optimizeCommand1, session1 -> {
//            if (ReturnCode.isSuccess(session1.getReturnCode())) {
//                Log.i("FFmpeg", "Первое видео успешно оптимизировано.");
//
//                // Шаг 2: Оптимизируем второе видео (movflags faststart)
//                String optimizeCommand2 = String.format("-i %s -c:v libx264 -c:a aac -movflags faststart %s", inputVideo2, optimizedInput2);
//                FFmpegKit.executeAsync(optimizeCommand2, session2 -> {
//                    if (ReturnCode.isSuccess(session2.getReturnCode())) {
//                        Log.i("FFmpeg", "Второе видео успешно оптимизировано.");
//
//                        // Шаг 3: Создаем список файлов для конкатенации
//                        String[] inputVideoPaths = new String[2];
//                        inputVideoPaths[0] = optimizedInput1;
//                        inputVideoPaths[1] = optimizedInput2;
//
//                        String concatListPath = optimizedInput1.substring(0, optimizedInput1.lastIndexOf(".")) + ".txt";
//                        File concatFile = new File(concatListPath);
//
//                        try {
//                            concatFile.createNewFile();
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(concatFile))) {
//                            for (String videoPath : inputVideoPaths) {
//                                writer.write("file '" + videoPath + "'");
//                                writer.newLine();
//                            }
//                            Log.i("FFmpeg", "Concat list file created: " + concatListPath);
//                        } catch (IOException e) {
//                            Log.e("FFmpeg", "Failed to create concat list file: " + e.getMessage());
//                            throw new RuntimeException(e);
//                        }
//
//                        // Шаг 4: Склеиваем видео с помощью FFmpeg
//                        String concatCommand = String.format("-f concat -safe 0 -i %s -c copy -f mp4 %s", concatListPath, outputVideo);
//                        FFmpegKit.executeAsync(concatCommand, session3 -> {
//                            if (ReturnCode.isSuccess(session3.getReturnCode())) {
//                                Log.i("FFmpeg", "Видео успешно склеены.");
//
//                                // Шаг 5: Удаляем временные файлы
//                                new File(optimizedInput1).delete();
//                                new File(optimizedInput2).delete();
//                                new File(concatListPath).delete();
//
//                                System.out.println("Процесс завершен! Видео готово.");
//                            } else {
//                                System.err.println("Ошибка при склейке видео: " + session3.getFailStackTrace());
//                            }
//                        });
//                    } else {
//                        Log.e("FFmpeg", "Ошибка при оптимизации второго видео: " + session2.getFailStackTrace());
//                    }
//                });
//            } else {
//                Log.e("FFmpeg", "Ошибка при оптимизации первого видео: " + session1.getFailStackTrace());
//                Log.e("FFmpeg", "Логи FFmpeg: " + session1.getLogsAsString());
//            }
//        });
//    }

    private void reduceVideoFromFotoSize(){

    }


    private void returnTimeVideo(String uriFile) {
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
