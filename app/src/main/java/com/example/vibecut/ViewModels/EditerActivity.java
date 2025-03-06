package com.example.vibecut.ViewModels;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.vibecut.Adapters.CountTimeAndWidth;
import com.example.vibecut.Adapters.EffectClickListener;
import com.example.vibecut.Adapters.FillingMediaFile;
import com.example.vibecut.JSONHelper;
import com.example.vibecut.Adapters.MediaLineAdapter;
import com.example.vibecut.Models.MediaFile;
import com.example.vibecut.Models.ProjectInfo;
import com.example.vibecut.R;
import java.time.Duration;
import java.util.List;

public class EditerActivity extends AppCompatActivity implements TimePickerDialog.TimePickerDialogListener {
    private static final int PICK_MEDIA_REQUEST = 1;
    private TextView nameProjectTextView;
    private HorizontalScrollView horizontalScrollView;
    private ProjectInfo projectInfo;//текущий  проект
    private List<MediaFile> MediaFiles;
    private static MediaLineAdapter adapter;
    private RelativeLayout mediaLineContainer;
    private RelativeLayout audioLineContainer;
    private CountTimeAndWidth countTimeAndWidth;
    private FillingMediaFile fillingMediaFile;
    private ImageButton buttonCuttingVideo;
    private ImageButton buttonTextImpose;
    private ImageButton buttonAddEffects;

    public static MediaLineAdapter getAdapter() {
        return adapter;
    }
    public ProjectInfo getProjectInfo(){
            return projectInfo;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editer_activity);

        new CountTimeAndWidth(this);// Устанавливаем контекст для статических функций класса

        horizontalScrollView = findViewById(R.id.scroll_all_media_line);
        mediaLineContainer = findViewById(R.id.media_line_container);
        audioLineContainer = findViewById(R.id.audio_line_container);

        if (getIntent() != null && getIntent().hasExtra("project_info")) {
            projectInfo = (ProjectInfo) getIntent().getSerializableExtra("project_info");

            // Инициализируем TextView
            nameProjectTextView = findViewById(R.id.nameProject);

            // Устанавливаем имя проекта в TextView
            if (projectInfo != null) {
                nameProjectTextView.setText(projectInfo.getName());
            }
        }

        MediaFiles = projectInfo.getProjectFiles();

        adapter = new MediaLineAdapter(horizontalScrollView, mediaLineContainer, MediaFiles, projectInfo, this, this); // Создаем адаптер

