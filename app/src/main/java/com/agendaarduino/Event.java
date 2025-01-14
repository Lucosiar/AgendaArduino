package com.agendaarduino;

import java.util.List;

public class Event extends Action{
    private String idEvent;
    private String date;

    public Event() {
    }

    public Event(String date, String idUser, String idEvent) {
        this.date = date;
        this.idEvent = idEvent;
    }

    // Getters
    public String getIdEvent(){
        return idEvent;
    }
    public String getDate() {
        return date;
    }

    // Setters
    public void setIdEvent(String idEvent) {
        this.idEvent = idEvent;
    }
    public void setDate(String date) {
        this.date = date;
    }

}

