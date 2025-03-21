package com.example.vibecut.Adapters.WorkWithVideo;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

public class GetVideoDuration {

    /**
     * Получает длительность видео в миллисекундах.
     *
     * @param context Контекст приложения.
     * @param videoUri URI видеофайла.
     * @return Длительность видео в миллисекундах или -1, если произошла ошибка.
     */
    public static long getVideoDuration(Context context, Uri videoUri) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            // Устанавливаем источник данных (видеофайл)
            extractor.setDataSource(context, videoUri, null);

            // Получаем количество треков в файле
            int numTracks = extractor.getTrackCount();

            // Перебираем все треки, чтобы найти видео-трек
            for (int i = 0; i < numTracks; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);

                // Если это видео-трек
                if (mime != null && mime.startsWith("video/")) {
                    // Получаем длительность трека в микросекундах
                    long durationUs = format.getLong(MediaFormat.KEY_DURATION);
                    // Преобразуем микросекунды в миллисекунды
                    return durationUs / 1000;
                }
            }
        } catch (IOException e) {
            Log.e("VideoDurationUtil", "Ошибка при получении длительности видео", e);
        } finally {
            // Освобождаем ресурсы
            extractor.release();
        }

        // Если длительность не найдена, возвращаем -1
        return -1;
    }
}