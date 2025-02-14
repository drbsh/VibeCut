package com.example.vibecut.Models;

import android.net.Uri;

import java.io.File;
import java.time.Duration;
import java.time.LocalTime;

public class MediaFile extends BaseFile {
    private String previewMedia; // Уникальное поле для MediaFile
    private String originalPathToFile;

    // Конструктор
    public MediaFile(String nameFile, Uri previewMedia, Uri pathToEditedFile, Uri originalPathToFile, Duration duration, String typeMedia, int widthOnTimeline) {
        super(nameFile, pathToEditedFile, duration, typeMedia, widthOnTimeline); // Вызов конструктора базового класса
        this.previewMedia = previewMedia.toString(); // Инициализация уникального поля
        this.originalPathToFile = originalPathToFile.getPath();
    }

    // Геттер и сеттер для previewMedia
    public Uri getPreviewMedia() {
        return Uri.parse(previewMedia); // Десериализация строки в Uri
    }

    public void setPreviewMedia(Uri previewMedia) {
        this.previewMedia = previewMedia.toString(); // Сериализация Uri в строку
    }
    public Uri getPathToOriginalFile() {
        return Uri.parse(originalPathToFile);
    }
    public void setPathToOriginalFile(String originalPathToFile) {
        this.originalPathToFile = originalPathToFile;
    }

}