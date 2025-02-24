package com.example.vibecut.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.vibecut.Models.ProjectInfo;
import com.example.vibecut.R;

import java.io.File;

public class EffectClickListener implements View.OnClickListener {
    private Context context;

    public EffectClickListener(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.layout_brightness) {// Обработка нажатия на "Яркость"
            isAllVideo("Яркость");
            Toast.makeText(context, "Яркость", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.layout_contrast) {// Обработка нажатия на "Контраст"
            Toast.makeText(context, "Контраст", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.layout_saturation) {// Обработка нажатия на "Насыщенность"
            Toast.makeText(context, "Насыщенность", Toast.LENGTH_SHORT).show();
        }
    }
    private void isAllVideo(String effectName){
        new AlertDialog.Builder(context)
                .setTitle("")
                .setMessage("Вы хотите применить эффект " + effectName + "для всего видео?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ВЫЗВАТЬ ФУНКЦИЮ ДЛЯ ПРИМЕНЕНИЯ ЭФФЕКТА
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Выберите необходимый отрывок на таймлайне", Toast.LENGTH_SHORT);
                        //ПОЛУЧИТЬ КЛИК
                        //ЛИБО ПРОСТО ВЫБОР ИЗ СПИСКА
                    }
                })
                .show();
    }
}