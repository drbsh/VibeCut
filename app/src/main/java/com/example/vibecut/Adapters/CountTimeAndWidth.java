package com.example.vibecut.Adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.time.Duration;

public class CountTimeAndWidth {
    private Context context;
    private int screenWidth;
    private double oneMilliSecondWidth;
    public CountTimeAndWidth(Context context){
        this.context = context;
        screenWidth = getScreenWidth();
    }
    public Duration TimeByWidthChanged(int newWidth){
        oneMilliSecondWidth = getOneMilliSecondWidth();
        double durationInSeconds = (double) newWidth / oneMilliSecondWidth;
        return Duration.ofMillis((long) durationInSeconds);
    }
    public int WidthByTimeChanged(Duration newTime){
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

    private double getOneMilliSecondWidth(){
        return (double) screenWidth / 30000;
    }

    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

}
