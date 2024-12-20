package com.agendaarduino;

public class Action {
    private String title;
    private String description;
    private String time;
    private String label;
    private String status;
    private String recordatory;
    // Constructores
    public Action(){

    }

    public Action(String title, String description, String time, String label,
                  String status, String recordatory) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.label = label;
        this.status = status;
        this.recordatory = recordatory;
    }

    // Getters y setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecordatory() {
        return recordatory;
    }

    public void setRecordatory(String recordatory) {
        this.recordatory = recordatory;
    }

}
