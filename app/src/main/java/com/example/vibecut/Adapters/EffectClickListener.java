package com.example.vibecut.Adapters;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.example.vibecut.R;

public class EffectClickListener implements View.OnClickListener {
    private Context context;

    public EffectClickListener(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.layout_brightness) {// Обработка нажатия на "Яркость"
            Toast.makeText(context, "Яркость", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.layout_contrast) {// Обработка нажатия на "Контраст"
            Toast.makeText(context, "Контраст", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.layout_saturation) {// Обработка нажатия на "Насыщенность"
            Toast.makeText(context, "Насыщенность", Toast.LENGTH_SHORT).show();
        }
    }
}