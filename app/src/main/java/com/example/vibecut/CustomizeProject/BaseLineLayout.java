package com.example.vibecut.CustomizeProject;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.vibecut.CustomizeProject.CustomLayoutManager;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.R;
import com.example.vibecut.ViewModels.EditerActivity;

public abstract class BaseLineLayout extends RelativeLayout {
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
    }


    public void init() {
        startHandle = findViewById(R.id.start_medialine_item);
        endHandle = findViewById(R.id.end_medialine_item);
        duration = findViewById(R.id.item_duration);
        setHandlesVisibility(isHandleVisible); // Скрыть рамки по умолчанию

        originalPosition = CustomLayoutManager.getOriginalPosition();
        mediaFile = CustomLayoutManager.getMediasInLayouts();
        parentLayout = CustomLayoutManager.getParentLayout();
        context = CustomLayoutManager.getEditerActivity();

        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                isDragging = true;
            }
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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
        getLayoutManager();
    }

    protected void getLayoutManager() {
        EditerActivity editerActivity = new EditerActivity();
        this.layoutManager = EditerActivity.layoutManagerMedia;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Общий код для обработки touch events
        return true;
    }

    protected void resizeItem(int newWidth) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
        params.width = newWidth;
        this.setLayoutParams(params);
        this.requestLayout();
    }

    protected boolean isTouchingStartHandle(MotionEvent event) {
        if (startHandle.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] location = new int[2];
        startHandle.getLocationInWindow(location);
        float touchX = event.getRawX();
        return touchX >= location[0] && touchX <= location[0] + dpToPx(10);
    }

    protected boolean isTouchingEndHandle(MotionEvent event) {
        if (endHandle.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] location = new int[2];
        endHandle.getLocationInWindow(location);
        float touchX = event.getRawX();
        return touchX >= location[0] && touchX <= location[0] + dpToPx(10);
    }

    protected int getTargetPosition(MotionEvent event) {
        float touchX = event.getRawX();
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            if (child != this) {
                int[] location = new int[2];
                child.getLocationInWindow(location);
                float childLeft = location[0];
                float childRight = childLeft + child.getWidth();
                if (touchX >= childLeft && touchX <= childRight) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected void highlightTargetPosition(int position) {
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            if (i == position) {
                child.setBackgroundColor(Color.LTGRAY);
            } else {
                child.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    protected void resetPosition() {
        int currentPosition = indexOfChild(this);
        if (currentPosition != originalPosition) {
            parentLayout.removeView(this);
            parentLayout.addView(this, originalPosition);
            setX(0);
            setY(0);
        }
    }

    protected int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    public interface OnWidthChangeListener {
        void onWidthChanged(BaseLineLayout view, int newWidth);
    }
}