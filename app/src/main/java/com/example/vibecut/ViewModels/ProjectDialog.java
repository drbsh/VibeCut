package com.example.vibecut.ViewModels;

    import static android.app.Activity.RESULT_OK;

    import android.app.AlertDialog;
    import android.app.Dialog;
    import android.content.ClipData;
    import android.content.Context;
    import android.content.Intent;
    import android.database.Cursor;
    import android.graphics.Bitmap;
    import android.media.MediaMetadataRetriever;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.OpenableColumns;
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

    import com.example.vibecut.Adapters.MediaAdapter;
    import com.example.vibecut.Models.MediaFile;
    import com.example.vibecut.Models.ProjectInfo;
    import com.example.vibecut.R;

    import org.apache.commons.io.FilenameUtils;

    import java.io.File;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.time.LocalDateTime;
    import java.time.LocalTime;
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
            projectInfo.setName(nameProject.getText().toString());
            projectInfo.setProjectFiles(mediaFiles);
            projectInfo.setDate(LocalDateTime.now());

            List<ProjectInfo> savedProjects = MainActivity.getProjectList();

            if (listener != null) {
                if(projectInfo.getProjectFiles().size() == 0)
                {
                    Toast.makeText(getActivity(), "Пожалуйста, выберите хотя бы один медиафайл.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(projectInfo.getName() == null || projectInfo.getName().isEmpty()) {
                    Toast.makeText(getActivity(), "Пожалуйста, заполните название проекта.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
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
                    processingFile(item);
                }
            } else if (data.getData() != null) {
                // Если выбран только один файл
                processingFile(data.getData());
            }
        }
    }

    // Обработка каждого файла
    private void processingFile(ClipData.Item item) {
        Uri selectedMediaUri = item.getUri(); // Получаем Uri из ClipData.Item
        processUri(selectedMediaUri);
    }

    private void processingFile(Uri selectedMediaUri) {
        processUri(selectedMediaUri);
    }

    private void processUri(Uri selectedMediaUri) {
        // Получаем имя файла из Uri
        String fileName = getFileName(selectedMediaUri);
        String mimeType = requireContext().getContentResolver().getType(selectedMediaUri);
        Uri preview = getPreview(mimeType, selectedMediaUri);
        String typeMedia = "";
        LocalTime duration = LocalTime.of(0,0, 0, 0);
        if (mimeType.startsWith("image/")) {
            typeMedia = "img";
            duration = LocalTime.of(0, 0,3,0);
        } else if (mimeType.startsWith("video/")){
            typeMedia = "video";
            try {
                duration = getVideoDuration(selectedMediaUri);
            } catch (IOException e) {
                e.printStackTrace(); // Логируем ошибку
                Toast.makeText(requireContext(), "Не удалось получить длительность видео.", Toast.LENGTH_SHORT).show();
            }
        }
        MediaFile mediaFile = new MediaFile(fileName, preview, selectedMediaUri, duration, typeMedia);
        // Добавляем MediaFile в проект
        projectInfo.addMediaFile(mediaFile);
        mediaFile.setWidthOnTimeline(100);
        // Добавляем MediaFile в список
        mediaFiles.add(mediaFile);
        // Уведомляем адаптер об изменении данных
        mediaAdapter.notifyItemInserted(mediaFiles.size() - 1);
    }


    // Получение предпросмотра
    private Uri getPreview(String mimeType, Uri selectedMediaUri) {
        Uri preview = null;
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                preview = selectedMediaUri;
            } else if (mimeType.startsWith("video/")) {
                try {
                    preview = getVideoThumbnail(selectedMediaUri); // Метод для получения миниатюры видео
                } catch (IOException e) {
                    e.printStackTrace(); // Логируем ошибку
                    Toast.makeText(requireContext(), "Не удалось получить миниатюру видео.", Toast.LENGTH_SHORT).show();
                    return preview;
                }
            } else {
                Toast.makeText(requireContext(), "Выбранный файл не является изображением или видео.", Toast.LENGTH_SHORT).show();
                return preview;
            }
        }
        return preview;
    }

    // Метод для получения имени файла из Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) { // Проверяем, что индекс не -1
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            // Если имя не найдено, пробуем извлечь его из пути
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    //метод для сохранения миниатюры видео в папку проекта
    private Uri savePreviewToFolderProject(String prName, Bitmap img, String nameFile){
        File folder = new File(context.getFilesDir(), "VibeCutProjects/" + prName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Создаем файл для сохранения миниатюры
        File file = new File(folder, nameFile + ".png"); // Вы можете изменить имя файла по своему усмотрению

        try (FileOutputStream out = new FileOutputStream(file)) {
            img.compress(Bitmap.CompressFormat.PNG, 100, out); // Сохраняем изображение в формате PNG
        } catch (IOException e) {
            e.printStackTrace(); // Обработка ошибок
        }
        return Uri.fromFile(file);
    }

    //Метод для получения миниатюры видео
    private Uri getVideoThumbnail(Uri uri) throws IOException{
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(requireContext(), uri);
        Bitmap bitmap = retriever.getFrameAtTime(0); // Получаем первый кадр
        retriever.release();
        return savePreviewToFolderProject(projectInfo.getName(), bitmap, FilenameUtils.removeExtension(getFileName(uri)));
    }
    private LocalTime getVideoDuration(Uri videoUri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(requireContext(), videoUri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long durationInMillis = Long.parseLong(time);
        retriever.release();

        // Преобразуем длительность в LocalTime
        return LocalTime.ofNanoOfDay(durationInMillis * 1_000_000);
    }

}
