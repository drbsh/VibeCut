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
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.example.vibecut.CustomizeProject.CustomLayoutManager;
    import com.example.vibecut.CustomizeProject.CustomMediaLineLayout;
    import com.example.vibecut.Models.MediaFile;
    import com.example.vibecut.R;

    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.List;

    public class MediaLineAdapter  {
        private MediaFile mediaFile;
        private CustomLayoutManager layoutManager;

        private List<MediaFile> mediaFiles;
        private List<Integer> mediaFileWidths;
        private LayoutInflater inflater;
        private LinearLayout mediaLineContainer;
        private Context context;

        private static final int MIN_WIDTH = 100; // Минимальная ширина элемента

        public void InflateToCustomMediaLineLayout( CustomMediaLineLayout customMediaLineLayout) {
            LayoutInflater.from(customMediaLineLayout.getContext()).inflate(R.layout.mediafile_lineitem, customMediaLineLayout, true);
        }

        public MediaLineAdapter(LinearLayout mediaLineContainer, List<MediaFile> mediaFiles, CustomLayoutManager layoutManager, Context context) {
            this.mediaLineContainer = mediaLineContainer;
            this.mediaFiles = mediaFiles;
            this.layoutManager = layoutManager;
            this.context = context;

            populateMediaItems(); // Заполнение контейнера элементами
        }

        public void pullingInfoCustomLayout(MediaFile mediaFile, CustomMediaLineLayout newcustomMediaLineLayout) {
            this.mediaFile = mediaFile;
            CustomMediaLineLayout customMediaLineLayout = newcustomMediaLineLayout;
            onBindViewHolder(customMediaLineLayout);
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
        private void AddImem(MediaFile mediaFile, Boolean isFirst, Boolean isEnd){
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
            }else{
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


        public void onBindViewHolder(CustomMediaLineLayout customMediaLineLayout) {
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


//    public int getItemCount() {
//        return mediaFiles.size();
//    }
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        public TextView itemDuration;
//        public ImageView previewImageView;
//        public CustomMediaLineLayout customMediaLineLayout;
//
//        public ViewHolder(View itemView, MediaLineAdapter adapter) {
//            super(itemView);
//            previewImageView = itemView.findViewById(R.id.MediaLineItem);
//            itemDuration = itemView.findViewById(R.id.item_duration);
//            customMediaLineLayout = (CustomMediaLineLayout) itemView;
//            customMediaLineLayout.setLayoutManager(adapter.layoutManager); // Set layoutManager
//            customMediaLineLayout.setOnWidthChangeListener((view, newWidth) -> adapter.layoutManager.resizeItem(view, newWidth));
//
//            itemDuration.setOnClickListener(v -> {
//                // Создаем EditText для ввода длительности
//                EditText input = new EditText(v.getContext());
//                input.setHint("HH:mm:ss:SSS"); // Подсказка для ввода
//
//                // Создаем AlertDialog
//                new AlertDialog.Builder(v.getContext())
//                        .setTitle("Введите длительность")
//                        .setMessage("Введите длительность в формате HH:mm:ss:SSS")
//                        .setView(input)
//                        .setPositiveButton("OK", (dialog, which) -> {
//                            String durationInput = input.getText().toString();
//                            if (isValidDuration(durationInput)) {
//                                Toast.makeText(v.getContext(), durationInput, Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(v.getContext(), "Некорректный формат", Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .setNegativeButton("Отмена", (dialog, which) -> dialog.cancel())
//                        .show();
//            });
//        }
//        private boolean isValidDuration(String duration) {
//            // Регулярное выражение для проверки формата HH:mm:ss:SSS
//            String regex = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d):([0-9]{3})$";
//            return duration.matches(regex);
//        }
//    }

}
//    private List<MediaFile> mediaFiles; // Список медиафайлов
//    private LinearLayout mediaLineContainer; // Ссылка на LinearLayout
//
//    public MediaLineAdapter(LinearLayout mediaLineContainer, List<MediaFile> mediaFiles) {
//        this.mediaLineContainer = mediaLineContainer;
//        this.mediaFiles = mediaFiles;
//    }
//
//    public void updateViews() {
//        mediaLineContainer.removeAllViews(); // Очистите контейнер перед добавлением новых элементов
//
//        for (MediaFile mediaFile : mediaFiles) {
//            CustomMediaLineLayout customLayout = new CustomMediaLineLayout(mediaLineContainer.getContext(), null);
//            customLayout.getmedia(mediaFiles);
//            customLayout.setmedia(customLayout.getId(R.id.MediaLineItem));
//            customLayout.setLayoutParams(new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT)); // Высота - match_parent для горизонтального расположения
//
//            // Здесь вы можете установить данные в customLayout, например:
//            // customLayout.setData(mediaFile);
//
//            mediaLineContainer.addView(customLayout);
//        }
//    }
//}

