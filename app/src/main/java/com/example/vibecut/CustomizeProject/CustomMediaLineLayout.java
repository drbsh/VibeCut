package com.example.vibecut.CustomizeProject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.vibecut.Adapters.CountTimeAndWidth;
import com.example.vibecut.Adapters.LineAdapters.MediaLineAdapter;
import com.example.vibecut.JSONHelper;
import com.example.vibecut.R;
import com.example.vibecut.ViewModels.EditerActivity;

import java.time.Duration;

public class CustomMediaLineLayout extends BaseCustomLineLayout {

    private float initialXDraggingPosition;
    private int differenceLeftBorderFromLeftSide, differenceRightBorderFromRightSide;

    public CustomMediaLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void init() {
        startHandle = findViewById(R.id.start_medialine_item);
        endHandle = findViewById(R.id.end_medialine_item);
        duration = findViewById(R.id.item_duration);
        setHandlesVisibility(isHandleVisible); // Скрыть рамки по умолчанию

        this.post(() -> {
            RelativeLayout.LayoutParams params = (LayoutParams) this.getLayoutParams();
            params.width = mediaFile.getWidthOnTimeline();
            this.setLayoutParams(params);
            requestLayout();
            differenceRightBorderFromRightSide = mediaFile.getDifferenceRightBorderFromRightSide();
            differenceLeftBorderFromLeftSide = mediaFile.getDifferenceLeftBorderFromLeftSide();
        });
        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                isDragging = true;
            }
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        parentLayout = (RelativeLayout) getParent();
        int newWidth = initialWidth; // Инициализируем newWidth значением initialWidth

        int MIN_WIDTH = 100;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getRawX();
                initialXDraggingPosition = getX();
                initialWidth = getWidth();
                parentLayout.requestDisallowInterceptTouchEvent(true); // <<<<---------------- ЗАМЕНА НА SCROLLVIEW

