package com.agendaarduino;

public class Event {
    private String title;
    private String description;
    private String tag;
    private String date;
    private String time;

    private String status;

    public Event() {
        // Constructor vacío para Firebase
    }

    public Event(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

