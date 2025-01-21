package com.example.vibecut;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CustomMediaLineLayout extends RelativeLayout {
    private View startHandle;
    private View endHandle;

    private float initialX;
    private float initialWidth;

    public CustomMediaLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Инициализация ваших элементов
        startHandle = (View) findViewById(R.id.start_medialine_item);// возвращает null
        endHandle = findViewById(R.id.end_medialine_item); // возвращает null не надит по id
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Запоминаем начальные координаты и ширину
                initialX = event.getX();
                initialWidth = getWidth();
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - initialX;

                // Проверяем, на какой рамке было нажатие
                if (event.getX() < startHandle.getX() + startHandle.getWidth()) { // ЗДЕСЬ ОШИБКА
                    // Растягиваем с левой стороны
                    float newWidth = initialWidth - deltaX;
                    if (newWidth > 100) {
                        setLayoutParams(new RelativeLayout.LayoutParams((int) newWidth, getHeight()));
                    }
                } else if (event.getX() > endHandle.getX()) {
                    // Растягиваем с правой стороны
                    float newWidth = initialWidth + deltaX;
                    if (newWidth > 100) {
                        setLayoutParams(new RelativeLayout.LayoutParams((int) newWidth, getHeight()));
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                // Завершаем обработку
                break;
        }
        return true;
    }
}