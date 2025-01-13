package com.example.vibecut;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder>{

    private List<MediaFile> mediaFiles;
    private Context context;

    public TimelineAdapter(Context context, List<MediaFile> mediaFiles) {
        this.context = context;
        this.mediaFiles = mediaFiles;
    }
    @Override
    public TimelineAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
        return new TimelineAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(TimelineAdapter.ViewHolder holder, int position) {
        MediaFile mediaFile = mediaFiles.get(position);
        Uri previewUri = mediaFile.getPreviewUri();
        Glide.with(holder.itemView.getContext())
                .load(previewUri)
                .into(holder.imageView);
    }


    @Override
    public int getItemCount() {
        return mediaFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}

