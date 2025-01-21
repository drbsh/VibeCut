package com.example.vibecut;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;

public class CustomLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "CustomLayoutManager"; // Тег для логов
    private int countMedia;
    private int totalContentWidth;

    public CustomLayoutManager(int countMedia){
        this.countMedia = countMedia;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        totalContentWidth = 0;
        int itemCount = getItemCount();
        int x = getPaddingLeft(); // Начинаем с левого отступа

        Log.d(TAG, "onLayoutChildren: started, itemCount=" + itemCount);

        for (int i = 0; i < itemCount; i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            if (view.getVisibility() == View.VISIBLE) {
                layoutDecorated(view, x, getPaddingTop(), x + width, getPaddingTop() + height);
                x += width + 10; // Отступ между элементами
                totalContentWidth += width + 10;
                Log.d(TAG, "onLayoutChildren: Item " + i + ", left=" + x + ", width=" + width);
            }
        }

        Log.d(TAG, "onLayoutChildren: finished, totalContentWidth=" + totalContentWidth);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrollAmount = dx;
        int currentOffset = 0; // Начальное смещение 0

        // Ограничение прокрутки:
        int leftLimit = -getPaddingLeft() - 300;
        int rightLimit = totalContentWidth - getWidth() + getPaddingRight() + 300;

        //  Вместо getDecoratedLeft(getChildAt(0)) используем текущее смещение
        currentOffset = getHorizontalScrollOffset();

        if (currentOffset + dx < leftLimit) {
            scrollAmount = leftLimit - currentOffset;
        } else if (currentOffset + dx > rightLimit) {
            scrollAmount = rightLimit - currentOffset;
        }

        offsetChildrenHorizontal(-scrollAmount);

        Log.d(TAG, "scrollHorizontallyBy: dx=" + dx + ", scrollAmount=" + scrollAmount + ", currentOffset=" + currentOffset);
        Log.d(TAG, "scrollHorizontallyBy: leftLimit=" + leftLimit + ", rightLimit=" + rightLimit);

        // Более точный расчет положения скроллбара
        float scrollbarPosition = (float) Math.max(0, currentOffset + getPaddingLeft()) / Math.max(1, totalContentWidth) * 100; // Избегаем деления на ноль
        Log.d(TAG, "scrollHorizontallyBy: scrollbarPosition (approx) = " + scrollbarPosition + "%");

        return scrollAmount;
    }


    // ... остальной код ...

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
