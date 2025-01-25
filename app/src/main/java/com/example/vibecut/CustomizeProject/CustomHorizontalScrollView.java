package com.example.vibecut.CustomizeProject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

import com.example.vibecut.R;

public class CustomHorizontalScrollView extends HorizontalScrollView {

    public CustomHorizontalScrollView(Context context) {
        super(context);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int translationX = (int) getResources().getDimension(R.dimen.margin_150dp);

        // Используем post() для отложенного выполнения
        post(new Runnable() {
            @Override
            public void run() {
                smoothScrollTo(translationX, 0);
            }
        });
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    // Вы можете переопределить другие методы, если это необходимо
}