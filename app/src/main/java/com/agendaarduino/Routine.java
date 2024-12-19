package com.agendaarduino;

public class Routine {

    private String idRoutine;
    private String title;
    private String description;
    private String label;
    private String daysOfWeek;
    private String time;
    private String statusDay;
    private String recordatory;
    private String idUser;

    // Constructores
    public Routine() {
        // Constructor vacío para Firebase
    }

    public Routine(String title, String description, String label,
                   String daysOfWeek, String time,
                   String statusDay, String recordatory, String idUser) {
        this.title = title;
        this.description = description;
        this.label = label;
        this.daysOfWeek = daysOfWeek;
        this.time = time;
        this.statusDay = statusDay;
        this.recordatory = recordatory;
        this.idUser = idUser;

    }

    // Getters

    public String getIdRoutine() {
        return idRoutine;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public String getTime() {
        return time;
    }

    public String getStatusDay() {
        return statusDay;
    }

    public String getRecordatory() {
        return recordatory;
    }

    public String getIdUser() {
        return idUser;
    }


    // Setters
    public void setIdRoutine(String idRoutine) {
        this.idRoutine = idRoutine;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setStatusDay(String statusDay) {
        this.statusDay = statusDay;
    }

    public void setRecordatory(String recordatory) {
        this.recordatory = recordatory;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

}
