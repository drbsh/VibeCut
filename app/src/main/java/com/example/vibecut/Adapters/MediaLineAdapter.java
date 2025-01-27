    package com.example.vibecut.Adapters;

    import static android.view.View.inflate;
    import static java.security.AccessController.getContext;

    import android.content.Context;
    import android.net.Uri;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.RelativeLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.example.vibecut.CustomizeProject.CustomLayoutManager;
    import com.example.vibecut.CustomizeProject.CustomMediaLineLayout;
    import com.example.vibecut.Models.MediaFile;
    import com.example.vibecut.R;
    import com.example.vibecut.ViewModels.TimePickerDialog;

    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.List;

    public class MediaLineAdapter {
        private MediaFile mediaFile;
        private CustomLayoutManager layoutManager;

        private List<MediaFile> mediaFiles;
        private List<Integer> mediaFileWidths;
        private LayoutInflater inflater;
        private LinearLayout mediaLineContainer;
        private Context context;
        private AppCompatActivity activity;
        private static final int MIN_WIDTH = 100; // Минимальная ширина элемента

        public void InflateToCustomMediaLineLayout(CustomMediaLineLayout customMediaLineLayout) {
            LayoutInflater.from(customMediaLineLayout.getContext()).inflate(R.layout.mediafile_lineitem, customMediaLineLayout, true);
        }

        public MediaLineAdapter(LinearLayout mediaLineContainer, List<MediaFile> mediaFiles, CustomLayoutManager layoutManager, Context context, AppCompatActivity activity) {
            this.mediaLineContainer = mediaLineContainer;
            this.mediaFiles = mediaFiles;
            this.layoutManager = layoutManager;
            this.context = context;
            this.activity = activity;
            populateMediaItems(); // Заполнение контейнера элементами
        }

        public void pullingInfoCustomLayout(MediaFile mediaFile, CustomMediaLineLayout newcustomMediaLineLayout) {
            onBindViewHolder(mediaFile, newcustomMediaLineLayout);
        }

        private void populateMediaItems() {
            mediaLineContainer.removeAllViews(); // Очистка контейнера перед добавлением новых элементов
            for (int i = 0; i < mediaFiles.size(); i++) {
                MediaFile mediaFile = mediaFiles.get(i);

                boolean isFirst = (i == 0);// Проверка, является ли элемент первым или последним
                boolean isEnd = (i == mediaFiles.size() - 1);
                AddImem(mediaFile, isFirst, isEnd);// Добавляем элемент
            }
        }

        private void AddImem(MediaFile mediaFile, Boolean isFirst, Boolean isEnd) {
            CustomMediaLineLayout customMediaLineLayout = new CustomMediaLineLayout(mediaLineContainer.getContext(), null);
            InflateToCustomMediaLineLayout(customMediaLineLayout);
            customMediaLineLayout.setMediaFile(mediaFile); // Важное изменение: устанавливаем MediaFile
            pullingInfoCustomLayout(mediaFile, customMediaLineLayout);
            customMediaLineLayout.setLayoutManager(layoutManager);
            mediaLineContainer.addView(customMediaLineLayout);
            // Устанавливаем отступы
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            if (isFirst) {
                // Устанавливаем отступ слева в 150dp для первого элемента
                int leftMargin = (int) context.getResources().getDimension(R.dimen.margin_150dp);
                params.setMargins(leftMargin, 0, 10, 0); // Отступ слева
            } else if (isEnd) {
                // Устанавливаем отступ справа в 150dp для остальных элементов
                int rightMargin = (int) context.getResources().getDimension(R.dimen.margin_150dp);
                params.setMargins(0, 0, rightMargin, 0); // Отступ справа
            } else {
                // Устанавливаем отступ снизу в 10dp для остальных элементов
                params.setMargins(0, 0, 10, 0); // Отступ справа
            }

            customMediaLineLayout.setLayoutParams(params);

            // Проверяем, есть ли у customMediaLineLayout родитель
            ViewGroup parent = (ViewGroup) customMediaLineLayout.getParent();
            if (parent != null) {
                parent.removeView(customMediaLineLayout); // Удаляем из родителя, если он есть
            }
            mediaLineContainer.addView(customMediaLineLayout);
        }


        public void onBindViewHolder(MediaFile mediaFile, CustomMediaLineLayout customMediaLineLayout) {
            TextView itemDuration = customMediaLineLayout.findViewById(R.id.item_duration);
            ImageView mediaLineItem = customMediaLineLayout.findViewById(R.id.MediaLineItem);

            // Устанавливаем длительность
            LocalTime durationTime = mediaFile.getDuration();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
            String duration = durationTime.format(formatter);
            itemDuration.setText(duration);

            // Загружаем изображение с помощью Glide
            Uri previewUri = mediaFile.getPreviewUri();
            Glide.with(customMediaLineLayout.getContext())
                    .load(previewUri)
                    .into(mediaLineItem);

            // Устанавливаем обработчик нажатия
            itemDuration.setOnClickListener(v -> {
                List<Integer> currentTime = getCurrentDuration(duration);

                // Создаем экземпляр TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(currentTime, mediaFile);

                // Показываем диалог
                timePickerDialog.show(activity.getSupportFragmentManager(), "timePicker");
            });

        }
        public void notifyItemRemoved(int index) {
            if (index >= 0 && index < mediaFiles.size()) {
                mediaFiles.remove(index); // Удаляем элемент из списка
                mediaLineContainer.removeViewAt(index); // Удаляем элемент из контейнера
                populateMediaItems();
            }
        }

        public void notifyItemInserted(int index) {
            // Создаем новый элемент MediaFile (или получаем его из другого источника)
            MediaFile newMediaFile = mediaFiles.get(index); // Получаем элемент из списка

            populateMediaItems();
        }
        private List<Integer> getCurrentDuration(String durationString){
            String[] parts = durationString.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            int millis = Integer.parseInt(parts[3]);
            return List.of(hours, minutes, seconds, millis);
        }
    }


