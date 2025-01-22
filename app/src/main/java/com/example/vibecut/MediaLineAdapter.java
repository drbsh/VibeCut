package com.example.vibecut;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        Glide.with(holder.itemView.getContext())
                .load(previewUri)
                .into(holder.previewImageView);
    }

    @Override
    public int getItemCount() {
        return mediaFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView previewImageView;
        public CustomMediaLineLayout customMediaLineLayout;

        public ViewHolder(View itemView, MediaLineAdapter adapter) {
            super(itemView);
            previewImageView = itemView.findViewById(R.id.MediaLineItem);
            customMediaLineLayout = (CustomMediaLineLayout) itemView;
            customMediaLineLayout.setLayoutManager(adapter.layoutManager); // Set layoutManager
            customMediaLineLayout.setOnWidthChangeListener((view, newWidth) -> adapter.layoutManager.resizeItem(view, newWidth));
            // Удаляем вызов adapter.updateWidth
        }
    }
}

