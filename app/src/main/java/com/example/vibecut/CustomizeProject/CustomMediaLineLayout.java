package com.example.vibecut.CustomizeProject;

import static com.example.vibecut.CustomizeProject.CustomLayoutManager.MIN_WIDTH;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.example.vibecut.R;

public class CustomMediaLineLayout extends RelativeLayout {
private float initialX;
private int initialWidth;
private int flagStartOrEnd = 0;
private CustomLayoutManager layoutManager;
private OnWidthChangeListener listener;

    public interface OnWidthChangeListener {
        void onWidthChanged(CustomMediaLineLayout view, int newWidth);
    }
    public void setOnWidthChangeListener(OnWidthChangeListener listener) {
        this.listener = listener;
    }
    public CustomMediaLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setLayoutManager(CustomLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int newWidth = initialWidth; // Инициализируем newWidth значением initialWidth
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialWidth = getWidth();

                if (isTouchingStartHandle(event)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    flagStartOrEnd = 1;
                }
                else if(isTouchingEndHandle(event))
                {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    flagStartOrEnd = 2;
                }
                else flagStartOrEnd = 0; // Обработка на какую из рамок нажал пользователь или вообще не нажимал

                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - initialX;

                // Определите, за какую рамку тянет пользователь
                if (flagStartOrEnd == 1) {
                    // Растягиваем с левой стороны
                    newWidth = initialWidth - (int) dx; // Уменьшаем ширину
                    newWidth = Math.max(MIN_WIDTH, newWidth);
                    layoutManager.resizeItem(this, newWidth); // Вызов метода resizeItem из CustomLayoutManager
                } else if (flagStartOrEnd == 2) {
                    // Растягиваем с правой стороны
                    newWidth = initialWidth + (int) dx; // Увеличиваем ширину
                    newWidth = Math.max(MIN_WIDTH, newWidth);
                    Log.d("TouchEvent", "Resizing from right: newWidth: " + newWidth);
                    layoutManager.resizeItem(this, newWidth); // Вызов метода resizeItem из CustomLayoutManager
                }

                // Обновляем listener только если ширина изменилась
                if (listener != null) {
                    listener.onWidthChanged(this, newWidth); // Notify the listener
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d("TouchEvent", "ACTION_UP or ACTION_CANCEL: finalWidth: " + newWidth);
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    // Метод для проверки, касается ли пользователь левой рамки
    private boolean isTouchingStartHandle(MotionEvent event) {
        int[] location = new int[2];
        findViewById(R.id.start_medialine_item).getLocationOnScreen(location);
        float touchX = event.getRawX();
        boolean isTouching = touchX >= location[0] && touchX <= location[0] + dpToPx(10); // 10 - ширина рамки
        Log.d("TouchEvent", "isTouchingStartHandle: " + isTouching);
        return isTouching;
    }

    // Метод для проверки, касается ли пользователь правой рамки
    private boolean isTouchingEndHandle(MotionEvent event) {
        int[] location = new int[2];
        findViewById(R.id.end_medialine_item).getLocationOnScreen(location);
        float touchX = event.getRawX();
        boolean isTouching = touchX >= location[0] && touchX <= location[0] + dpToPx(10); // 10 - ширина рамки
        Log.d("TouchEvent", "isTouchingEndHandle: " + isTouching);
        return isTouching;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

}