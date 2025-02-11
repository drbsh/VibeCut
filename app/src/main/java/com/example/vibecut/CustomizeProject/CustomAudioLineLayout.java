package com.example.vibecut.CustomizeProject;

import static com.example.vibecut.CustomizeProject.CustomLayoutManager.MIN_WIDTH;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

import com.example.vibecut.Adapters.MediaLineAdapter;
import com.example.vibecut.R;
import com.example.vibecut.ViewModels.EditerActivity;

public class CustomAudioLineLayout extends BaseCustomLineLayout {
    public CustomAudioLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
        getLayoutManager();
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
    public void init() {
        startHandle = findViewById(R.id.start_medialine_item);
        endHandle = findViewById(R.id.end_medialine_item);
        duration = findViewById(R.id.item_duration);
        setHandlesVisibility(isHandleVisible); // Скрыть рамки по умолчанию

        originalPosition = CustomLayoutManager.getOriginalPositionMedia();
        mediaFile = CustomLayoutManager.getMediasInLayouts();
        parentLayout = CustomLayoutManager.getParentLayout();
        context = CustomLayoutManager.getEditerActivity();
        if(mediaFile.getWidthOnTimeline() == 0) {
            mediaFile.setWidthOnTimeline(dpToPx(200));
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                CustomLayoutManager.getWidthByPositionInit(originalPosition),
                RelativeLayout.LayoutParams.WRAP_CONTENT

        );
        this.setLayoutParams(params);
        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                isDragging = true;
            }
        };
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


                // Определите, за какую рамку тянет пользователь
                if (flagStartOrEnd == 1) {
                    // Растягиваем с левой стороны
                    flagDrag = true;
                    isDragging = false;
                    dX = event.getRawX() - initialX;
                    newWidth = initialWidth - (int) dX; // Уменьшаем ширину
                    newWidth = Math.max(MIN_WIDTH, newWidth);
                    layoutManager.setWidth(newWidth, this);
                    requestLayout();
                } else if (flagStartOrEnd == 2) {
                    flagDrag = true;
                    isDragging = false;
                    dX = event.getRawX() - initialX;
                    // Растягиваем с правой стороны
                    newWidth = initialWidth + (int) dX; // Увеличиваем ширину
                    newWidth = Math.max(MIN_WIDTH, newWidth);
                    Log.d("TouchEvent", "Resizing from right: newWidth: " + newWidth);
                    layoutManager.setWidth(newWidth, this);
                    requestLayout();
                } else if (!isDragging) {
                    getParent().requestDisallowInterceptTouchEvent(false);// <<<<---------------- ЗАМЕНА НА SCROLLVIEW
                }
                if (isDragging) { // 10 - пороговое значение
                    // Устанавливаем флаг, что началось перетаскивание
                    flagDrag = false;
                    setAlpha(0.5f); // Устанавливаем прозрачность
                    requestLayout(); // Запрашиваем повторное размещение

                }

                if (isDragging) {
                    if (flagVibrate) {
                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null) {
                            vibrator.vibrate(300); // Вибрация на 500 миллисекунд
                        }
                        flagVibrate = false;
                    }
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
                if (isDragging && !flagDrag) {
                    isDragging = false;
                    handler.removeCallbacks(longPressRunnable);
                    setAlpha(1.0f); // Возвращаем прозрачность к норме
                    MediaLineAdapter adapter = EditerActivity.getAdapter();
                    adapter.updateWithSwitchPositions(this, targetPosition);

                }
                if ((flagStartOrEnd == 0) && !isScrolling) {
                    setHandlesVisibility(true);

                    CustomLayoutManager.updateHandlesVisibility(this);
                }
                flagVibrate = true;
                isDragging = false;
                isScrolling = false; // возвращаем в значение по дефолту
                Log.d("TouchEvent", "ACTION_UP or ACTION_CANCEL: finalWidth: " + newWidth);
                getParent().requestDisallowInterceptTouchEvent(false);// <<<<---------------- ЗАМЕНА НА SCROLLVIEW
                break;
        }
        return true;
    }
    public void resetPosition() {

        // Получаем текущего родителя
        RelativeLayout parent = (RelativeLayout) getParent();
        if (parent != null) {
            // Удаляем элемент из текущего родителя
            parent.removeView(this);
        }

        // Добавляем элемент в оригинальный контейнер
        if (parentLayout != null) {
            parentLayout.addView(this, originalPosition);
        }

        // Устанавливаем оригинальную позицию
        setX(originalPosition);

        // Обновляем layout
        requestLayout();
    }
}
