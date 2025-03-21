package com.example.vibecut.ViewModels;

import static com.example.vibecut.Adapters.WorkWithVideo.MakeEffects.changeBrightness;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.vibecut.R;


public class SeekBarDialogFragment extends DialogFragment {

    public interface SeekBarListener {
        void onSeekBarValueChanged(float value);
    }
    private SeekBarListener listener;
    private SeekBar seekBar;
    private TextView valueSeekbarShow;
    private FrameLayout closeSeekbar;
    private FrameLayout saveValue;
    private int percent;
    public void setSeekBarListener(SeekBarListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_seekbar, container, false);

        seekBar = view.findViewById(R.id.seekBar);
        valueSeekbarShow = view.findViewById(R.id.seekBarValue);
        closeSeekbar = view.findViewById(R.id.closeSeekbar);
        saveValue = view.findViewById(R.id.saveValue);

        updateSeekBarValue(seekBar.getProgress());
        //слушатель seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSeekBarValue(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        //кнопка закрытия
        closeSeekbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Закрываем диалог
                dismiss();
            }
        });
        //сохранить значение
        saveValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Убираем затемнение фона
        if (dialog.getWindow() != null) {
            dialog.getWindow().setDimAmount(0f);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Устанавливаем позицию диалога внизу экрана
        Dialog dialog = getDialog();
        if (dialog != null) {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setGravity(Gravity.BOTTOM);

                float density = getResources().getDisplayMetrics().density;
                int marginInPixels = (int) (50 * density);
                dialog.getWindow().getAttributes().y = marginInPixels; // Отступ снизу
            }
        }
    }
    // Метод для обновления значения SeekBar в процентах
    private void updateSeekBarValue(int progress) {
        // Преобразуем значение SeekBar в диапазон от -100% до 100%
        int min = 0;
        int max = seekBar.getMax();
        int range = max - min;
        percent = (int) ((progress - range / 2) * 100.0 / (range / 2));

        // Обновляем TextView
        valueSeekbarShow.setText(percent + "%");
        if (listener != null) {
            listener.onSeekBarValueChanged(percentToFloat());
        }
    }
    private float percentToFloat(){
        return (float) percent / 100;
    }
}