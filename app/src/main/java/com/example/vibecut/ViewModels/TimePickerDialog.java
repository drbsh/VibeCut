package com.example.vibecut.ViewModels;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView; // Используем TextView вместо Button
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.vibecut.CustomizeProject.CustomTimePicker;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.R;
import java.util.List;

public class TimePickerDialog extends DialogFragment {
    private CustomTimePicker customTimePicker;
    private Context context;
    private TimePickerDialogListener listener;
    private OnTimeSetListener listener2;
    private MediaFile mediaFile;
    private List<Integer> times;

    public interface OnTimeSetListener {
        void onTimeSet();
    }

    public void setOnTimeSetListener(OnTimeSetListener listener2) {
        this.listener2 = listener2;
    }

    private void notifyTimeSet() {
        if (listener2 != null) {
            listener2.onTimeSet();
        }
    }

    public void onTimeSaved(int hours, int minutes, int seconds, int millis, MediaFile mediaFile) {
        if (listener != null) {
            listener.onTimeSaved(hours, minutes, seconds, millis, mediaFile);
        }
    }

    public interface TimePickerDialogListener {
        void onTimeSaved(int hours, int minutes, int seconds, int millis, MediaFile mediaFile);
    }

    public TimePickerDialog(List<Integer> times, MediaFile mediaFile) {
        this.times = times;
        this.mediaFile = mediaFile;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof TimePickerDialogListener) {
            listener = (TimePickerDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " должен реализовать TimePickerDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_time_picker, container, false);

        customTimePicker = view.findViewById(R.id.custom_time_picker);
        customTimePicker.setTime(times);

        // Находим TextView вместо Button
        TextView buttonSave = view.findViewById(R.id.selectMediaText); // Исправлено на правильный ID
        TextView buttonCancel = view.findViewById(R.id.button_save);

        // Получаем состояние темы из SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDarkTheme = preferences.getBoolean("isDarkTheme", false);

        // Устанавливаем фон для диалогового окна
        if (isDarkTheme) {
            view.setBackgroundResource(R.drawable.layout_project_dialog_background_black);
            buttonSave.setBackgroundResource(R.drawable.rounded_button_background_dark);
            buttonCancel.setBackgroundResource(R.drawable.rounded_button_background_dark);
            // Устанавливаем цвет текста на белый
            buttonSave.setTextColor(getResources().getColor(R.color.white));
            buttonCancel.setTextColor(getResources().getColor(R.color.white));
        } else {
            view.setBackgroundResource(R.drawable.layout_project_dialog_background); // Убедитесь, что у вас есть этот ресурс
            buttonSave.setBackgroundResource(R.drawable.rounded_button_background);
            buttonCancel.setBackgroundResource(R.drawable.rounded_button_background);

            // Устанавливаем цвет текста на черный
            buttonSave.setTextColor(getResources().getColor(R.color.black));
            buttonCancel.setTextColor(getResources().getColor(R.color.black));
        }
        customTimePicker.updateTextColor(isDarkTheme);


        // Обработка нажатия на "Сохранить"
        buttonSave.setOnClickListener(v -> {
            int hours = customTimePicker.getHours();
            int minutes = customTimePicker.getMinutes();
            int seconds = customTimePicker.getSeconds();
            int millis = customTimePicker.getMillis();

            if (listener != null) {
                listener.onTimeSaved(hours, minutes, seconds, millis, mediaFile);
            }
            notifyTimeSet();
            dismiss(); // Закрываем диалог
        });

        // Обработка нажатия на "Отмена"
        buttonCancel.setOnClickListener(v -> dismiss()); // Закрываем диалог

        return view;
    }
}