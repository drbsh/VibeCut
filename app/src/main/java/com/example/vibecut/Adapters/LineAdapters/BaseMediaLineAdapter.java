package com.example.vibecut.Adapters.LineAdapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.vibecut.Adapters.CountTimeAndWidth;
import com.example.vibecut.CustomizeProject.BaseCustomLineLayout;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.Models.ProjectInfo;
import com.example.vibecut.R;
import com.example.vibecut.ViewModels.TimePickerDialog;

import java.io.File;
import java.time.Duration;
import java.util.List;

public abstract class BaseMediaLineAdapter implements TimePickerDialog.OnTimeSetListener {
    protected ProjectInfo projectInfo;
    protected List<MediaFile> mediaFiles;
    protected final RelativeLayout mediaLineContainer;
    protected final Context context;
    protected final AppCompatActivity activity;
    protected final HorizontalScrollView horizontalScrollView;

    public BaseMediaLineAdapter(HorizontalScrollView horizontalScrollView, RelativeLayout mediaLineContainer, List<MediaFile> mediaFiles, ProjectInfo projectInfo, Context context, AppCompatActivity activity) {
        this.mediaLineContainer = mediaLineContainer;
        this.mediaFiles = mediaFiles;
        this.projectInfo = projectInfo;
        this.context = context;
        this.activity = activity;
        this.horizontalScrollView = horizontalScrollView;
        populateMediaItems(); // Заполнение контейнера элементами
    }

    public void updateWithSwitchPositions(BaseCustomLineLayout customLineLayout, int targetPosition) {
        int currentPosition = customLineLayout.getOriginalPosition();
        MediaFile file = mediaFiles.get(currentPosition);
        mediaFiles.set(currentPosition, mediaFiles.get(targetPosition));
        mediaFiles.set(targetPosition, file);
        populateMediaItems();
    }

    public void InflateToCustomMediaLineLayout(BaseCustomLineLayout customLineLayout) {
        LayoutInflater.from(customLineLayout.getContext()).inflate(R.layout.mediafile_lineitem, customLineLayout, true);
    }

    public void notifyItemInserted() {
        populateMediaItems();
    }

    public void pullingInfoCustomLayout(MediaFile mediaFile, BaseCustomLineLayout newcustomMediaLineLayout) {
        onBindViewHolder(mediaFile, newcustomMediaLineLayout);
    }

    protected void populateMediaItems() {
        BaseCustomLineLayout previous = null;

        mediaLineContainer.removeAllViews(); // Очистка контейнера перед добавлением новых элементов
        for (int i = 0; i < mediaFiles.size(); i++) {
            MediaFile mediaFile = mediaFiles.get(i);
            boolean isFirst = (i == 0);// Проверка, является ли элемент первым или последним
            boolean isEnd = (i == mediaFiles.size() - 1);
            previous = AddItemMedia(i, mediaFile, isFirst, isEnd, previous);// Добавляем элемент
        }
    }

    protected abstract BaseCustomLineLayout AddItemMedia(int index, MediaFile mediaFile, Boolean isFirst, Boolean isEnd, BaseCustomLineLayout previous);

    public void onBindViewHolder(MediaFile mediaFile, BaseCustomLineLayout customLineLayout) {
        TextView itemDuration = customLineLayout.findViewById(R.id.item_duration);
        ImageView mediaLineItem = customLineLayout.findViewById(R.id.MediaLineItem);

        // Устанавливаем длительность
        Duration durationTime = mediaFile.getDuration();

        String duration = CountTimeAndWidth.formatDurationToString(durationTime);
        itemDuration.setText(duration);

        // Загружаем изображение с помощью Glide
        Uri previewUri = mediaFile.getPreviewMedia();
        Glide.with(customLineLayout.getContext())
                .load(previewUri)
                .into(mediaLineItem);

        // Устанавливаем обработчик нажатия
        itemDuration.setOnClickListener(v -> {
            List<Integer> currentTime = getCurrentDuration(itemDuration.getText().toString());

            // Создаем экземпляр TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(currentTime, mediaFile);

            // Показываем диалог
            timePickerDialog.show(activity.getSupportFragmentManager(), "timePicker");
            timePickerDialog.setOnTimeSetListener(this);

        });

    }

    public void notifyItemRemoved(int index) {
        if (index >= 0 && index < mediaFiles.size()) {
            File filedel = new File(mediaFiles.get(index).getPathToEditedFile().getPath());
            filedel.delete();
            filedel = new File(mediaFiles.get(index).getPathToOriginalFile().getPath());
            filedel.delete();
            mediaFiles.remove(index); // Удаляем элемент из списка
            mediaLineContainer.removeViewAt(index); // Удаляем элемент из контейнера
            populateMediaItems();
        }
    }

    private List<Integer> getCurrentDuration(String durationString) {
        String[] parts = durationString.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int millis = Integer.parseInt(parts[3]);
        return List.of(hours, minutes, seconds, millis);
    }

    @Override
    public void onTimeSet() {
        this.mediaFiles = projectInfo.getProjectFiles();
        populateMediaItems();
    }
}