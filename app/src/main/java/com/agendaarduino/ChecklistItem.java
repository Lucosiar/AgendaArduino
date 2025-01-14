package com.agendaarduino;

public class ChecklistItem {
    private String actionId;
    private String title;
    private String status;

    public ChecklistItem() {
    }

    public ChecklistItem(String actionId, String title, String status) {
        this.actionId = actionId;
        this.title = title;
        this.status = status;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
