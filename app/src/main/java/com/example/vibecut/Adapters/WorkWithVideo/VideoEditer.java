package com.example.vibecut.Adapters.WorkWithVideo;

import static com.example.vibecut.Adapters.CountTimeAndWidth.formatDurationToString;

import android.util.Log;

import com.example.vibecut.Adapters.CountTimeAndWidth;
import com.example.vibecut.Adapters.FillingMediaFile;
import com.example.vibecut.Models.MediaFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class VideoEditer {
    private MediaFile mediaFile;
    private long previousDurationOfVideoFromPhoto = 3000;
    private String stringUriEditedFile;
    private String stringOriginalPathToFile;
    FFmpegEditer fFmpegEditer;

    public VideoEditer(MediaFile mediaFile){
        this.mediaFile = mediaFile;
        stringUriEditedFile = mediaFile.getPathToEditedFile().getPath();
        stringOriginalPathToFile = mediaFile.getPathToOriginalFile().getPath();
        fFmpegEditer = new FFmpegEditer( new String[]{stringOriginalPathToFile}, new String[]{stringUriEditedFile});
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
            fFmpegEditer.extendRegularVideo(formatDurationToString(difDurationLeft), formatDurationToString(difDurationRight));
        }
        fFmpegEditer.returnTimeVideo(stringUriEditedFile);
    }



    private void exchangeSizeVideoFromFoto()  {
        long durationInMillis =  mediaFile.getDuration().toMillis();
        if(durationInMillis > previousDurationOfVideoFromPhoto) {
            concatenateVideos(durationInMillis);
        }
        else{
            reduceVideoFromFotoSize(durationInMillis);
        }
        previousDurationOfVideoFromPhoto = durationInMillis;
    }
    private void concatenateVideos(long durationInMillis){
        String pathToTempFile = stringUriEditedFile.substring(0, stringUriEditedFile.lastIndexOf(".")) + "_TEMP.mp4";
        String pathTempFileToAppendRemains = stringUriEditedFile.substring(0, stringUriEditedFile.lastIndexOf(".")) + "_TEMP_REMAINS.mp4";
        File tempFile = new File(pathToTempFile);
        File tempFileToAppendRemains = new File(pathTempFileToAppendRemains);

        long i;
        long difDurations = durationInMillis - previousDurationOfVideoFromPhoto;
        for (i = 3000; i < difDurations; i += 3000) {
            concatenateVideosCommand(stringUriEditedFile, stringOriginalPathToFile, pathToTempFile);
            try {
                FillingMediaFile.moveFile(new File(pathToTempFile), new File(stringUriEditedFile));
            } catch (IOException e) {
                Log.e("CopyFileError", "Error Copying File: " + e);
            }
        }
        if (i - difDurations != 0) {
            String left = formatDurationToString(Duration.ofMillis(0));
            String right = formatDurationToString(Duration.ofMillis((difDurations % 3000)));

            fFmpegEditer.extendRegularVideo(left, right, pathTempFileToAppendRemains, stringOriginalPathToFile, () -> {
                // Обрезка завершена, начинаем склейку
                concatenateVideosCommand(stringUriEditedFile, pathTempFileToAppendRemains, pathToTempFile);
                try {
                    FillingMediaFile.moveFile(new File(pathToTempFile), new File(stringUriEditedFile));
                } catch (IOException e) {
                    Log.e("CopyFileError", "Error Copying File: " + e);
                }
                tempFileToAppendRemains.delete();

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

    private void reduceVideoFromFotoSize(long durationInMillis){
        String pathToTempFile = stringUriEditedFile.substring(0, stringUriEditedFile.lastIndexOf(".")) + "_TEMP.mp4";
        File tempFile = new File(pathToTempFile);
        fFmpegEditer.extendRegularVideo(formatDurationToString(Duration.ofMillis(0)), formatDurationToString(Duration.ofMillis(durationInMillis)), pathToTempFile, stringUriEditedFile, () -> {
            try {
                FillingMediaFile.moveFile(new File(pathToTempFile), new File(stringUriEditedFile));
            } catch (IOException e) {
                Log.e("CopyFileError", "Error Copying File: " + e);
            }


        });
    }





}
