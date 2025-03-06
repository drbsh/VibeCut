package com.example.vibecut.CustomizeProject;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.vibecut.Adapters.WorkWithVideo.VideoEditer;
import com.example.vibecut.Models.BaseFile;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.Models.ProjectInfo;
import com.example.vibecut.ViewModels.EditerActivity;

public abstract class BaseCustomLineLayout extends RelativeLayout implements BaseCustomLineLayoutInterface {
    protected boolean flagVibrate = true;
    protected Context context;
    protected float initialX;
    protected float dX;
    protected int initialWidth;
    protected short flagStartOrEnd = 0;
    protected OnWidthChangeListener listener;
    protected View startHandle;
    protected View endHandle;

    protected TextView duration;
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
    protected ProjectInfo projectInfo;
    public VideoEditer videoEditer;
    protected HorizontalScrollView horizontalScrollView;

    public BaseCustomLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = (EditerActivity) context; // Привязываем контекст активности
    }
    @Override
    public void setHandlesVisibility(boolean visible){
            startHandle.setVisibility(visible ? View.VISIBLE : View.GONE);
            endHandle.setVisibility(visible ? View.VISIBLE : View.GONE);
        isHandleVisible = visible; // Обновляем состояние видимости
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




    protected int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    public interface OnWidthChangeListener {
        void onWidthChanged(BaseCustomLineLayout view, int newWidth); // Интерфейс для слушателя изменений ширины
    }
    public void setOnWidthChangeListener(OnWidthChangeListener listener) {
        this.listener = listener; // Устанавливаем слушателя изменений ширины
    }
    protected void notifyWidthChanged(int newWidth) {
        if (listener != null) {
            listener.onWidthChanged(this, newWidth); // Уведомляем слушателя об изменении ширины
        }
    }



    @Override
    public int getOriginalPosition() {
        return originalPosition;
    }
    public void setOriginalPosition(int position) {
        this.originalPosition = position; // Устанавливаем исходную позицию
    }
    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }
    public void setProjectInfo(ProjectInfo projectInfo){this.projectInfo = projectInfo;}
    public void setParentLayout(RelativeLayout parentLayout) {
        this.parentLayout = parentLayout;
    }
    public void setContext(Context context) { this.context = context; }
    public void setVideoEditer(){
        videoEditer = new VideoEditer(mediaFile);
    }
    public void setHorizontalScrollView() {this.horizontalScrollView = horizontalScrollView;}


}
