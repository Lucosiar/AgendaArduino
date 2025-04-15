package com.agendaarduino;

import android.view.View;
import android.widget.TextView;

import com.kizitonwose.calendarview.ui.ViewContainer;

public class DayViewContainer extends ViewContainer {
    public final TextView textView;

    public DayViewContainer(View view) {
        super(view);
        textView = view.findViewById(R.id.calendarDayText);
    }
}