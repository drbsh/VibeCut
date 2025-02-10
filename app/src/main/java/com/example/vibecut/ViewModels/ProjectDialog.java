package com.example.vibecut.ViewModels;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibecut.Adapters.FillingMediaFile;
import com.example.vibecut.Adapters.MediaAdapter;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.Models.ProjectInfo;
import com.example.vibecut.R;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectDialog extends DialogFragment {
    private RecyclerView listMediaView;
    private MediaAdapter mediaAdapter;

    private ProjectInfo projectInfo;
    private File projectFolder;
    private EditText nameProject;
    private ProjectDialogListener listener;
    private List<MediaFile> mediaFiles = new ArrayList<MediaFile>(); // Список для хранения выбранных медиафайлов
    private Context context;
    private static final int PICK_MEDIA_REQUEST = 1;
    private boolean isDarkTheme;
    private FillingMediaFile fillingMediaFile;
    private boolean isDialogClosedByUser = true;

    public interface ProjectDialogListener {
        void onProjectSaved(ProjectInfo projectInfo);
    }

    public ProjectDialog(File projectFolder, ProjectInfo projectInfo, boolean isDarkTheme) {
        this.projectFolder = projectFolder;
        this.projectInfo = projectInfo;
        this.isDarkTheme = isDarkTheme;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        // Проверяем, реализует ли контекст интерфейс ProjectDialogListener
        if (context instanceof ProjectDialogListener) {
            listener = (ProjectDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " должен реализовать ProjectDialogListener");
        }
    }

    private void updateTheme(View view, Boolean isDarkTheme) {
        LinearLayout linearLayout = view.findViewById(R.id.dialog_project);
        TextView nameMedia = view.findViewById(R.id.nameMedia);
        EditText projectName = view.findViewById(R.id.projectName);
        TextView mediaFilesLabel = view.findViewById(R.id.mediaFilesLabel);
        RelativeLayout selectMediaButton = view.findViewById(R.id.selectMediaButton);
        RelativeLayout saveProjectButton = view.findViewById(R.id.saveProjectButton);
        TextView saveProjectText = view.findViewById(R.id.saveProjectText);
        TextView selectMediaText = view.findViewById(R.id.selectMediaText);

        if (isDarkTheme) {
            linearLayout.setBackgroundResource(R.drawable.layout_project_dialog_background_black);
            nameMedia.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
            projectName.setHintTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
            mediaFilesLabel.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
            selectMediaButton.setBackgroundResource(R.drawable.rounded_button_background_dark);
            saveProjectButton.setBackgroundResource(R.drawable.rounded_button_background_dark);
            saveProjectText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
            selectMediaText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        } else {
            linearLayout.setBackgroundResource(R.drawable.layout_project_dialog_background);
            nameMedia.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
            projectName.setHintTextColor(ContextCompat.getColor(view.getContext(), R.color.gray2));
            mediaFilesLabel.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
            selectMediaButton.setBackgroundResource(R.drawable.project_dialog_rounded_button_background_gray);
            saveProjectButton.setBackgroundResource(R.drawable.project_dialog_rounded_button_background_gray);
            saveProjectText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
            selectMediaText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_project, null);
        listMediaView = view.findViewById(R.id.listMedia);

        mediaAdapter = new MediaAdapter(context, mediaFiles);
        listMediaView.setAdapter(mediaAdapter);
        listMediaView.setLayoutManager(new LinearLayoutManager(context));

        builder.setView(view);
        nameProject = view.findViewById(R.id.projectName);
        View addMedia = view.findViewById(R.id.selectMediaButton);
        View saveButton = view.findViewById(R.id.saveProjectButton);
        ImageButton favouriteButton = view.findViewById(R.id.change_favourite);

        //текущее знач


        // Вызовите updateTheme для установки начальной темы
        updateTheme(view, isDarkTheme);
        // Инициализация полей ввода

        addMedia.setOnClickListener(v -> {
            File folder = new File(context.getFilesDir(), "VibeCutProjects/" + projectInfo.getIdProj());
            if (!folder.exists()) {
                folder.mkdirs();
            }
            pickMedia();
        });

        // Обработчик для кнопки "Избранное"
        favouriteButton.setOnClickListener(v -> {
            boolean newFavouriteState = !projectInfo.getFavourite();
            projectInfo.setFavourite(newFavouriteState);

            favouriteButton.setBackgroundResource(newFavouriteState ? R.drawable.save_star_favourite_full : R.drawable.save_star_favourite_empty);
            String message = newFavouriteState ? "Добавлено в избранное" : "Удалено из избранного";
            Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();

        });

        // Обработчик для кнопки "Сохранить"
        saveButton.setOnClickListener(v -> {
            isDialogClosedByUser = false; // чтобы не выполнялся dismiss
            projectInfo.setName(nameProject.getText().toString());
            projectInfo.setProjectFiles(mediaFiles);
            projectInfo.setDate(LocalDateTime.now());

            List<ProjectInfo> savedProjects = MainActivity.getProjectList();

            if (listener != null) {
                if (projectInfo.getProjectFiles().size() == 0) {
                    Toast.makeText(getActivity(), "Пожалуйста, выберите хотя бы один медиафайл.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (projectInfo.getName() == null || projectInfo.getName().isEmpty()) {
                    Toast.makeText(getActivity(), "Пожалуйста, заполните название проекта.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    for (ProjectInfo savedProject : savedProjects) {
                        if (savedProject.getName().equals(projectInfo.getName())) {
                            Toast.makeText(getActivity(), "Проект с именем " + projectInfo.getName() + " уже существует.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                listener.onProjectSaved(projectInfo);
            }

            dismiss(); // Закрываем диалог
        });

        // Обработчик для кнопки "Отмена"
        builder.setNegativeButton("Отмена", (dialog, id) -> {
            ProjectDialog.this.getDialog().cancel();
        });
        fillingMediaFile = new FillingMediaFile(requireContext(), projectInfo, mediaFiles, mediaAdapter);

        return builder.create();
    }

    private void pickMedia() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*"); // Выбор изображений и видео
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_MEDIA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Если выбрано несколько файлов
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    ClipData.Item item = data.getClipData().getItemAt(i);
                    fillingMediaFile.processingFile(item);
                }
            } else if (data.getData() != null) {
                // Если выбран только один файл
                fillingMediaFile.processingFile(data.getData());
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // Ваши действия при закрытии диалогового окна
        if (isDialogClosedByUser) {
            File folder = new File(String.format("%s/%s", projectFolder, projectInfo.getIdProj()));
            if (folder.exists()) {
                // Удаляем все файлы и папки внутри folder
                if (deleteDirectory(folder)) {
                    Toast.makeText(context, "Папка проекта успешно удалена.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Не удалось удалить папку проекта.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Метод для рекурсивного удаления папки
    private boolean deleteDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child); // Рекурсивно удаляем содержимое
                }
            }
        }
        return dir.delete(); // Удаляем саму папку
    }
}
