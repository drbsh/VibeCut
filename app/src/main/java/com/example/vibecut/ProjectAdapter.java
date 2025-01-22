package com.example.vibecut;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private List<ProjectInfo> projects;
    private final Context context;
    private boolean isDarkTheme;


    public ProjectAdapter(Context context, List<ProjectInfo> projects, boolean isDarkTheme) {
        this.context = context;
        this.projects = projects;
        this.inflater = LayoutInflater.from(context);
        this.isDarkTheme = isDarkTheme;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_view_item, parent, false);
        return new ViewHolder(view);

    }
    @Override
    public void onBindViewHolder(ProjectAdapter.ViewHolder holder, int position) {
        ProjectInfo project = projects.get(position);
        if (project != null)
        {
            if (isDarkTheme) {
                holder.itemView.setBackgroundResource(R.drawable.rounded_corners_item_project_dark);
                holder.nameView.setTextColor(context.getResources().getColor(R.color.white)); // Цвет текста для темной темы
                holder.durationView.setTextColor(context.getResources().getColor(R.color.white)); // Цвет текста для длительности
                holder.dateView.setTextColor(context.getResources().getColor(R.color.white)); // Цвет текста для даты
            } else {
                holder.itemView.setBackgroundResource(R.drawable.rounded_corners_item_project);
                holder.nameView.setTextColor(context.getResources().getColor(R.color.black)); // Цвет текста для светлой темы
                holder.durationView.setTextColor(context.getResources().getColor(R.color.black)); // Цвет текста для длительности
                holder.dateView.setTextColor(context.getResources().getColor(R.color.black)); // Цвет текста для даты
            }

            Glide.with(holder.itemView.getContext())
                    .load(project.getPreview())
                    .into(holder.previewView);//ставим предпросмотр Uri
            holder.nameView.setText(project.getName());
        }
        else{
            return;
        }

        // Форматирование времени
        LocalTime duration = project.getDurarion();
        String formattedDuration;

        if (duration.getHour() > 0) {
            formattedDuration = duration.format(DateTimeFormatter.ofPattern("HH:mm:ss")); // HH:mm:ss
        } else {
            formattedDuration = String.format("%02d:%02d", duration.getMinute(), duration.getSecond()); // mm:ss
        }

        holder.durationView.setText(formattedDuration);

        // Форматирование даты
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = project.getDate().format(dateFormatter);
        holder.dateView.setText(formattedDate);

        //проверка избранного
        boolean isFavourite = project.getFavourite();
        holder.favouriteButton.setBackgroundResource(isFavourite ? R.drawable.save_star_favourite_full : R.drawable.save_star_favourite_empty);

        //открытие окна редактирования
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(inflater.getContext(), EditerActivity.class);
            intent.putExtra("project_info", project);
            context.startActivity(intent);
        });
    }
    public void setDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView previewView;
        final TextView nameView, durationView, dateView;
        private ImageButton favouriteButton, deleteButton;
        ViewHolder(View view){
            super(view);
            previewView = view.findViewById(R.id.video_preview);
            nameView = view.findViewById(R.id.video_title);
            durationView = view.findViewById(R.id.video_duration);
            dateView = view.findViewById(R.id.video_date);
            favouriteButton = itemView.findViewById(R.id.change_favourite);
            deleteButton = itemView.findViewById(R.id.delete_project);

            favouriteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ProjectInfo projectInfo = projects.get(position);
                    // Переключаем состояние избранного
                    boolean newFavouriteState = !projectInfo.getFavourite();
                    projectInfo.setFavourite(newFavouriteState);

                    // Обновляем иконку кнопки
                    favouriteButton.setBackgroundResource(newFavouriteState ? R.drawable.save_star_favourite_full : R.drawable.save_star_favourite_empty);
                    JSONHelper.exportToJSON(itemView.getContext(), projectInfo);
                    // Опционально, показать сообщение
                    String message = newFavouriteState ? "Добавлено в избранное" : "Удалено из избранного";
                    Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });

            //обработка удалить
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ProjectInfo projectInfo = projects.get(position);

                    // Создаем диалог подтверждения
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Подтверждение удаления")
                            .setMessage("Вы уверены, что хотите удалить проект: " + projectInfo.getName() + "?")
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Удаляем папку проекта
                                    ProjectInfo projectToDelete =  projects.get(position);
                                    File directory = new File(itemView.getContext().getFilesDir(), "VibeCutProjects" + "/" + projectToDelete.getIdProj());
                                    deleteDirectory(directory);

                                    projects.remove(position);
                                    notifyItemRemoved(position);
                                    // Показываем сообщение об удалении
                                    String message = "Проект: " + projectInfo.getName() + " удален.";
                                    Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Нет", null) // Ничего не делаем, если пользователь нажал "Нет"
                            .show();
                }
            });
        }
    }
    private boolean deleteDirectory(File directory) {
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file); // Рекурсивно удаляем содержимое
                }
            }
        }
        return directory.delete(); // Удаляем саму директорию
    }
}
