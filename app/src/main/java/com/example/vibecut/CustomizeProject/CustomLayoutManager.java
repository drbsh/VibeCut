package com.example.vibecut.CustomizeProject;

import static android.view.View.VISIBLE;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.vibecut.Adapters.MediaLineAdapter;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.R;

import java.util.List;

public class CustomLayoutManager {
    public static final int MIN_WIDTH = 100;
    private LinearLayout mediaLineContainer;
    private List<MediaFile> MediaFiles;
    private static CustomMediaLineLayout currentVisibleHandlesLayout; // Поле для хранения текущего элемента с видимыми рамками


    public CustomLayoutManager(List<MediaFile> MediaFiles, LinearLayout mediaLineContainer){
        this.MediaFiles = MediaFiles;
        this.mediaLineContainer = mediaLineContainer;
    }
    public static void updateHandlesVisibility(CustomMediaLineLayout newLayout) {
        if (currentVisibleHandlesLayout != null && currentVisibleHandlesLayout != newLayout) {
            currentVisibleHandlesLayout.setHandlesVisibility(false); // Скрываем рамки у предыдущего элемента
        }
        currentVisibleHandlesLayout = newLayout; // Обновляем текущий элемент
    }

    public MediaFile getMediaFileFromContext(CustomMediaLineLayout customMediaLineLayout){
        for (int i = 0; i < mediaLineContainer.getChildCount(); i++) {
            View child = mediaLineContainer.getChildAt(i);

            // Проверяем, является ли дочерний элемент экземпляром CustomMediaLineLayout
            if (child instanceof CustomMediaLineLayout) {
                CustomMediaLineLayout layout = (CustomMediaLineLayout) child;

                // Сравниваем с переданным layout
                if (customMediaLineLayout.equals(layout)) {
                    // Предполагаем, что у CustomMediaLineLayout есть метод для получения MediaFile
                    return MediaFiles.get(i);
                }
            }
        }
        return null;
    }
}
