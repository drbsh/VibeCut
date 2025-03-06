package com.example.vibecut.Models;

import android.net.Uri;

import java.io.Serializable;
import java.time.Duration;
import java.util.UUID;

public class MediaFile implements Serializable {
    private String previewMedia; // Уникальное поле для MediaFile
    private String originalPathToFile;
    private String idFile;
    private String nameFile;
    private String pathToFile;
    private Duration duration;
    private String typeMedia;
    private int widthOnTimeline;
    private Duration maxDuration;
    private int differenceLeftBorderFromLeftSide;
    private int differenceRightBorderFromRightSide;
    // Конструктор для общих полей
    public MediaFile(String nameFile, Uri previewMedia, Uri pathToEditedFile, Uri originalPathToFile, Duration duration, String typeMedia, int widthOnTimeline) {
        this.idFile = UUID.randomUUID().toString(); // Рандомный ID
        this.nameFile = nameFile;
        this.pathToFile = pathToFile.toString(); // Сериализация Uri в строку
        this.duration = duration;
        this.typeMedia = typeMedia;
        this.widthOnTimeline = widthOnTimeline;
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

    // Геттеры и сеттеры
    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public Uri getPathToEditedFile() {
        return Uri.parse(pathToFile); // Десериализация строки в Uri
    }

    public void setPathToFile(Uri pathToFile) {
        this.pathToFile = pathToFile.toString(); // Сериализация Uri в строку
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getTypeMedia() {
        return typeMedia;
    }

    public void setTypeMedia(String typeMedia) {
        this.typeMedia = typeMedia;
    }

    public String getIdFile() {
        return idFile;
    }

    public void setIdFile(String idFile) {
        this.idFile = idFile;
    }

    public int getWidthOnTimeline() {
        return widthOnTimeline;
    }

    public void setWidthOnTimeline(int widthOnTimeline) {
        this.widthOnTimeline = widthOnTimeline;
    }

    public Duration getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Duration maxDuration) {
        this.maxDuration = maxDuration;
    }

    public int getDifferenceLeftBorderFromLeftSide() {
        return differenceLeftBorderFromLeftSide;
    }

    public int getDifferenceRightBorderFromRightSide() {
        return differenceRightBorderFromRightSide;
    }

    public void setDifferenceRightBorderFromRightSide(int differenceRightBorderFromRightSide) {
        this.differenceRightBorderFromRightSide = differenceRightBorderFromRightSide;
    }
    public void setDifferenceLeftBorderFromLeftSide(int differenceLeftBorderFromLeftSide) {
        this.differenceLeftBorderFromLeftSide = differenceLeftBorderFromLeftSide;
    }


}