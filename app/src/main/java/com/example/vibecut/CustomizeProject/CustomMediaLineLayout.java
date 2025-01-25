package com.example.vibecut.CustomizeProject;

import static com.example.vibecut.CustomizeProject.CustomLayoutManager.MIN_WIDTH;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.R;
import com.example.vibecut.ViewModels.EditerActivity;

import java.util.List;

public class CustomMediaLineLayout extends RelativeLayout {
private float initialX;
private float initialY;

private int initialWidth;
private short flagStartOrEnd = 0;
private OnWidthChangeListener listener;
private View startHandle;
private View endHandle;
private CustomLayoutManager layoutManager;
private boolean isHandleVisible = false;
private boolean isScrolling = false;
private List<MediaFile> mediaFiles;

public void setLayoutManager(CustomLayoutManager layoutManager){
    this.layoutManager = layoutManager;
}

    public void getmedia(List<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
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
        setHandlesVisibility(isHandleVisible); // Скрыть рамки по умолчанию

    }

    public void setHandlesVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        startHandle.setVisibility(visibility);
        endHandle.setVisibility(visibility);
        isHandleVisible = visible;
        requestLayout();
    }
    public void getLayoutManager(){
        EditerActivity editerActivity = new EditerActivity() ;
        this.layoutManager = editerActivity.layoutManager;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init(); // Инициализация после завершения инфляции
        getLayoutManager();
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
                else
                {
                    flagStartOrEnd = 0; // Обработка на какую из рамок нажал пользователь или вообще не нажимал

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
                if(flagStartOrEnd == 0 && isScrolling == false){
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

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

}