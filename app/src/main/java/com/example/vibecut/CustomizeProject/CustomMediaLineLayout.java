package com.example.vibecut.CustomizeProject;

import static com.example.vibecut.CustomizeProject.CustomLayoutManager.MIN_WIDTH;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.R;
import com.example.vibecut.ViewModels.EditerActivity;
import com.example.vibecut.ViewModels.TimePickerDialog;

import java.util.List;

public class CustomMediaLineLayout extends RelativeLayout {
    private float initialX;
    private float initialY;

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
    public boolean equals(Object obj) {
        if (this == obj) return true; // Проверка на идентичность
        else return false; // Проверка на null и тип
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init(); // Инициализация после завершения инфляции
        getLayoutManager();
        mediaFile = layoutManager.getMediaFileFromContext(this);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("CustomMediaLineLayout", "onTouchEvent called");
        Log.d("CustomMediaLineLayout", "startHandle: " + startHandle + ", endHandle: " + endHandle);
        Log.d("CustomMediaLineLayout", "layoutManager: " + layoutManager + ", isHandleVisible: " + isHandleVisible);
        int newWidth = initialWidth; // Инициализируем newWidth значением initialWidth
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialWidth = getWidth();
                getParent().requestDisallowInterceptTouchEvent(true); // <<<<---------------- ЗАМЕНА НА SCROLLVIEW

                if (isTouchingStartHandle(event)) {
                    flagStartOrEnd = 1;
                }
                else if(isTouchingEndHandle(event))
                {
                    flagStartOrEnd = 2;
                }
                else if(isTouchingDuration(event)){
                    flagStartOrEnd = 3;
                }
                else
                {
                    flagStartOrEnd = 0; // Обработка на какую из рамок нажал пользователь или длительность или вообще не нажимал
                }

                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - initialX;
                isScrolling = true; //определяет то что пользователь начал движение пальцем


                // Определите, за какую рамку тянет пользователь
                if (flagStartOrEnd == 1) {
                    // Растягиваем с левой стороны
                    newWidth = initialWidth - (int) dx; // Уменьшаем ширину
                    newWidth = Math.max(MIN_WIDTH, newWidth);
                    resizeItem(newWidth); // Вызов метода resizeItem из CustomLayoutManager
                } else if (flagStartOrEnd == 2) {
                    // Растягиваем с правой стороны
                    newWidth = initialWidth + (int) dx; // Увеличиваем ширину
                    newWidth = Math.max(MIN_WIDTH, newWidth);
                    Log.d("TouchEvent", "Resizing from right: newWidth: " + newWidth);
                    resizeItem(newWidth); // Вызов метода resizeItem из CustomLayoutManager
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);// <<<<---------------- ЗАМЕНА НА SCROLLVIEW
                }

                // Обновляем listener только если ширина изменилась
                if (listener != null) {
                    listener.onWidthChanged(this, newWidth); // Notify the listener
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if((flagStartOrEnd == 0  || flagStartOrEnd == 3) && !isScrolling){
                    if(flagStartOrEnd == 3){
                        // Запускаем диалоговое окно для редактирования времени
                        showTimePickerDialog(mediaFile);
                    }
                    setHandlesVisibility(true);
                    requestLayout();
                    CustomLayoutManager.updateHandlesVisibility(this);
                }
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
    // Метод для проверки, касается ли пользователь строки длительности
    private boolean isTouchingDuration(MotionEvent event) {
        if (duration.getVisibility() != View.VISIBLE) {
            return false; // Если элемент не видим, возвращаем false
        }
        int[] location = new int[2];
        duration.getLocationInWindow(location);
        float touchX = event.getRawX();
        float touchY = event.getRawY();

        // Проверяем, находится ли касание в пределах ширины и высоты элемента duration
        boolean isTouching = touchX >= location[0] && touchX <= location[0] + duration.getWidth() &&
                touchY >= location[1] && touchY <= location[1] + duration.getHeight();

        Log.d("TouchEvent", "isTouchingDuration: " + isTouching);
        return isTouching;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void showTimePickerDialog(MediaFile currentMediaFile) {
        // Получаем текущие значения времени
        List<Integer> currentTime = getCurrentDuration(duration.getText().toString());

        // Создаем экземпляр TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(currentTime, currentMediaFile);

        // Показываем диалог
        timePickerDialog.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "timePicker");
    }
    private List<Integer> getCurrentDuration(String durationString){
        String[] parts = durationString.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int millis = Integer.parseInt(parts[3]);
        return List.of(hours, minutes, seconds, millis);
    }

}