        // <<<<<<<<<<||||||||||||||||||||||||||||||||||||||||>>>>>>>>
//        MediaLineAdapter adapter1 = new MediaLineAdapter(audioLineContainer, MediaFiles, projectInfo, layoutManagerAudio, this, this);
        // УБЕРИ ЭТУ СТРОКУ ЧТОБЫ СККРЫТЬ НИЖНИЙ РЯД


        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkTheme = preferences.getBoolean("isDarkTheme", false);
        updateTheme(isDarkTheme);
        countTimeAndWidth = new CountTimeAndWidth(this);
        fillingMediaFile = new FillingMediaFile(this, adapter, projectInfo, MediaFiles);
    }

    private void updateTheme(boolean isDarkTheme) {
        LinearLayout headerLayout = findViewById(R.id.headerLayout);
        TextView titleTextView = findViewById(R.id.titleTextView);
        ImageButton saveButton = findViewById(R.id.save_button); // ID кнопки "Сохранить"
        ImageButton exportButton = findViewById((R.id.export_button));

        if (isDarkTheme) {
            headerLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.black2));
            titleTextView.setTextColor(getResources().getColor(R.color.backgroundHeaderMain));
            saveButton.setImageResource(R.drawable.save_dark); // Устанавливаем темную иконку
            exportButton.setImageResource(R.drawable.export_dark); // Устанавливаем tint для кнопки "Экспорт"
        } else {
            headerLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundHeaderMain));
            titleTextView.setTextColor(getResources().getColor(R.color.black));
            saveButton.setImageResource(R.drawable.save); // Устанавливаем светлую иконку
            exportButton.setImageResource(R.drawable.export);
        }
    }

    public void backClick(View view) {
        finish();
    }

    public void showProjectFiles(View view) {
        PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        for (MediaFile file: MediaFiles) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView textView = new TextView(this);
            textView.setText(file.getNameFile());
            textView.setPadding(16, 8, 16, 8);
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

            ImageButton button = new ImageButton(this);
            button.setBackground(null);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.trash);
            button.setImageBitmap(bitmap);
            button.setLayoutParams(new LinearLayout.LayoutParams(135, 135));
            button.setScaleType(ImageView.ScaleType.FIT_CENTER);

            button.setOnClickListener(v -> {
                int index = MediaFiles.indexOf(file); // Находим индекс файла
                if (index != -1) {
                    adapter.notifyItemRemoved(index); // Уведомляем адаптер об удалении
                    boolean success = JSONHelper.exportToJSON(this, projectInfo); // Сохраняем изменения
                    if (success) {
                        Toast.makeText(this, "Файл " + file.getNameFile() + " успешно удален из проекта", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка сохранения изменений!", Toast.LENGTH_SHORT).show();
                    }
                }
                popupWindow.dismiss();
            });

            rowLayout.addView(textView);
            rowLayout.addView(button);
            layout.addView(rowLayout);
        }

        popupWindow.setContentView(layout);
        popupWindow.setHeight(WRAP_CONTENT);
        popupWindow.setWidth(WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(view, 0, 0);
    }

    public void addProjectFiles(View view){
        pickMedia();
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
    public void onTimeSaved(int hours, int minutes, int seconds, int millis, MediaFile mediaFile) {
        Duration newDuration = Duration.ofHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds)
                .plusMillis(millis);

        if (newDuration.compareTo(mediaFile.getMaxDuration()) < 0) {
            mediaFile.setDuration(newDuration); // Set to newDuration if it's less than maxDuration
            mediaFile.setWidthOnTimeline(countTimeAndWidth.WidthByTimeChanged(newDuration));

        } else {
            mediaFile.setDuration(mediaFile.getMaxDuration()); // Set to maxDuration if newDuration is greater
            mediaFile.setWidthOnTimeline(countTimeAndWidth.WidthByTimeChanged(mediaFile.getMaxDuration()));
        }

        Toast.makeText(this, "Время изменено: " + mediaFile.getDuration(), Toast.LENGTH_SHORT).show();
        Log.d("widthTimeChange", String.valueOf(mediaFile.getWidthOnTimeline()));
        projectInfo.updateMediafile(mediaFile);

        JSONHelper.exportToJSON(this, projectInfo);
    }

    public void addEffects(View anchorView) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupContentView = layoutInflater.inflate(R.layout.popup_effects_window, null);

        // Создаем PopupWindow
        PopupWindow effectsPopupWindow = new PopupWindow(popupContentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        effectsPopupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
        effectsPopupWindow.setOutsideTouchable(true);
        effectsPopupWindow.setFocusable(true);

        // Получаем ширину экрана
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Вычисляем новую ширину и высоту для квадратного PopupWindow
        int padding = 100; // Отступы по 100 пикселей с каждой стороны
        int popupSize = screenWidth - 2 * padding; // Размер для квадратного PopupWindow

        // Устанавливаем новую ширину и высоту для PopupWindow
        effectsPopupWindow.setWidth(popupSize);
        effectsPopupWindow.setHeight(popupSize);

        // Создаем экземпляр обработчика кликов
        EffectClickListener clickListener = new EffectClickListener(this);

        // Находим LinearLayout и назначаем обработчики кликов
        LinearLayout layoutBrightness = popupContentView.findViewById(R.id.layout_brightness);
        LinearLayout layoutContrast = popupContentView.findViewById(R.id.layout_contrast);
        LinearLayout layoutSaturation = popupContentView.findViewById(R.id.layout_saturation);

        layoutBrightness.setOnClickListener(clickListener);
        layoutContrast.setOnClickListener(clickListener);
        layoutSaturation.setOnClickListener(clickListener);

        int xOffset = -screenWidth / 2 + padding;
        int yOffset = -effectsPopupWindow.getHeight() - anchorView.getHeight(); // Сдвигаем вверх на высоту PopupWindow
        effectsPopupWindow.showAsDropDown(anchorView, xOffset, yOffset);
    }

}
