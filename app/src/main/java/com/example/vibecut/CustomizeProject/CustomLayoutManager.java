package com.example.vibecut.CustomizeProject;


import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.ViewModels.EditerActivity;

import java.util.List;

public class CustomLayoutManager {
    public static final int MIN_WIDTH = 100;
    private static RelativeLayout mediaLineContainer;
    private static EditerActivity context;

    public static int id;
    private HorizontalScrollView horizontalScrollView;
    private static List<MediaFile> MediaFiles;
    private static CustomMediaLineLayout currentVisibleHandlesLayout; // Поле для хранения текущего элемента с видимыми рамками

    public CustomLayoutManager(List<MediaFile> MediaFiles, RelativeLayout mediaLineContainer, EditerActivity context){
        this.context = context;
        this.MediaFiles = MediaFiles;
        this.mediaLineContainer = mediaLineContainer;
        this.context = context;
    }

    public static EditerActivity getEditerActivity() {
        return context;
    }

    public void setHorizontalScrollView(HorizontalScrollView horizontalScrollView){
        this.horizontalScrollView = horizontalScrollView;
    }

    public HorizontalScrollView getHorizontalScrollView() {
        return horizontalScrollView;
    }

    public static void updateHandlesVisibility(CustomMediaLineLayout newLayout) {
        if (currentVisibleHandlesLayout != null && currentVisibleHandlesLayout != newLayout) {
            currentVisibleHandlesLayout.setHandlesVisibility(false); // Скрываем рамки у предыдущего элемента
        }
        currentVisibleHandlesLayout = newLayout; // Обновляем текущий элемент
    }

    public static int getOriginalPosition() {
        return id;
    } // Должен вызываться до getMediasInLayouts чтобы к id не прибавлялась единица
    public static MediaFile getMediasInLayouts() {
        return MediaFiles.get(id++);
    }
    public static RelativeLayout getParentLayout() {
        return mediaLineContainer;
    }


    public void detach(CustomMediaLineLayout customMediaLineLayout) {
        // Отключаем взаимодействие с зависимыми элементами
        for (int i = 0; i < mediaLineContainer.getChildCount(); i++) {
            View child = mediaLineContainer.getChildAt(i);
            if (child instanceof CustomMediaLineLayout && i != customMediaLineLayout.getOriginalPosition()) {
                // Отключаем взаимодействие с другими элементами
                child.setEnabled(false); // Отключаем элемент
                // Вы можете также скрыть или изменить видимость, если это необходимо
            }
        }
    }

    public void restore(CustomMediaLineLayout customMediaLineLayout) {
        // Восстанавливаем взаимодействие с зависимыми элементами
        for (int i = 0; i < mediaLineContainer.getChildCount(); i++) {
            View child = mediaLineContainer.getChildAt(i);
            if (child instanceof CustomMediaLineLayout && i != customMediaLineLayout.getOriginalPosition()) {
                // Восстанавливаем взаимодействие с другими элементами
                child.setEnabled(true); // Включаем элемент
                // Вы можете также восстановить видимость, если это необходимо
            }
        }
    }
}