                if (isTouchingStartHandle(event)) {
                    flagStartOrEnd = 1;
                } else if (isTouchingEndHandle(event)) {
                    flagStartOrEnd = 2;
                } else {
                    flagStartOrEnd = 0; // Обработка на какую из рамок нажал пользователь или длительность или вообще не нажимал
                    if (handler.hasMessages(0) || isDragging) {
                        flagDrag = true;
                        isDragging = false;
                    }
                    handler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);

                }

                break;
            case MotionEvent.ACTION_MOVE:
                isScrolling = true; //определяет то что пользователь начал движение пальцем
                dX = event.getRawX() - initialX;


                // Определите, за какую рамку тянет пользователь
                if (flagStartOrEnd == 1) {
                    // Растягиваем с левой стороны
                    flagDrag = true;
                    isDragging = false;

                    if(newWidth - dX < MIN_WIDTH){
                        dX = newWidth - MIN_WIDTH;
                    }
                    else if((newWidth - differenceRightBorderFromRightSide - dX) >= maxWidth )
                    {
                        dX = 0;
                        differenceLeftBorderFromLeftSide = 0;
                        newWidth = maxWidth + differenceRightBorderFromRightSide;
                    }
                    newWidth -= (int) dX; // Уменьшаем ширину

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
                    params.width = newWidth; // Изменяем только ширину
                    this.setLayoutParams(params); // Применяем изменения
                    Duration newDuration = CountTimeAndWidth.TimeByWidthChanged(newWidth);
                    duration.setText(CountTimeAndWidth.formatDurationToString(newDuration));
                    mediaFile.setDuration(newDuration);
                    mediaFile.setWidthOnTimeline(newWidth);
                    requestLayout();
                } else if (flagStartOrEnd == 2) {
                    flagDrag = true;
                    isDragging = false;
                    // Растягиваем с правой стороны
                    if(newWidth + dX < MIN_WIDTH){
                        dX = -(newWidth - MIN_WIDTH);
                    }
                    else if((newWidth  + dX + differenceLeftBorderFromLeftSide) >= maxWidth )
                    {
                        dX = 0;
                        differenceRightBorderFromRightSide = 0;
                        newWidth = maxWidth - differenceLeftBorderFromLeftSide;
                    }
                    newWidth += (int) dX; // Уменьшаем ширину
                    Log.d("TouchEvent", "Resizing from right: newWidth: " + newWidth);
                    resizeItem(newWidth);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
                    params.width = newWidth; // Изменяем только ширину
                    this.setLayoutParams(params); // Применяем изменения
                    Duration newDuration = CountTimeAndWidth.TimeByWidthChanged(newWidth);
                    duration.setText(CountTimeAndWidth.formatDurationToString(newDuration));
                    mediaFile.setDuration(newDuration);
                    mediaFile.setWidthOnTimeline(newWidth);


                    requestLayout();
                } else if (!isDragging) {
                    getParent().requestDisallowInterceptTouchEvent(false);// <<<<---------------- ЗАМЕНА НА SCROLLVIEW
                }
                if (isDragging) {
                    // Устанавливаем флаг, что началось перетаскивание
                    flagDrag = false;
                    setAlpha(0.5f); // Устанавливаем прозрачность
                    requestLayout(); // Запрашиваем повторное размещение

                }

                if (isDragging) {
                    if (flagVibrate) {
                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null) {
                            vibrator.vibrate(150); // вибрация 150 милисек
                        }
                        flagVibrate = false;
                    }
                    float newXThanDragging = initialXDraggingPosition + dX; // Используем rawX для абсолютной позиции

                    setX(newXThanDragging); // Устанавливаем новое положение

                    targetPosition = getTargetPosition(event);
                    if (targetPosition != -1) {
                        highlightTargetPosition(targetPosition);
                    } else {
                        targetPosition = originalPosition;
                        highlightTargetPosition(targetPosition);
                    }

                }

                float touchX = event.getRawX();
                int screenWidth = getResources().getDisplayMetrics().widthPixels;

                // Проверяем, находится ли палец близко к левому краю
                if (touchX < 80) { // 50 пикселей от левого края
                    horizontalScrollView.smoothScrollBy(-10, 0); // Прокрутка влево
                }
                // Проверяем, находится ли палец близко к правому краю
                else if (touchX > screenWidth - 80) { // 50 пикселей от правого края
                    horizontalScrollView.smoothScrollBy(10, 0); // Прокрутка вправо
                }

                // Обновляем listener только если ширина изменилась

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDragging && !flagDrag) {
                    isDragging = false;
                    handler.removeCallbacks(longPressRunnable);
                    setAlpha(1.0f); // Возвращаем прозрачность к норме
                    if (targetPosition == originalPosition) {
                        // Если объект не был перемещен на другую позицию, возвращаем его на исходное место
                        setX(initialXDraggingPosition);
                        resetHighlightTargetPosition(targetPosition);
                    } else {
                        MediaLineAdapter adapter = EditerActivity.getAdapter();
                        adapter.updateWithSwitchPositions(this, targetPosition);
                    }
                }
                if (flagStartOrEnd == 1){
                    differenceLeftBorderFromLeftSide += (int) dX;
                    mediaFile.setDifferenceLeftBorderFromLeftSide(differenceLeftBorderFromLeftSide);
                    videoEditer.ChangeLengthByBorders(CountTimeAndWidth.TimeByWidthChanged(differenceLeftBorderFromLeftSide), CountTimeAndWidth.TimeByWidthChanged(differenceRightBorderFromRightSide));                }
                else if(flagStartOrEnd == 2){
                    differenceRightBorderFromRightSide += (int) dX;
                    mediaFile.setDifferenceRightBorderFromRightSide(differenceRightBorderFromRightSide);
                    videoEditer.ChangeLengthByBorders(CountTimeAndWidth.TimeByWidthChanged(differenceLeftBorderFromLeftSide), CountTimeAndWidth.TimeByWidthChanged(differenceRightBorderFromRightSide));
                }
                else if ((flagStartOrEnd == 0) && !isScrolling) {
                    setHandlesVisibility(true);
                    CustomLayoutHelper.updateHandlesVisibility(this);
                }
                flagVibrate = true;
                isDragging = false;
                isScrolling = false; // возвращаем в значение по дефолту
                JSONHelper.exportToJSON(context, projectInfo);
                Log.d("TouchEvent", "ACTION_UP or ACTION_CANCEL: finalWidth: " + newWidth);
                parentLayout.requestDisallowInterceptTouchEvent(false);// <<<<---------------- ЗАМЕНА НА SCROLLVIEW
                break;
        }
        return true;
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
    public void resetHighlightTargetPosition(int position) {
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            if (i == position) {
                child.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

}
