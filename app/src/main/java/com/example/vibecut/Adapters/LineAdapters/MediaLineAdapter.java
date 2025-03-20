package com.example.vibecut.Adapters.LineAdapters;

import android.content.Context;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vibecut.CustomizeProject.BaseCustomLineLayout;
import com.example.vibecut.CustomizeProject.CustomMediaLineLayout;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.Models.ProjectInfo;
import com.example.vibecut.R;

import java.util.List;

public class MediaLineAdapter extends BaseMediaLineAdapter {

    public MediaLineAdapter(HorizontalScrollView horizontalScrollView, RelativeLayout mediaLineContainer, List<MediaFile> mediaFiles, ProjectInfo projectInfo, Context context, AppCompatActivity activity) {
        super(horizontalScrollView, mediaLineContainer, mediaFiles, projectInfo, context, activity);
    }

    @Override
    protected BaseCustomLineLayout AddItemMedia(int index, MediaFile mediaFile, Boolean isFirst, Boolean isEnd, BaseCustomLineLayout previous) {
        CustomMediaLineLayout customLineLayout = new CustomMediaLineLayout(mediaLineContainer.getContext(), null);
        customLineLayout.setProjectInfo(projectInfo);
        InflateToCustomMediaLineLayout(customLineLayout);
        pullingInfoCustomLayout(mediaFile, customLineLayout);

        //Инициализация и установка значений
        customLineLayout.init();
        customLineLayout.setParentLayout(mediaLineContainer);
        customLineLayout.setMediaFile(mediaFile);
        customLineLayout.setContext(context);
        customLineLayout.setOriginalPosition(index);
        customLineLayout.setVideoEditer();
        customLineLayout.setMaxWidth(mediaFile.getMaxDuration());
        customLineLayout.setHorizontalScrollView(horizontalScrollView);

        mediaLineContainer.addView(customLineLayout);

        // Устанавливаем отступы
        customLineLayout.setId(View.generateViewId());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        if (previous == null) {
            // Если это первый элемент, устанавливаем его к левому краю родительского контейнера
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            // Если это не первый элемент, размещаем его справа от предыдущего
            params.addRule(RelativeLayout.RIGHT_OF, previous.getId());
        }

        if (isFirst) {
            // Устанавливаем отступ слева в 150dp для первого элемента
            int rightMargin = (int) context.getResources().getDimension(R.dimen.margin_150dp);
            int leftMargin = (int) context.getResources().getDimension(R.dimen.margin_150dp);
            if (isEnd) {
                params.setMargins(leftMargin, 0, rightMargin, 0); // Отступ слева
            } else {
                params.setMargins(leftMargin, 0, 10, 0); // Отступ слева
            }

        } else if (isEnd) {
            // Устанавливаем отступ справа в 150dp для остальных элементов
            int rightMargin = (int) context.getResources().getDimension(R.dimen.margin_150dp);
            params.setMargins(0, 0, rightMargin, 0); // Отступ справа
        } else {
            // Устанавливаем отступ снизу в 10dp для остальных элементов
            params.setMargins(0, 0, 10, 0); // Отступ справа
        }

        customLineLayout.setLayoutParams(params);
        return customLineLayout;
    }
    @Override
    public void onTimeSet() {
        this.mediaFiles = projectInfo.getMediaFiles();
        populateMediaItems();
    }
}