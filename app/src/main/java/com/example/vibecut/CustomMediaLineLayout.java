package com.example.vibecut;

import static com.example.vibecut.CustomLayoutManager.MIN_WIDTH;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

public class CustomMediaLineLayout extends RelativeLayout {
private float initialX;
private int initialWidth;
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialWidth = getWidth();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - initialX;
                int newWidth = initialWidth + (int) dx;
                newWidth = Math.max(MIN_WIDTH, newWidth);
                if (listener != null) {
                    listener.onWidthChanged(this, newWidth); // Notify the listener
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }
}