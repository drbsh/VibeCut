package com.example.vibecut.Adapters;

import static com.example.vibecut.Adapters.WorkWithVideo.MakeEffects.changeBrightness;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.vibecut.Models.ProjectInfo;
import com.example.vibecut.R;
import com.example.vibecut.ViewModels.SeekBarDialogFragment;

import java.io.File;

public class EffectClickListener implements View.OnClickListener, SeekBarDialogFragment.SeekBarListener {
    private Context context;
    private PopupWindow effectsPopupWindow;
    private float seekBarValue;
    public EffectClickListener(Context context, PopupWindow effectsPopupWindow) {
        this.context = context;
        this.effectsPopupWindow = effectsPopupWindow;
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
        effectsPopupWindow.dismiss(); //скрытие popup
    }
    private void isAllVideo(String effectName){
        new AlertDialog.Builder(context)
                .setTitle("")
                .setMessage("Вы хотите применить эффект " + effectName + " для всего проекта?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSeekbar();
                        //changeBrightness
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

    private float showSeekbar(){
        SeekBarDialogFragment dialog = new SeekBarDialogFragment();
        dialog.setSeekBarListener(this);
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        dialog.show(fragmentManager, "SeekBarDialogFragment");
        return 0;
    }
    @Override
    public void onSeekBarValueChanged(float value) {
        seekBarValue = value;
    }
}