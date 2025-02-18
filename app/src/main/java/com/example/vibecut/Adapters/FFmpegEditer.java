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
            stringUriEditedFile = mediaCodecConverter.convertImageToVideoMediaCodec(Uri.parse(stringOriginalPathToFile), Uri.parse(stringUriEditedFile), (float) (mediaFile.getDuration().toMillis()/1000.0));
            returnTimeVideo(stringUriEditedFile);
        } else {
            // Если это видео, растягиваем его с указанной стороны
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

    private void returnTimeVideo(String uriFile) {
        // Команда для ffprobe: извлекаем длительность видео
        String command = String.format("ffprobe -i %s -show_entries format=duration -v quiet -of csv=\"p=0\"\n", uriFile);

        FFmpegKit.executeAsync(command, session -> {
            if (!ReturnCode.isSuccess(session.getReturnCode())) {
                Log.e("FFmpeg", "Failed to retrieve video duration: " + session.getFailStackTrace());
            }
        });
    }


}
