package com.agendaarduino;

import java.util.List;

public interface ChecklistCallback {
    void onChecklistLoaded(List<ChecklistItem> checklistItems);
}