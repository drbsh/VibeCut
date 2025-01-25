package com.example.vibecut.CustomizeProject;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.example.vibecut.R;

import java.util.List;

public class CustomTimePicker extends LinearLayout {
    private NumberPicker npHours, npMinutes, npSeconds, npMillis;
    private OnTimeChangedListener onTimeChangedListener;

    public CustomTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.custom_number_picker, this); // Ваша XML-разметка
        npHours = findViewById(R.id.np_hours);
        npMinutes = findViewById(R.id.np_minutes);
        npSeconds = findViewById(R.id.np_seconds);
        npMillis = findViewById(R.id.np_millis);

        // Установка диапазонов значений для NumberPicker
        npHours.setMinValue(0);
        npHours.setMaxValue(23);
        npMinutes.setMinValue(0);
        npMinutes.setMaxValue(59);
        npSeconds.setMinValue(0);
        npSeconds.setMaxValue(59);
        npMillis.setMinValue(0);
        npMillis.setMaxValue(999);


        // Добавление слушателей для изменения значений
        npHours.setOnValueChangedListener(onValueChangeListener);
        npMinutes.setOnValueChangedListener(onValueChangeListener);
        npSeconds.setOnValueChangedListener(onValueChangeListener);
        npMillis.setOnValueChangedListener(onValueChangeListener);
    }

    private final NumberPicker.OnValueChangeListener onValueChangeListener = (picker, oldVal, newVal) -> {
        if (onTimeChangedListener != null) {
            onTimeChangedListener.onTimeChanged(this, getHours(), getMinutes(), getSeconds(), getMillis());
        }
    };


    public int getHours() { return npHours.getValue(); }
    public int getMinutes() { return npMinutes.getValue(); }
    public int getSeconds() { return npSeconds.getValue(); }
    public int getMillis() { return npMillis.getValue(); }

    public void setTime(List<Integer> times) {
        npHours.setValue(times.get(0));
        npMinutes.setValue(times.get(1));
        npSeconds.setValue(times.get(2));
        npMillis.setValue(times.get(3));
    }

    public interface OnTimeChangedListener {
        void onTimeChanged(CustomTimePicker view, int hourOfDay, int minute, int second, int millis);
    }
}
