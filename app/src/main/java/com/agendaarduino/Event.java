package com.agendaarduino;

public class Event extends Action{
    private String idEvent;
    private String date;
    private String idUser;

    // Constructores
    public Event() {
        // Constructor vacío para Firebase
    }

    public Event(String date, String idUser, String idEvent) {
        this.date = date;
        this.idUser = idUser;
        this.idEvent = idEvent;
    }

    // Getters
    public String getIdEvent(){
        return idEvent;
    }
    public String getDate() {
        return date;
    }
    public String getIdUser() {
        return idUser;
    }

    // Setters
    public void setIdEvent(String idEvent) {
        this.idEvent = idEvent;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }
}

