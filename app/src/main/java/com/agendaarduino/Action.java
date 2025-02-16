package com.agendaarduino;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Action {
    private String id;
    private String title;
    private String description;
    private String time;
    private String label;
    private String status;
    private String recordatory;
    private String dateUpdated;

    private String hourCalculate;
    private List<ChecklistItem> checkList;

    private String idUser;
    // Constructores
    public Action(){

    }

    public Action(String title, String description, String time, String label,
                  String status, String recordatory, String idUser, List<ChecklistItem> checkList) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.label = label;
        this.status = status;
        this.recordatory = recordatory;
        this.idUser = idUser;
        this.checkList = checkList;
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

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getDateUpdated() {return dateUpdated;}
    public void setDateUpdated(String dateUpdated) {this.dateUpdated = dateUpdated;}

    public List<ChecklistItem> getChecklist() {
        return checkList;
    }

    public void setChecklist(List<ChecklistItem> checklist) {
        this.checkList = checklist;
    }
    public void updateStatusForToday() {
        LocalDate today = LocalDate.now();
        String todayDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE);

        if (!todayDate.equals(dateUpdated)) {
            if (this instanceof Routine && "completado".equals(status)) {
                status = "pendiente";
            }
            dateUpdated = todayDate;
        }
    }

    public String getHourCalculate() {
        return hourCalculate;
    }

    public void setHourCalculate(String hourCalculate) {
        this.hourCalculate = hourCalculate;
    }

}
