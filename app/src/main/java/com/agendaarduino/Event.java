package com.agendaarduino;

public class Event {
    private String id;
    private String title;
    private String description;
    private String label;
    private String date;
    private String time;

    private String status;

    public Event() {
        // Constructor vacío para Firebase
    }

    public Event(String title, String description, String label, String date, String time, String status) {
        this.title = title;
        this.description = description;
        this.label = label;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getId(){
        return id;
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

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

