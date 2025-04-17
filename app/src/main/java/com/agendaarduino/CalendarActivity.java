package com.agendaarduino;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kizitonwose.calendarview.utils.Size;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private LocalDate selectedDate;
    private YearMonth currentMonth;
    private YearMonth startMonth;
    private YearMonth endMonth;
    private TextView textMonthTitle;
    private ImageButton btnPrevMonth, btnNextMonth;
    private final DateTimeFormatter titleFormatter =
            DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es"));
    private final Map<LocalDate, List<Action>> actionsByDate = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        inicialize();

        // Inicializamos el mes actual y la configuración del calendario
        currentMonth = YearMonth.now();
        startMonth = currentMonth;
        endMonth = currentMonth.plusMonths(12);

        calendarView.setup(startMonth, endMonth, DayOfWeek.MONDAY);
        calendarView.scrollToMonth(currentMonth);
        updateMonthTitle(currentMonth);

        // Escuchar cambios al deslizar entre meses
        calendarView.setMonthScrollListener(calendarMonth -> {
            currentMonth = calendarMonth.getYearMonth();
            updateMonthTitle(currentMonth);
            return Unit.INSTANCE;
        });

        // Configurar como se ve cada día en el calendario
        calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @Override
            public DayViewContainer create(View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(DayViewContainer container, CalendarDay day) {
                TextView textView = container.dayText;
                textView.setText(String.valueOf(day.getDate().getDayOfMonth()));
                container.date = day.getDate();
                container.eventsContainer.removeAllViews();

                if (day.getOwner() == DayOwner.THIS_MONTH) {
                    textView.setVisibility(View.VISIBLE);

                    // Día actual
                    if (day.getDate().equals(LocalDate.now())) {
                        textView.setTextColor(ContextCompat.getColor(CalendarActivity.this, R.color.colorSecundary));
                    } else {
                        textView.setTextColor(Color.WHITE);
                    }

                    // Mostrar eventos
                    List<Action> actions = actionsByDate.get(day.getDate());
                    if (actions != null) {
                        for (Action action : actions) {
                            TextView eventView = new TextView(CalendarActivity.this);
                            eventView.setText(action.getTitle() + " - " + action.getTime());
                            eventView.setTextSize(12f);
                            eventView.setMaxLines(1);
                            eventView.setEllipsize(TextUtils.TruncateAt.END);
                            container.eventsContainer.addView(eventView);
                        }
                    }

                } else {
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Navegación entre meses
        btnPrevMonth.setOnClickListener(v -> {
            YearMonth prevMonth = currentMonth.minusMonths(1);
            calendarView.smoothScrollToMonth(prevMonth);
        });

        btnNextMonth.setOnClickListener(v -> {
            YearMonth nextMonth = currentMonth.plusMonths(1);
            calendarView.smoothScrollToMonth(nextMonth);
        });

        loadAllUserActions();
    }

    private void loadAllUserActions() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Utility.getCollectionReferenceForEvents()
                .whereEqualTo("idUser", currentUserId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("CalendarActivity", "Error al cargar eventos", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        actionsByDate.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                LocalDate date = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                                actionsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
                            }
                        }

                        // También cargamos las rutinas
                        loadAllUserRoutines(currentUserId);
                    }
                });
    }

    private void loadAllUserRoutines(String userId) {
        Utility.getCollectionReferenceForRoutines()
                .whereEqualTo("idUser", userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("CalendarActivity", "Error al cargar rutinas", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Routine routine = doc.toObject(Routine.class);
                            if (routine != null) {
                                for (LocalDate date : getAllDatesInCalendarRange()) {
                                    String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toLowerCase();
                                    if (routine.getDaysOfWeek().toLowerCase().contains(dayOfWeek)) {
                                        actionsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(routine);
                                    }
                                }
                            }
                        }

                        // Finalmente, refrescamos el calendario
                        calendarView.notifyCalendarChanged();
                    }
                });
    }

    private List<LocalDate> getAllDatesInCalendarRange() {
        List<LocalDate> dates = new ArrayList<>();

        LocalDate current = startMonth.atDay(1);
        LocalDate end = endMonth.atEndOfMonth();

        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    public void showDayDetailsPopup(LocalDate date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_day_details, null);

        TextView txtTitle = dialogView.findViewById(R.id.txtDialogTitle);
        LinearLayout layoutEventList = dialogView.findViewById(R.id.layoutEventList);
        Button btnAddEvent = dialogView.findViewById(R.id.btnAddEvent);

        txtTitle.setText("Eventos de " + date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("es"))));

        List<Action> actions = actionsByDate.get(date);
        layoutEventList.removeAllViews();

        if (actions != null && !actions.isEmpty()) {
            for (Action action : actions) {
                TextView actionView = new TextView(this);
                actionView.setText("• " + action.getTitle() + " - " + (action.getTime() != null ? action.getTime() : "Sin hora"));
                actionView.setTextSize(14f);
                actionView.setPadding(0, 8, 0, 8);
                layoutEventList.addView(actionView);
            }
        } else {
            TextView noEventsView = new TextView(this);
            noEventsView.setText("No hay eventos ni rutinas para este día.");
            noEventsView.setPadding(0, 8, 0, 8);
            layoutEventList.addView(noEventsView);
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnAddEvent.setOnClickListener(v -> {
            dialog.dismiss();
            // Aquí puedes abrir la actividad de creación de evento
            Intent intent = new Intent(CalendarActivity.this, EditEventActivity.class);
            intent.putExtra("selectedDate", date.toString());
            startActivity(intent);
        });
    }


    @NonNull
    private static StringBuilder getStringBuilder(List<Action> actions) {
        StringBuilder message = new StringBuilder();

        if (actions != null && !actions.isEmpty()) {
            for (Action action : actions) {
                message.append("• ")
                        .append(action.getTitle())
                        .append(" - ")
                        .append(action.getTime() != null ? action.getTime() : "Sin hora")
                        .append("\n");
            }
        }
        return message;
    }


    private void updateMonthTitle(YearMonth month) {
        textMonthTitle.setText(titleFormatter.format(month));
    }

    private void inicialize(){
        calendarView = findViewById(R.id.calendarView);
        textMonthTitle = findViewById(R.id.textMonthTitle);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        calendarView.setDaySize(new Size(140, 400));

    }
}