package com.agendaarduino;

public class Routine extends Action{

    private String idRoutine;
    private String daysOfWeek;


    // Constructores
    public Routine() {
        // Constructor vacío para Firebase
    }

    public Routine(String idRoutine, String daysOfWeek) {
        this.idRoutine = idRoutine;
        this.daysOfWeek = daysOfWeek;
    }

    public String getIdRoutine() {
        return idRoutine;
    }
    public String getDaysOfWeek() {
        return daysOfWeek;
    }
    public void setIdRoutine(String idRoutine) {
        this.idRoutine = idRoutine;
    }
    public void setDaysOfWeek(String daysOfWeek) {this.daysOfWeek = daysOfWeek;}

    public void checkAndUpdateStatus() {
        updateStatusForToday();
    }

}
