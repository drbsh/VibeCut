package com.example.vibecut.CustomizeProject;

import android.view.MotionEvent;

public interface BaseCustomLineLayoutInterface {
    void init();
    void setHandlesVisibility(boolean visible);
    int getOriginalPosition();
    void resizeItem(int newWidth);
    boolean isTouchingStartHandle(MotionEvent event);
    boolean isTouchingEndHandle(MotionEvent event);
    int getTargetPosition(MotionEvent event);
    void highlightTargetPosition(int position);
}
