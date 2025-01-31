package com.example.vibecut.Models;

import android.net.Uri;

import java.time.LocalTime;

public class MediaFile extends BaseAudioFile {
    private String previewMedia; // Уникальное поле для MediaFile

    // Конструктор
    public MediaFile(String nameFile, Uri previewMedia, Uri pathToFile, LocalTime duration, String typeMedia) {
        super(nameFile, pathToFile, duration, typeMedia); // Вызов конструктора базового класса
        this.previewMedia = previewMedia.toString(); // Инициализация уникального поля
    }

    // Геттер и сеттер для previewMedia
    public Uri getPreviewMedia() {
        return Uri.parse(previewMedia); // Десериализация строки в Uri
    }

    public void setPreviewMedia(Uri previewMedia) {
        this.previewMedia = previewMedia.toString(); // Сериализация Uri в строку
    }
}