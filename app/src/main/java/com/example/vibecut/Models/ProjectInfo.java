package com.example.vibecut.Models;

import android.net.Uri;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProjectInfo implements Serializable {
    private String idProj;
    private String preview; // Изменено на String
    private String name;
    private LocalTime duration = LocalTime.of(0, 0); // 00:00
    private LocalDateTime date;
    private Boolean isFavourite = false;
    private List<MediaFile> projectFiles = new ArrayList<MediaFile>();
    //private File fileProject;
    public ProjectInfo(){
        this.idProj = UUID.randomUUID().toString(); //рандом id
    }

    public ProjectInfo(String preview, String name, LocalTime duration, LocalDateTime date, Boolean isFavourite, ArrayList<MediaFile> projectFiles/*,File fileProject*/) {
        this.idProj = UUID.randomUUID().toString(); //рандом id
        this.preview = preview; // Сериализация Uri в строку
        this.name = name;
        this.duration = duration;
        this.date = date;
        this.isFavourite = isFavourite;
        this.projectFiles = projectFiles;
        //this.fileProject = fileProject;
    }

    public String getIdProj(){
        return idProj;
    }

    public void setIdProj(String idProj){
        this.idProj = idProj;
    }

    public Uri getPreview() {
        if (preview != null){
            return Uri.parse(preview); // Десериализация строки в Uri
        }
        return null;
    }

    public void setPreview(Uri preview) {
        this.preview = preview.toString(); // Сериализация Uri в строку
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalTime getDurarion() {
        return duration;
    }

    public void setDurarion(LocalTime duration) {
        this.duration = duration;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Boolean getFavourite() {
        return isFavourite;
    }

    public void setFavourite(Boolean favourite) {
        isFavourite = favourite;
    }

    public void setProjectFiles(List<MediaFile> projectFiles){
        this.projectFiles = projectFiles;
    }
    public void addMediaFile(MediaFile mediaFile){
        projectFiles.add(mediaFile);
    }
    public List<MediaFile> getProjectFiles(){
        return projectFiles;
    }

    public void updateMediafile(MediaFile newFile) {
        for (int i = 0; i < projectFiles.size(); i++) {
            if (Objects.equals(projectFiles.get(i).getIdFile(), newFile.getIdFile())) {
                projectFiles.set(i, newFile);
                return;
            }
        }
    }

    /**
     * Возвращает список медиафайлов с типом "video" или "img".
     *
     * @return Список медиафайлов.
     */
    public List<MediaFile> getMediaFiles() {
        return projectFiles.stream()
                .filter(file -> file.getTypeMedia().equals("video") || file.getTypeMedia().equals("img"))
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список аудиофайлов с типом "audio".
     *
     * @return Список аудиофайлов.
     */
    public List<MediaFile> getAudioFiles() {
        return projectFiles.stream()
                .filter(file -> file.getTypeMedia().equals("audio"))
                .collect(Collectors.toList());
    }

}
