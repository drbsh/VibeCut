package com.example.vibecut.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.time.Duration;

public class CountTimeAndWidth {
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static int screenWidth;
    private static double oneMilliSecondWidth;
    public CountTimeAndWidth(Context context){
        CountTimeAndWidth.context = context;
        screenWidth = getScreenWidth();
    }
    public static Duration TimeByWidthChanged(int newWidth){
        oneMilliSecondWidth = getOneMilliSecondWidth();
        double durationInSeconds = (double) newWidth / oneMilliSecondWidth;
        return Duration.ofMillis((long) durationInSeconds);
    }

    public static int WidthByTimeChanged(Duration newTime){
        oneMilliSecondWidth = getOneMilliSecondWidth();
        long durationInMilliSeconds = newTime.toMillis();
        return (int) (durationInMilliSeconds * oneMilliSecondWidth);
    }


    public static String formatDurationToString(Duration duration){
            long totalMillis = duration.toMillis();

            long hours = totalMillis / 3_600_000;
            long minutes = (totalMillis % 3_600_000) / 60_000;
            long seconds = (totalMillis % 60_000) / 1_000;
            long millis = totalMillis % 1_000;

            return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis);
    }

    private static double getOneMilliSecondWidth(){
        return (double) screenWidth / 30000;
    }

    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static Duration subtractDurations(Duration duration1, Duration duration2) {
        long durLong1 = WidthByTimeChanged(duration1);
        long durLong2 = WidthByTimeChanged(duration2);
        long result = durLong1 - durLong2;
        return TimeByWidthChanged((int)Math.max(result, 0)); // Возвращаем 0, если результат отрицательный
    }

}
