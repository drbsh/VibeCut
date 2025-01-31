package com.example.vibecut.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.R;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder>{
    private final LayoutInflater inflater;
    private static List<MediaFile> mediaFiles;

    public MediaAdapter(Context context, List<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public MediaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mediafiles_item, parent, false);
        return new MediaAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MediaAdapter.ViewHolder holder, int position) {
        MediaFile mediaFile = mediaFiles.get(position);
        holder.nameMedia.setText(mediaFile.getNameFile());
        Uri previewUri = mediaFile.getPreviewMedia();
        Glide.with(holder.itemView.getContext())
                .load(previewUri)
                .into(holder.preview);
    }

    @Override
    public int getItemCount() {
        return mediaFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameMedia;
        final ImageView preview;
        private ImageButton deleteMedia;
        ViewHolder(View view){
            super(view);
            nameMedia = view.findViewById(R.id.nameMedia);
            preview = view.findViewById(R.id.previewMedia);
            deleteMedia = view.findViewById(R.id.delete_media);

            deleteMedia.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MediaFile mediaFile = mediaFiles.get(position);
                    mediaFiles.remove(mediaFile);
                    notifyItemRemoved(position);
                    Toast.makeText(itemView.getContext(), "Файл успешно удален из проекта.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
