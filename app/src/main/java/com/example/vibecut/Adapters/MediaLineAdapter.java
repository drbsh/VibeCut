package com.example.vibecut.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vibecut.CustomizeProject.CustomLayoutManager;
import com.example.vibecut.CustomizeProject.CustomMediaLineLayout;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.R;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaLineAdapter extends RecyclerView.Adapter<MediaLineAdapter.ViewHolder> {
    private List<MediaFile> mediaFiles;
    private List<Integer> mediaFileWidths;
    private LayoutInflater inflater;
    private final CustomLayoutManager layoutManager;
    private static final int MIN_WIDTH = 100; // Минимальная ширина элемента

    public MediaLineAdapter(Context context, List<MediaFile> mediaFiles, CustomLayoutManager layoutManager) {
        this.mediaFiles = mediaFiles;
        this.mediaFileWidths = new ArrayList<>(Collections.nCopies(mediaFiles.size(), MIN_WIDTH));
        this.inflater = LayoutInflater.from(context);
        this.layoutManager = layoutManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.mediafile_lineitem, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaFile mediaFile = mediaFiles.get(position);
        Uri previewUri = mediaFile.getPreviewUri();

        LocalTime durationTime = mediaFile.getDuration();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
        String duration = durationTime.format(formatter);

        Glide.with(holder.itemView.getContext())
                .load(previewUri)
                .into(holder.previewImageView);
        holder.itemDuration.setText(duration);
    }

    @Override
    public int getItemCount() {
        return mediaFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemDuration;
        public ImageView previewImageView;
        public CustomMediaLineLayout customMediaLineLayout;

        public ViewHolder(View itemView, MediaLineAdapter adapter) {
            super(itemView);
            previewImageView = itemView.findViewById(R.id.MediaLineItem);
            itemDuration = itemView.findViewById(R.id.item_duration);
            customMediaLineLayout = (CustomMediaLineLayout) itemView;
            customMediaLineLayout.setLayoutManager(adapter.layoutManager); // Set layoutManager
            customMediaLineLayout.setOnWidthChangeListener((view, newWidth) -> adapter.layoutManager.resizeItem(view, newWidth));

            itemDuration.setOnClickListener(v -> {
                // Создаем EditText для ввода длительности
                EditText input = new EditText(v.getContext());
                input.setHint("HH:mm:ss:SSS"); // Подсказка для ввода

                // Создаем AlertDialog
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Введите длительность")
                        .setMessage("Введите длительность в формате HH:mm:ss:SSS")
                        .setView(input)
                        .setPositiveButton("OK", (dialog, which) -> {
                            String durationInput = input.getText().toString();
                            if (isValidDuration(durationInput)) {
                                Toast.makeText(v.getContext(), durationInput, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(v.getContext(), "Некорректный формат", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Отмена", (dialog, which) -> dialog.cancel())
                        .show();
            });
        }
        private boolean isValidDuration(String duration) {
            // Регулярное выражение для проверки формата HH:mm:ss:SSS
            String regex = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d):([0-9]{3})$";
            return duration.matches(regex);
        }
    }

}

