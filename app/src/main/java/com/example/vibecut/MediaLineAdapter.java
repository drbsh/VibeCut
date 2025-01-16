package com.example.vibecut;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MediaLineAdapter extends RecyclerView.Adapter<MediaLineAdapter.ViewHolder>{
    private List<MediaFile> mediaFiles;
    private final LayoutInflater inflater;

    public MediaLineAdapter(Context context, List<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mediafile_lineitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MediaLineAdapter.ViewHolder holder, int position) {
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

        public ViewHolder(View view) {
            super(view);
            previewImageView = view.findViewById(R.id.MediaLineItem);
        }
    }
}
