package com.agendaarduino;

public class Routine extends Action{

    private String idRoutine;
    private String daysOfWeek;
    private String statusDay;
    private String idUser;

    // Constructores
    public Routine() {
        // Constructor vacío para Firebase
    }

    public Routine(String daysOfWeek,String statusDay, String idUser) {
        this.daysOfWeek = daysOfWeek;
        this.statusDay = statusDay;
        this.idUser = idUser;

    }

    // Getters

    public String getIdRoutine() {
        return idRoutine;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }
    public String getStatusDay() {
        return statusDay;
    }
    public String getIdUser() {
        return idUser;
    }


    // Setters
    public void setIdRoutine(String idRoutine) {
        this.idRoutine = idRoutine;
    }
    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
    public void setStatusDay(String statusDay) {
        this.statusDay = statusDay;
    }
    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

}
