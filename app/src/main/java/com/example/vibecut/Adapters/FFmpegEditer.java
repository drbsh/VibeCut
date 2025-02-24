package com.example.vibecut.Adapters;

import static com.example.vibecut.Adapters.CountTimeAndWidth.formatDurationToString;

import android.net.Uri;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFprobeKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.example.vibecut.Models.MediaFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;

public class FFmpegEditer {
    private MediaFile mediaFile;
    private int previousDurationOfVideoFromFoto = 3000;
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
            MediaCodecConverter mediaCodecConverter = new MediaCodecConverter();
            String extension = stringOriginalPathToFile.substring(stringOriginalPathToFile.lastIndexOf(".")) ;
//            if(!extension.equals(".mp4")) {
//                stringUriEditedFile = mediaCodecConverter.convertImageToVideoMediaCodec(Uri.parse(stringOriginalPathToFile), Uri.parse(stringUriEditedFile), (float) (mediaFile.getDuration().toMillis() / 1000.0));
//            }else{
                exchangeSizeVideoFromFoto();
//            }
//                returnTimeVideo(stringOriginalPathToFile);
        } else {
            // Если это видео, растягиваем его с указанной стороны
            difDurationRight = CountTimeAndWidth.TimeByWidthChanged(Math.abs(CountTimeAndWidth.WidthByTimeChanged(difDurationRight)));
            difDurationLeft = CountTimeAndWidth.TimeByWidthChanged(Math.abs(CountTimeAndWidth.WidthByTimeChanged(difDurationLeft)));

            difDurationRight = CountTimeAndWidth.subtractDurations(mediaFile.getMaxDuration(), difDurationRight);
            extendRegularVideo(formatDurationToString(difDurationLeft), formatDurationToString(difDurationRight));
        }
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
        double durationInMillis =  mediaFile.getDuration().toMillis();
        if(durationInMillis > previousDurationOfVideoFromFoto) {
            concatenateVideos(durationInMillis);
        }
        else{

        }
    }
    private void concatenateVideos(double durationInMillis){
        String pathToTempFile = stringUriEditedFile.substring(0, stringUriEditedFile.lastIndexOf(".")) + "_TEMP.mp4";
        File tempFile = new File(pathToTempFile);
        try{
            if(!tempFile.exists()) {
                tempFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e("Error Create File", "Error create" + pathToTempFile);
        }

        double i;
        for (i = 0; i < durationInMillis - 3000; i += 3000) {
            concatenateVideosCommand(stringUriEditedFile, stringOriginalPathToFile, pathToTempFile);
            try {
                FillingMediaFile.copyFile(new File(pathToTempFile), new File(stringUriEditedFile));
            } catch (IOException e) {
                Log.e("CopyFileError", "Error Copying File: " + e);
            }
        }
        if (durationInMillis - i != 0) {
            concatenateVideosCommand(stringUriEditedFile, stringOriginalPathToFile, pathToTempFile);
            try {
                FillingMediaFile.copyFile(new File(pathToTempFile), new File(stringUriEditedFile));
            } catch (IOException e) {
                Log.e("CopyFileError", "Error Copying File: " + e);
            }
        }
        try {
            FillingMediaFile.copyFile(new File(pathToTempFile), new File(stringUriEditedFile));
        } catch (IOException e) {
            Log.e("CopyFileError", "Error Copying File: " + e);
        }
        if (tempFile.exists()) {
            if (!tempFile.delete()) {
                Log.e(pathToTempFile, "Failed to delete existing output file.");
            }
        }
    }
    private void concatenateVideosCommand(String inputVideo1, String inputVideo2, String outputVideo) {
        // Шаг 0: Создаем временные пути для оптимизированных видео
        String optimizedInput1 = inputVideo1.substring(0, inputVideo1.lastIndexOf(".")) + "_optimized.mp4";
        String optimizedInput2 = inputVideo2.substring(0, inputVideo2.lastIndexOf(".")) + "_optimized.mp4";
        try {
            new File(optimizedInput1).createNewFile();
            new File(optimizedInput2).createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Шаг 1: Оптимизируем первое видео (movflags faststart)
        String optimizeCommand1 = String.format("-i %s -c copy -movflags faststart %s", inputVideo1, optimizedInput1);
        FFmpegKit.executeAsync(optimizeCommand1, session1 -> {
            if (ReturnCode.isSuccess(session1.getReturnCode())) {
                Log.i("FFmpeg", "Первое видео успешно оптимизировано.");

                // Шаг 2: Оптимизируем второе видео (movflags faststart)
                String optimizeCommand2 = String.format("-i %s -c copy -movflags faststart %s", inputVideo2, optimizedInput2);
                FFmpegKit.executeAsync(optimizeCommand2, session2 -> {
                    if (ReturnCode.isSuccess(session2.getReturnCode())) {
                        Log.i("FFmpeg", "Второе видео успешно оптимизировано.");

                        // Шаг 3: Создаем список файлов для конкатенации
                        String[] inputVideoPaths = new String[2];
                        inputVideoPaths[0] = optimizedInput1;
                        inputVideoPaths[1] = optimizedInput2;

                        String concatListPath = optimizedInput1.substring(0, optimizedInput1.lastIndexOf(".")) + ".txt";
                        File concatFile = new File(concatListPath);

                        try {
                            concatFile.createNewFile();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(concatFile))) {
                            for (String videoPath : inputVideoPaths) {
                                writer.write("file '" + videoPath + "'");
                                writer.newLine();
                            }
                            Log.i("FFmpeg", "Concat list file created: " + concatListPath);
                        } catch (IOException e) {
                            Log.e("FFmpeg", "Failed to create concat list file: " + e.getMessage());
                            throw new RuntimeException(e);
                        }

                        // Шаг 4: Склеиваем видео с помощью FFmpeg
                        String concatCommand = String.format("-f concat -safe 0 -i %s -c copy -f mp4 %s", concatListPath, outputVideo);
                        FFmpegKit.executeAsync(concatCommand, session3 -> {
                            if (ReturnCode.isSuccess(session3.getReturnCode())) {
                                Log.i("FFmpeg", "Видео успешно склеены.");

                                // Шаг 5: Удаляем временные файлы
                                new File(optimizedInput1).delete();
                                new File(optimizedInput2).delete();
                                new File(concatListPath).delete();

                                System.out.println("Процесс завершен! Видео готово.");
                            } else {
                                System.err.println("Ошибка при склейке видео: " + session3.getFailStackTrace());
                            }
                        });
                    } else {
                        System.err.println("Ошибка при оптимизации второго видео: " + session2.getFailStackTrace());
                    }
                });
            } else {
                Log.e("FFmpeg", "Ошибка при оптимизации первого видео: " + session1.getFailStackTrace());
                Log.e("FFmpeg", "Логи FFmpeg: " + session1.getLogsAsString());
            }
        });
    }

    private void reduceVideoFromFotoSize(){

    }


    private void returnTimeVideo(String uriFile) {
        // Команда для ffprobe: извлекаем длительность видео
        String command = String.format("AtomicParsley %s -T ", uriFile);

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
