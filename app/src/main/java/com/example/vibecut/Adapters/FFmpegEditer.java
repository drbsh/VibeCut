package com.example.vibecut.Adapters;

import static com.example.vibecut.Adapters.CountTimeAndWidth.formatDurationToString;

import android.net.Uri;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFprobeKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.example.vibecut.Models.MediaFile;

import java.io.File;
import java.time.Duration;
import java.util.Locale;

public class FFmpegEditer {
    private MediaFile mediaFile;
    private String stringUriEditedFile;
    private String stringOriginalPathToFile;

    public FFmpegEditer(MediaFile mediaFile){
        this.mediaFile = mediaFile;
        stringUriEditedFile = mediaFile.getPathToEditedFile().getPath();
        stringOriginalPathToFile = mediaFile.getPathToOriginalFile().getPath();
    }
    public static Uri FromImgToMp4(Uri pathToFile, Uri pathToOrigin){
        String stringPath = pathToFile.getPath();
        String outputPath = pathToOrigin.getPath();


        // Команда для FFmpeg
        String command = String.format("-loop 1 -i %s -c:v libx264 -preset fast -t %d -pix_fmt yuv420p -r 0.5 %s", stringPath, 3, outputPath);

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.i("FFmpeg", "Conversion successful: " + outputPath);
                // Возвращаем Uri для выходного файла
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
    public void ChangeLengthByBorders(boolean LeftOrRight, Duration difDurationLeft, Duration difDurationRight) {


        if (!new File(stringUriEditedFile).exists()) {
            Log.e("FFmpeg", "File not found: " + stringUriEditedFile);
            return;
        }
        if (mediaFile.getTypeMedia().equals("img")) {
            extendImageVideo(mediaFile.getDuration());
        } else {
            // Если это видео, растягиваем его с указанной стороны
            extendRegularVideo(formatDurationToString(difDurationLeft), formatDurationToString(difDurationRight));
        }
    }

    private void extendImageVideo(Duration newDuration) { // find the optimal command
        // Команда для FFmpeg: растягиваем видео, созданное из фото
        int framerate = (int)(newDuration.toMillis() / 1000.0 * 30);
        stringUriEditedFile = stringUriEditedFile.substring(0, stringUriEditedFile.lastIndexOf(".")) + ".mp4";

        // Форматируем значение framerate с точкой
        String command = String.format(Locale.US,
                "-y -loop 1 -i %s -c:v libx264 -preset ultrafast -t %.3f -r 25 -vf scale=720:-2 %s",
                stringOriginalPathToFile, newDuration.toMillis() / 1000.0, stringUriEditedFile);
//        ffmpeg.exe -f image2 -framerate 25 -pattern_type sequence -start_number 1234 -r 3 -i Imgp%04d.jpg -s 720x480 test.avi

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.i("FFmpeg", "Image video extended successfully: " + stringUriEditedFile);
            } else {
                Log.e("FFmpeg", "Image video extension failed: " + session.getFailStackTrace());
            }
        });
        String probeCommand = String.format(Locale.US,
                "-v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 %s",
                stringUriEditedFile);

        FFprobeKit.executeAsync(probeCommand, session -> {
            String output = session.getOutput();
            double duration = Double.parseDouble(output.trim());
            Log.d("Duration", "Video duration: " + duration + " seconds");
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
}
