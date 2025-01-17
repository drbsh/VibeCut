package com.example.vibecut;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;

public class CustomLayoutManager extends RecyclerView.LayoutManager {
    private int countMedia;
    public CustomLayoutManager(int countMedia){
        this.countMedia = countMedia;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        int itemCount = getItemCount();
        int x = 0; // Начальное положение по горизонтали

        for (int i = 0; i < itemCount; i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0); // Измеряем элемент
            int width = getDecoratedMeasuredWidth(view); // Получаем ширину элемента
            int height = getDecoratedMeasuredHeight(view); // Получаем высоту элемента
            layoutDecorated(view, x, 0, x + width, height); // Располагаем элемент
            x += width + 10; // Обновляем позицию для следующего элемента, добавляя отступ 10
        }
    }


    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        offsetChildrenHorizontal(-dx);
        return dx;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public int getItemCount() {
        return countMedia;
    }

    public static class ItemTouchListener implements RecyclerView.OnItemTouchListener {
        private final CustomLayoutManager layoutManager;
        private float initialX;
        private View touchedView;

        public ItemTouchListener(CustomLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                if (childView != null) {
                    touchedView = childView;
                    initialX = e.getX();
                    return true; // Перехватываем событие
                }
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            if (touchedView != null) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getX() - initialX;
                        int newWidth = (int) (layoutManager.getDecoratedMeasuredWidth(touchedView) + dx);

                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) touchedView.getLayoutParams();
                        params.width = newWidth;
                        touchedView.setLayoutParams(params);
                        layoutManager.requestLayout();
                        initialX = e.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        touchedView = null;
                        break;
                }
            }
        }


        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            //  Необязательно
        }
    }
}
