package com.example.vibecut.Adapters;

import static com.example.vibecut.Adapters.CountTimeAndWidth.formatDurationToString;

import android.net.Uri;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFprobeKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.example.vibecut.Models.MediaFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
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
                returnTimeVideo(stringUriEditedFile);
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
        for (i = 0; i < durationInMillis; i += 3000) {
            concatenateVideosCommand(stringUriEditedFile, stringOriginalPathToFile, pathToTempFile);
            try {
                FillingMediaFile.copyFile(new File(pathToTempFile), new File(stringUriEditedFile));
            } catch (IOException e) {
                Log.e("CopyFileError", "Error Copying File: " + e);
            }
        }
        if (i - durationInMillis != 0) {
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
        String command = String.format("-i %s -i %s -filter_complex \"[0:v][0:a][1:v][1:a]concat=n=2:v=1:a=1[v][a]\" -map \"[v]\" -map \"[a]\" %s",
                inputVideo1, inputVideo2, outputVideo);

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                // Видео успешно склеены
                System.out.println("Видео успешно склеены!");
            } else {
                // Ошибка при склейке видео
                System.err.println("Ошибка при склейке видео: " + session.getFailStackTrace());
            }
        });
    }

    private void reduceVideoFromFotoSize(){

    }


    private void returnTimeVideo(String uriFile) {
        // Команда для ffprobe: извлекаем длительность видео
        String command = String.format("-i %s -f null -", uriFile);

        FFmpegKit.executeAsync(command, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                // Видео успешно склеены
                Log.d("FFmpeg", "Длина получена!: "+ session.toString());
            }
            else{
                Log.e("FFmpeg", "Failed to retrieve video duration: " + session.getFailStackTrace());
            }
        });
    }


}
