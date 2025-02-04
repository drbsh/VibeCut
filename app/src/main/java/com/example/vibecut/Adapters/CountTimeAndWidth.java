package com.example.vibecut.Adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.time.Duration;

public class CountTimeAndWidth {
    private Context context;
    private int screenWidth;
    private int oneSecondWidth;
    public CountTimeAndWidth(Context context){
        this.context = context;
        screenWidth = getScreenWidth();
    }
    public Duration TimeByWidthChanged(int newWidth){
        oneSecondWidth = getOneSecondWidth();
        double durationInSeconds = (double) newWidth / oneSecondWidth;
        return Duration.ofSeconds((long) durationInSeconds);
    }
    public int WidthByTimeChanged(Duration newTime){
        oneSecondWidth = getOneSecondWidth();
        long durationInSeconds = newTime.getSeconds();
        // Вычисляем ширину на основе одной секунды
        return (int) (durationInSeconds * oneSecondWidth);
    }

    public static String formatDurationToString(Duration duration){
            long totalMillis = duration.toMillis();

            long hours = totalMillis / 3_600_000;
            long minutes = (totalMillis % 3_600_000) / 60_000;
            long seconds = (totalMillis % 60_000) / 1_000;
            long millis = totalMillis % 1_000;

            return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis);
    }

    private int getOneSecondWidth(){
        return screenWidth / 30;
    }
    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

}
