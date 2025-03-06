    package com.example.vibecut.Adapters;

    import android.content.Context;
    import android.net.Uri;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.widget.HorizontalScrollView;
    import android.widget.ImageView;
    import android.widget.RelativeLayout;
    import android.widget.TextView;

    import androidx.appcompat.app.AppCompatActivity;

    import com.bumptech.glide.Glide;
    import com.example.vibecut.CustomizeProject.BaseCustomLineLayout;
    import com.example.vibecut.CustomizeProject.CustomMediaLineLayout;
    import com.example.vibecut.Models.MediaFile;
    import com.example.vibecut.Models.ProjectInfo;
    import com.example.vibecut.R;
    import com.example.vibecut.ViewModels.TimePickerDialog;

    import java.time.Duration;
    import java.util.List;

    public class MediaLineAdapter implements TimePickerDialog.OnTimeSetListener {
        private ProjectInfo projectInfo;
        private static List<MediaFile> mediaFiles;
        private final RelativeLayout mediaLineContainer;
        private final Context context;
        private final AppCompatActivity activity;
        private final HorizontalScrollView horizontalScrollView;
        private static final int MIN_WIDTH = 100; // Минимальная ширина элемента
        public MediaLineAdapter(HorizontalScrollView horizontalScrollView, RelativeLayout mediaLineContainer, List<MediaFile> mediaFiles, ProjectInfo projectInfo, Context context, AppCompatActivity activity) {
            this.mediaLineContainer = mediaLineContainer;
            this.mediaFiles = mediaFiles;
            this.projectInfo = projectInfo;
            this.context = context;
            this.activity = activity;
            this.horizontalScrollView = horizontalScrollView;
            populateMediaItems(); // Заполнение контейнера элементами
        }
        public void updateWithSwitchPositions(BaseCustomLineLayout customLineLayout, int targetPosition) {
            int currentPosition  = customLineLayout.getOriginalPosition();
            MediaFile file = mediaFiles.get(currentPosition);
            mediaFiles.set(currentPosition, mediaFiles.get(targetPosition));
            mediaFiles.set(targetPosition, file);
            populateMediaItems();
        }
        public void InflateToCustomMediaLineLayout(BaseCustomLineLayout customLineLayout) {
            LayoutInflater.from(customLineLayout.getContext()).inflate(R.layout.mediafile_lineitem, customLineLayout, true);
        }

        public void notifyItemInserted() {
            populateMediaItems();
        }

        public void pullingInfoCustomLayout(MediaFile mediaFile, BaseCustomLineLayout newcustomMediaLineLayout) {
            onBindViewHolder(mediaFile, newcustomMediaLineLayout);
        }

        private void populateMediaItems() {
//            CustomLayoutManager.idMediaItems = 0;
            BaseCustomLineLayout previous = null;

            mediaLineContainer.removeAllViews(); // Очистка контейнера перед добавлением новых элементов
            for (int i = 0; i < mediaFiles.size(); i++) {
                MediaFile mediaFile = mediaFiles.get(i);
                boolean isFirst = (i == 0);// Проверка, является ли элемент первым или последним
                boolean isEnd = (i == mediaFiles.size() - 1);
                previous = AddItemMedia(i, mediaFile, isFirst, isEnd, previous);// Добавляем элемент
            }
        }


        private CustomMediaLineLayout AddItemMedia(int index, MediaFile mediaFile, Boolean isFirst, Boolean isEnd, BaseCustomLineLayout previous) {
            CustomMediaLineLayout customLineLayout = new CustomMediaLineLayout(mediaLineContainer.getContext(), null);
            customLineLayout.setProjectInfo(projectInfo);
            InflateToCustomMediaLineLayout(customLineLayout);
            pullingInfoCustomLayout(mediaFile, customLineLayout);

            //Инициализация и установка значений
            customLineLayout.init();
            customLineLayout.setParentLayout(mediaLineContainer);
            customLineLayout.setMediaFile(mediaFile);
            customLineLayout.setContext(context);
            customLineLayout.setParentLayout(mediaLineContainer);
            customLineLayout.setOriginalPosition(index);
            customLineLayout.setVideoEditer();
            customLineLayout.setMaxWidth();
            customLineLayout.setHorizontalScrollView(horizontalScrollView);
;

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
                if(isEnd){
                    params.setMargins(leftMargin, 0, rightMargin, 0); // Отступ слева
                }
                else{
                    params.setMargins(leftMargin, 0, 10, 0); // Отступ слева
                }

            } else if (isEnd) {
                // Устанавливаем отступ справа в 150dp для остальных элементов
                int rightMargin = (int) context.getResources().getDimension(R.dimen.margin_150dp);
                params.setMargins(0, 0, rightMargin, 0); // Отступ справа
            }
            else {
                // Устанавливаем отступ снизу в 10dp для остальных элементов
                params.setMargins(0, 0, 10, 0); // Отступ справа
            }

            customLineLayout.setLayoutParams(params);
            return customLineLayout;
        }


        public void onBindViewHolder(MediaFile mediaFile, BaseCustomLineLayout customLineLayout) {
            TextView itemDuration = customLineLayout.findViewById(R.id.item_duration);
            ImageView mediaLineItem = customLineLayout.findViewById(R.id.MediaLineItem);

            // Устанавливаем длительность
            Duration durationTime = mediaFile.getDuration();

            String duration = CountTimeAndWidth.formatDurationToString(durationTime);
            itemDuration.setText(duration);

            // Загружаем изображение с помощью Glide
            Uri previewUri = mediaFile.getPreviewMedia();
            Glide.with(customLineLayout.getContext())
                    .load(previewUri)
                    .into(mediaLineItem);

            // Устанавливаем обработчик нажатия
            itemDuration.setOnClickListener(v -> {
                List<Integer> currentTime = getCurrentDuration(itemDuration.getText().toString());

                // Создаем экземпляр TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(currentTime, mediaFile);

                // Показываем диалог
                timePickerDialog.show(activity.getSupportFragmentManager(), "timePicker");
                timePickerDialog.setOnTimeSetListener(this);

            });

        }
        public void notifyItemRemoved(int index) {
            if (index >= 0 && index < mediaFiles.size()) {
                mediaFiles.remove(index); // Удаляем элемент из списка
                mediaLineContainer.removeViewAt(index); // Удаляем элемент из контейнера
                populateMediaItems();
            }
        }



        private List<Integer> getCurrentDuration(String durationString){
            String[] parts = durationString.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            int millis = Integer.parseInt(parts[3]);
            return List.of(hours, minutes, seconds, millis);
        }
        @Override
        public void onTimeSet() {
            this.mediaFiles = projectInfo.getProjectFiles();
            populateMediaItems();
        }
    }


