package com.agendaarduino;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kizitonwose.calendarview.ui.ViewContainer;

import java.time.LocalDate;

public class DayViewContainer extends ViewContainer {
    public final TextView dayText;
    public final LinearLayout eventsContainer;
    public LocalDate date;

    public DayViewContainer(View view) {
        super(view);
        dayText = view.findViewById(R.id.calendarDayText);
        eventsContainer = view.findViewById(R.id.eventsContainer);

        // Detectar clic en el día
        view.setOnClickListener(v -> {
            if (date != null) {
                // Aquí obtenemos la actividad y llamamos al método correctamente
                if (view.getContext() instanceof CalendarActivity) {
                    ((CalendarActivity) view.getContext()).showDayDetailsPopup(date);
                }
            }
        });
    }
}