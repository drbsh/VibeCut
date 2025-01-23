package com.example.vibecut.CustomizeProject;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.example.vibecut.Adapters.MediaLineAdapter;

public class CustomLayoutManager extends RecyclerView.LayoutManager {
    private MediaLineAdapter adapter;
    public static final int MIN_WIDTH = 100;
    private static final String TAG = "CustomLayoutManager"; // Тег для логов
    private int countMedia;
    private int totalContentWidth;
    private int scrollOffset = 0;
    private int totalContentWidthWithoutMargins = 0;


    public CustomLayoutManager(int countMedia){
        this.countMedia = countMedia;
        this.adapter = adapter;
    }
    public void setAdapter(MediaLineAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // Сохраняем текущее положение прокрутки
        int scrollX = getHorizontalScrollOffset();

        detachAndScrapAttachedViews(recycler);
        totalContentWidth = 0;
        int itemCount = getItemCount();
        int x = getPaddingLeft();

        for (int i = 0; i < itemCount; i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            if (view.getVisibility() == View.VISIBLE) {
                layoutDecorated(view, x, getPaddingTop(), x + width, getPaddingTop() + height);
                x += width + 10;
                totalContentWidth += width + 10;
                totalContentWidthWithoutMargins += width;
            }
        }

        // Восстанавливаем положение прокрутки
        offsetChildrenHorizontal(-scrollX);
        scrollOffset = getHorizontalScrollOffset();
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrollAmount = dx;
        int currentOffset = scrollOffset; // Используем отслеживаемое смещение прокрутки

        int leftLimit = -getPaddingLeft() - 300; // Добавлено дополнительное пространство для предотвращения скачков
        int rightLimit = totalContentWidth - getWidth() + getPaddingRight() + 300; // Добавлено дополнительное пространство

        if (currentOffset + dx < leftLimit) {
            scrollAmount = leftLimit - currentOffset;
        } else if (currentOffset + dx > rightLimit) {
            scrollAmount = rightLimit - currentOffset;
        }

        offsetChildrenHorizontal(-scrollAmount);
        scrollOffset += scrollAmount;

        Log.d(TAG, "scrollHorizontallyBy: dx=" + dx + ", scrollAmount=" + scrollAmount + ", currentOffset=" + currentOffset);
        Log.d(TAG, "scrollHorizontallyBy: leftLimit=" + leftLimit + ", rightLimit=" + rightLimit);

        // Более точный расчет положения скроллбара
        float scrollbarPosition = (float) Math.max(0, currentOffset + getPaddingLeft()) / Math.max(1, totalContentWidth) * 100; // Избегаем деления на ноль
        Log.d(TAG, "scrollHorizontallyBy: scrollbarPosition (approx) = " + scrollbarPosition + "%");

        return scrollAmount;
    }


    public void resizeItem(CustomMediaLineLayout view, int newWidth) {
        int oldWidth = getDecoratedMeasuredWidth(view);
        int widthDifference = newWidth - oldWidth;
        totalContentWidthWithoutMargins += widthDifference;
        totalContentWidth += widthDifference + (widthDifference > 0 ? 10 : -10);

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        params.width = newWidth;
        view.setLayoutParams(params);

        requestLayout();
    }
    //Получение текущего горизонтального смещения
    private int getHorizontalScrollOffset() {
        if (getChildCount() > 0) {
            return -getDecoratedLeft(getChildAt(0));
        }
        return 0;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }


}
