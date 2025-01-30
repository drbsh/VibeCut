package com.example.vibecut.CustomizeProject;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.ViewModels.EditerActivity;

public abstract class BaseLineLayout extends RelativeLayout implements BaseLineLayoutInterface {
    protected boolean flagVibrate = true;
    protected EditerActivity context;
    protected float initialX;
    protected float dX;
    protected int initialWidth;
    protected short flagStartOrEnd = 0;
    protected OnWidthChangeListener listener;
    protected View startHandle;
    protected View endHandle;
    protected TextView duration;
    protected CustomLayoutManager layoutManager;
    protected boolean isHandleVisible = false;
    protected boolean isScrolling = false;
    protected MediaFile mediaFile;
    protected Runnable longPressRunnable;
    protected Handler handler = new Handler();
    protected static final long LONG_PRESS_DURATION = 500; // Время зажатия в миллисекундах
    protected boolean isDragging = false;
    protected boolean flagDrag = true;
    protected RelativeLayout parentLayout;
    protected int originalPosition; // Исходная позиция объекта
    protected int targetPosition = 0;

    public BaseLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = (EditerActivity) context; // Привязываем контекст активности
    }

    @Override
    public void setHandlesVisibility(boolean visible){
            startHandle.setVisibility(visible ? View.VISIBLE : View.GONE);
            endHandle.setVisibility(visible ? View.VISIBLE : View.GONE);
        isHandleVisible = visible; // Обновляем состояние видимости
    }



    protected void getLayoutManager() {
        this.layoutManager = EditerActivity.layoutManagerMedia; // Получаем менеджер компоновки
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Общий код для обработки touch events
        return true;
    }
    @Override
    public void resizeItem(int newWidth) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
        params.width = newWidth;
        this.setLayoutParams(params);
        this.requestLayout();
    }
    @Override
    public boolean isTouchingStartHandle(MotionEvent event) {
        if (startHandle.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] location = new int[2];
        startHandle.getLocationInWindow(location);
        float touchX = event.getRawX();
        return touchX >= location[0] && touchX <= location[0] + dpToPx(10);
    }
    @Override
    public boolean isTouchingEndHandle(MotionEvent event) {
        if (endHandle.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] location = new int[2];
        endHandle.getLocationInWindow(location);
        float touchX = event.getRawX();
        return touchX >= location[0] && touchX <= location[0] + dpToPx(10);
    }
    @Override
    public int getTargetPosition(MotionEvent event) {
        float touchX = event.getRawX();
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            if (child != this) { // Игнорируем текущий элемент
                int[] location = new int[2];
                child.getLocationInWindow(location);
                float childLeft = location[0];
                float childRight = childLeft + child.getWidth();

                // Проверяем, находится ли touchX в пределах ширины дочернего элемента
                if (touchX >= childLeft && touchX <= childRight) {
                    return i; // Возвращаем индекс дочернего элемента
                }
            }
        }
        return -1; // Если ничего не найдено, возвращаем -1
    }
    @Override
    public void highlightTargetPosition(int position) {
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            if (i == position) {
                child.setBackgroundColor(Color.LTGRAY); // Подсвечиваем целевую позицию
            } else {
                child.setBackgroundColor(Color.TRANSPARENT); // Сбрасываем цвет для остальных
            }
        }
    }
    @Override
    public void resetPosition() {
        int currentPosition = indexOfChild(this);
        if (currentPosition != originalPosition) {
            parentLayout.removeView(this);
            parentLayout.addView(this, originalPosition);
            setX(0);
            setY(0);
        }
    }
    @Override
    public int getOriginalPosition() {
        return originalPosition;
    }

    protected int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void setOriginalPosition(int position) {
        this.originalPosition = position; // Устанавливаем исходную позицию
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile; // Устанавливаем объект MediaFile
    }

    public void setParentLayout(RelativeLayout parentLayout) {
        this.parentLayout = parentLayout; // Устанавливаем родительский контейнер
    }

    public interface OnWidthChangeListener {
        void onWidthChanged(BaseLineLayout view, int newWidth); // Интерфейс для слушателя изменений ширины
    }

    public void setOnWidthChangeListener(OnWidthChangeListener listener) {
        this.listener = listener; // Устанавливаем слушателя изменений ширины
    }

    protected void notifyWidthChanged(int newWidth) {
        if (listener != null) {
            listener.onWidthChanged(this, newWidth); // Уведомляем слушателя об изменении ширины
        }
    }
}
