package com.agendaarduino;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.YearMonth;


public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private LocalDate selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);

        YearMonth currentMonth = YearMonth.now();
        LocalDate firstDayOfWeek = LocalDate.now().withDayOfMonth(1);

        calendarView.setup(currentMonth, currentMonth.plusMonths(12), firstDayOfWeek.getDayOfWeek());
        calendarView.scrollToMonth(currentMonth);

        calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @Override
            public DayViewContainer create(View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(DayViewContainer container, CalendarDay day) {
                TextView textView = container.textView;
                textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

                if (day.getOwner() == DayOwner.THIS_MONTH) {
                    textView.setVisibility(View.VISIBLE);
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


}