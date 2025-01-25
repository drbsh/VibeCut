package com.example.vibecut.CustomizeProject;

import static android.view.View.VISIBLE;


import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.vibecut.Adapters.MediaLineAdapter;
import com.example.vibecut.R;

public class CustomLayoutManager {
    public static final int MIN_WIDTH = 100;
    private static CustomMediaLineLayout currentVisibleHandlesLayout; // Поле для хранения текущего элемента с видимыми рамками


    public static void updateHandlesVisibility(CustomMediaLineLayout newLayout) {
        if (currentVisibleHandlesLayout != null && currentVisibleHandlesLayout != newLayout) {
            currentVisibleHandlesLayout.setHandlesVisibility(false); // Скрываем рамки у предыдущего элемента
        }
        currentVisibleHandlesLayout = newLayout; // Обновляем текущий элемент
    }
}
