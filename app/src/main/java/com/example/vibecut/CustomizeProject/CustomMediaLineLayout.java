package com.example.vibecut.CustomizeProject;

import static com.example.vibecut.CustomizeProject.CustomLayoutManager.MIN_WIDTH;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vibecut.Adapters.MediaLineAdapter;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.R;
import com.example.vibecut.ViewModels.EditerActivity;
import com.example.vibecut.ViewModels.TimePickerDialog;

import java.util.List;

public class CustomMediaLineLayout extends RelativeLayout {
    private float initialX;
    private float initialY;
    private float dX;
    private int initialWidth;
    private short flagStartOrEnd = 0;
    private OnWidthChangeListener listener;
    private View startHandle;
    private View endHandle;
    private TextView duration;
    private CustomLayoutManager layoutManager;
    private boolean isHandleVisible = false;
    private boolean isScrolling = false;
    private MediaFile mediaFile;
    private Runnable longPressRunnable;
    private Handler handler = new Handler();
    private static final long LONG_PRESS_DURATION = 1000; // Время зажатия в миллисекундах
    private boolean isDragging = false;
    private boolean flagDrag = false;
    private RelativeLayout parentLayout;
    private int originalPosition; // Исходная позиция объекта
    private  int targetPosition = 0;
    public int getOriginalPosition(){
        return originalPosition;
    }
    public void setOriginalPosition(int originalPosition){
        this.originalPosition = originalPosition;
    }
    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }
    public void setLayoutManager(CustomLayoutManager layoutManager){
        this.layoutManager = layoutManager;
    }
    public interface OnWidthChangeListener {
        void onWidthChanged(CustomMediaLineLayout view, int newWidth);
    }

    public CustomMediaLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void init() {
        startHandle = findViewById(R.id.start_medialine_item);
        endHandle = findViewById(R.id.end_medialine_item);
        duration = findViewById(R.id.item_duration);
        setHandlesVisibility(isHandleVisible); // Скрыть рамки по умолчанию

//        parentLayout = CustomLayoutManager.getParentLayout();
        originalPosition = CustomLayoutManager.getOriginalPosition(); // Обязательно вызываем раньше getMediasInLayouts() (описание почему так в классе)
        mediaFile = CustomLayoutManager.getMediasInLayouts();
        parentLayout = CustomLayoutManager.getParentLayout();

        longPressRunnable = new Runnable() {
            @Override
            public void run() {}
        };

    }

    public void setHandlesVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        startHandle.setVisibility(visibility);
        endHandle.setVisibility(visibility);
        duration.setVisibility(visibility);
        isHandleVisible = visible;
        requestLayout();
    }

    public void getLayoutManager(){
        EditerActivity editerActivity = new EditerActivity() ;
        this.layoutManager = EditerActivity.layoutManager;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init(); // Инициализация после завершения инфляции
        getLayoutManager();
        // присвоение файла происходит по порядку
        // (каждому порядковуму номеру слоя в контейнере
        // присваивается аналогичный индекс структуры из списка)
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        parentLayout = CustomLayoutManager.getParentLayout();
        Log.d("CustomMediaLineLayout", "onTouchEvent called");
        Log.d("CustomMediaLineLayout", "startHandle: " + startHandle + ", endHandle: " + endHandle);
        Log.d("CustomMediaLineLayout", "layoutManager: " + layoutManager + ", isHandleVisible: " + isHandleVisible);
        int newWidth = initialWidth; // Инициализируем newWidth значением initialWidth

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getRawX();
                dX = getX() - initialX;

                initialWidth = getWidth();
                getParent().requestDisallowInterceptTouchEvent(true); // <<<<---------------- ЗАМЕНА НА SCROLLVIEW

                if (isTouchingStartHandle(event)) {
                    flagStartOrEnd = 1;
                }
                else if(isTouchingEndHandle(event))
                {
                    flagStartOrEnd = 2;
                }
                else
                {
                    flagStartOrEnd = 0; // Обработка на какую из рамок нажал пользователь или длительность или вообще не нажимал
                    handler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);
                }

                break;
            case MotionEvent.ACTION_MOVE:
                isScrolling = true; //определяет то что пользователь начал движение пальцем


                if (!isDragging && Math.abs(event.getRawX() - initialX) < 7 && !flagDrag) { // 10 - пороговое значение
                    isDragging = true; // Устанавливаем флаг, что началось перетаскивание
                    setAlpha(0.5f); // Устанавливаем прозрачность
                    bringToFront(); // Выдвигаем элемент вверх по слоям
                    requestLayout(); // Запрашиваем повторное размещение
                }


                if (!isDragging) {
                    flagDrag = true;
                    dX = event.getRawX() - initialX;
                    // Определите, за какую рамку тянет пользователь
                    if (flagStartOrEnd == 1) {
                        // Растягиваем с левой стороны
                        newWidth = initialWidth - (int) dX; // Уменьшаем ширину
                        newWidth = Math.max(MIN_WIDTH, newWidth);
                        resizeItem(newWidth); // Вызов метода resizeItem из CustomLayoutManager
                    } else if (flagStartOrEnd == 2) {
                        // Растягиваем с правой стороны
                        newWidth = initialWidth + (int) dX; // Увеличиваем ширину
                        newWidth = Math.max(MIN_WIDTH, newWidth);
                        Log.d("TouchEvent", "Resizing from right: newWidth: " + newWidth);
                        resizeItem(newWidth); // Вызов метода resizeItem из CustomLayoutManager
                    } else if (!isDragging) {
                        getParent().requestDisallowInterceptTouchEvent(false);// <<<<---------------- ЗАМЕНА НА SCROLLVIEW
                    }
                }
                else {

                    setAlpha(0.5f);
                    float newXThanDragging = event.getRawX() + dX; // Используем rawX для абсолютной позиции



                    setX(newXThanDragging); // Устанавливаем новое положение

                    targetPosition = getTargetPosition(event);
                    if (targetPosition != -1) {
                        highlightTargetPosition(targetPosition);
                    } else {
                        resetPosition();
                    }
                }
                HorizontalScrollView horizontalScrollView = layoutManager.getHorizontalScrollView();
                float touchX = event.getRawX();
                int screenWidth = getResources().getDisplayMetrics().widthPixels;

                // Проверяем, находится ли палец близко к левому краю
                if (touchX < 50) { // 50 пикселей от левого края
                    horizontalScrollView.smoothScrollBy(-10, 0); // Прокрутка влево
                }
                // Проверяем, находится ли палец близко к правому краю
                else if (touchX > screenWidth - 50) { // 50 пикселей от правого края
                    horizontalScrollView.smoothScrollBy(10, 0); // Прокрутка вправо
                }

                // Обновляем listener только если ширина изменилась
                if (listener != null) {
                    listener.onWidthChanged(this, newWidth); // Notify the listener
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(isDragging){
                    isDragging = false;
                    handler.removeCallbacks(longPressRunnable);
                    setAlpha(1.0f); // Возвращаем прозрачность к норме
                    MediaLineAdapter adapter = EditerActivity.getAdapter();
                    adapter.updateWithSwitchPositions(this, targetPosition);

                }
                if((flagStartOrEnd == 0) && !isScrolling){
                    setHandlesVisibility(true);
                    requestLayout();

                    CustomLayoutManager.updateHandlesVisibility(this);
                }
                flagDrag = false;
                isScrolling = false; // возвращаем в значение по дефолту
                Log.d("TouchEvent", "ACTION_UP or ACTION_CANCEL: finalWidth: " + newWidth);
                getParent().requestDisallowInterceptTouchEvent(false);// <<<<---------------- ЗАМЕНА НА SCROLLVIEW
                break;
        }
        return true;
    }
    private void resizeItem(int newWidth) {
        // Устанавливаем новую ширину для текущего объекта
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
        params.width = newWidth;
        this.setLayoutParams(params);
        this.requestLayout(); // Запрашиваем повторное размещение
    }

    // Метод для проверки, касается ли пользователь левой рамки
    private boolean isTouchingStartHandle(MotionEvent event) {
        if (startHandle.getVisibility() != View.VISIBLE) {
            return false; // Если элемент не видим, возвращаем false
        }
        int[] location = new int[2];
        startHandle.getLocationInWindow(location);
        float touchX = event.getRawX();
        boolean isTouching = touchX >= location[0] && touchX <= location[0] + dpToPx(10); // 10 - ширина рамки
        Log.d("TouchEvent", "isTouchingStartHandle: " + isTouching);
        return isTouching;
    }

    // Метод для проверки, касается ли пользователь правой рамки
    private boolean isTouchingEndHandle(MotionEvent event) {
        if (endHandle.getVisibility() != View.VISIBLE) {
            return false; // Если элемент не видим, возвращаем false
        }
        int[] location = new int[2];
        endHandle.getLocationInWindow(location);
        float touchX = event.getRawX();
        boolean isTouching = touchX >= location[0] && touchX <= location[0] + dpToPx(10); // 10 - ширина рамки
        Log.d("TouchEvent", "isTouchingEndHandle: " + isTouching);
        return isTouching;
    }

    private int getTargetPosition(MotionEvent event) {
        // Получаем координаты касания
        float touchX = event.getRawX();
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            if (child != this) { // Игнорируем сам объект
                int[] location = new int[2];
                child.getLocationInWindow(location);
                float childLeft = location[0];
                float childRight = childLeft + child.getWidth();

                // Проверяем, находится ли касание в пределах области элемента по горизонтали
                if (touchX >= childLeft && touchX <= childRight) {
                    return i; // Возвращаем индекс целевой позиции
                }
            }
        }

        return -1; // Если не найдено подходящее место
    }
    private void highlightTargetPosition(int position) {
        // Подсветка целевой позиции (например, изменяем цвет фона)

                for (int i = 0; i < parentLayout.getChildCount(); i++) {
                    View child = parentLayout.getChildAt(i);
                    if (i == position) {
                        child.setBackgroundColor(Color.LTGRAY); // Подсветка
                    } else {
                        child.setBackgroundColor(Color.TRANSPARENT); // Сбрасываем цвет для других
                    }
                }
            }
    private void resetPosition() {
        // Возвращаем объект на исходную позицию, если он не был перемещен
        int currentPosition = indexOfChild(this);
        if (currentPosition != originalPosition) {
            // Удаляем объект из текущей позиции
            parentLayout.removeView(this);
            // Добавляем объект обратно на исходную позицию
            parentLayout.addView(this, originalPosition);
            setX(0); // Сбрасываем X-координату
            setY(0); // Сбрасываем Y-координату
        }
    }
    private void detachDependentObjects() {
        // Логика для отключения зависимых объектов
        // Например, удаление слушателей событий или отключение взаимодействия
        if (layoutManager != null) {
            layoutManager.detach(this); // Пример метода для отключения зависимостей
        }
    }
    private void restoreDependentObjects() {
        // Логика для восстановления зависимых объектов
        if (layoutManager != null) {
            layoutManager.restore(this); // Пример метода для восстановления зависимостей
        }
    }


    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

}
