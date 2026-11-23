package com.example.meditrack2.Login;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Outline;
import android.view.ViewOutlineProvider;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;

import org.json.JSONArray;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class PanelPersonal extends AppCompatActivity {
    private static final int NAVY = Color.rgb(11, 46, 79);
    private static final int MUTED = Color.rgb(79, 106, 126);
    private static final int TEAL = Color.rgb(10, 116, 128);
    private final List<Cita> citas = new ArrayList<>();
    private LinearLayout content;
    private LinearLayout countersRow;
    private ScrollView scrollView;
    private View agendaHeading;
    private Button todayChip, weekChip;
    private String filtro = "Hoy";

    private static class Cita {
        String fecha, hora, paciente, estado;
        Cita(String fecha, String hora, String paciente, String estado) {
            this.fecha = fecha; this.hora = hora; this.paciente = paciente; this.estado = estado;
        }
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(234, 248, 248));
        getWindow().setNavigationBarColor(Color.rgb(234, 248, 248));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(234, 248, 248));
        setContentView(root);

        root.setClipChildren(false);
        root.setClipToPadding(false);
        View background = new SoftSpotsView();
        background.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        root.addView(background, new FrameLayout.LayoutParams(-1, -1));
        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        FrameLayout.LayoutParams scrollLp = new FrameLayout.LayoutParams(-1, -1);
        scrollLp.bottomMargin = dp(84);
        root.addView(scrollView, scrollLp);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int horizontal = dp(20);
        content.setPadding(horizontal, dp(26), horizontal, dp(22));
        scrollView.addView(content);

        boolean nursing = "ENFERMERO".equalsIgnoreCase(
                getSharedPreferences("sesion_meditrack", MODE_PRIVATE).getString("rol", ""));
        header(nursing);
        if (nursing) {
            nursingHome();
        } else {
        counters();
        nextConsultation();
        quickAccess();
        agendaSection();
        }
        LinearLayout bar = bottomBar(root, nursing);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            androidx.core.graphics.Insets system = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            scrollView.setPadding(0, system.top, 0, 0);
            ViewGroup.LayoutParams barParams = bar.getLayoutParams();
            barParams.height = dp(84) + system.bottom;
            bar.setLayoutParams(barParams);
            bar.setPadding(dp(14), 0, dp(14), system.bottom);
            FrameLayout.LayoutParams scrollParams = (FrameLayout.LayoutParams) scrollView.getLayoutParams();
            scrollParams.bottomMargin = dp(84) + system.bottom;
            scrollView.setLayoutParams(scrollParams);
            return insets;
        });
        androidx.core.view.ViewCompat.requestApplyInsets(root);
        if (!nursing) cargarCitas();
    }

    private void header(boolean nursing) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView title = text((nursing ? "Hola, Enf. " : "Hola, Dra. ") + sessionName(), 26, NAVY, true);
        title.setMaxLines(1); title.setEllipsize(TextUtils.TruncateAt.END);
        titles.addView(title);
        TextView subtitle = text(nursing ? "Panel de enfermería" : "Panel médico", 13, MUTED, false);
        subtitle.setMaxLines(1); subtitle.setEllipsize(TextUtils.TruncateAt.END);
        titles.addView(subtitle, sectionTop(2));
        row.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
        ImageView avatar = new ImageView(this);
        avatar.setImageResource(nursing ? R.drawable.enfermera : R.drawable.medico);
        avatar.setContentDescription(nursing ? "Perfil de enfermería" : "Foto de perfil del médico");
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setBackground(circle(Color.WHITE));
        avatar.setClipToOutline(true);
        avatar.setOutlineProvider(ovalOutline());
        avatar.setPadding(dp(3), dp(3), dp(3), dp(3));
        row.addView(avatar, new LinearLayout.LayoutParams(dp(52), dp(52)));
        content.addView(row);
    }

    private View nursingTasks, nursingPatients;
    private static final String[] NURSING_LINKS = {"Signos vitales", "Medicación", "Tratamientos", "Alertas"};
    private static final int[] NURSING_ICONS = {R.drawable.ic_pulso_enfermeria, R.drawable.pildoras,
            R.drawable.estetoscopio, R.drawable.alarma};

    private void nursingHome() {
        LinearLayout shift = card(26, 18, 18);
        shift.setOrientation(LinearLayout.HORIZONTAL);
        shift.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable surface = roundGradient(26, Color.WHITE, 0xffd9f3f4);
        surface.setStroke(dp(1), Color.WHITE);
        shift.setBackground(surface);
        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.VERTICAL);
        TextView overline = text("TURNO DE HOY", 12, TEAL, true);
        overline.setLetterSpacing(.05f);
        summary.addView(overline);
        summary.addView(text("Aún no hay tareas pendientes", 19, NAVY, true), sectionTop(6));
        shift.addView(summary, new LinearLayout.LayoutParams(0, -2, 1));
        TextView ring = text("—", 20, TEAL, true);
        GradientDrawable track = circle(Color.TRANSPARENT);
        track.setStroke(dp(10), 0xffd1eef0);
        ring.setBackground(track);
        ring.setGravity(Gravity.CENTER);
        ring.setContentDescription("Progreso del turno sin datos");
        shift.addView(ring, new LinearLayout.LayoutParams(dp(92), dp(92)));
        content.addView(shift, sectionTop(16));

        nursingHeading("Accesos rápidos", "Ver todos ›", this::showNursingShortcuts);
        LinearLayout shortcuts = new LinearLayout(this);
        shortcuts.setClipChildren(false);
        for (int i = 0; i < NURSING_LINKS.length; i++) {
            final String label = NURSING_LINKS[i];
            LinearLayout tile = card(22, 2, 10);
            tile.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            ImageView image = new ImageView(this);
            image.setImageResource(NURSING_ICONS[i]);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setContentDescription(label);
            if (i == 0) {
                image.setBackground(roundGradient(15, 0xffffe3de, 0xffffe3de));
                image.setPadding(dp(10), dp(10), dp(10), dp(10));
            }
            tile.addView(image, new LinearLayout.LayoutParams(dp(48), dp(48)));
            TextView caption = text(label, 11, NAVY, true);
            caption.setGravity(Gravity.CENTER);
            tile.addView(caption, sectionTop(8));
            tile.setOnClickListener(v -> showNursingDetail(label));
            LinearLayout.LayoutParams slot = new LinearLayout.LayoutParams(0, -1, 1);
            if (i > 0) slot.leftMargin = dp(10);
            shortcuts.addView(tile, slot);
        }
        content.addView(shortcuts);
        nursingTasks = nursingEmptySection("Tareas pendientes", "Ver todas ›", "Aún no hay tareas pendientes", R.drawable.outline_assignment_24);
        nursingPatients = nursingEmptySection("Pacientes asignados", "Ver todos ›", "Aún no hay pacientes asignados", R.drawable.paciente);
    }

    private View nursingHeading(String title, String link, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(text(title, 17, NAVY, true), new LinearLayout.LayoutParams(0, -2, 1));
        TextView more = text(link, 13, TEAL, true);
        more.setGravity(Gravity.CENTER);
        more.setMinHeight(dp(48));
        more.setOnClickListener(v -> action.run());
        row.addView(more);
        content.addView(row, sectionTop(12));
        return row;
    }

    private View nursingEmptySection(String title, String link, String message, int icon) {
        View heading = nursingHeading(title, link, () -> nursingDialog(title, message));
        LinearLayout panel = card(22, 16, 16);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        ImageView image = new ImageView(this);
        image.setImageResource(icon);
        image.setContentDescription(title);
        panel.addView(image, new LinearLayout.LayoutParams(dp(36), dp(36)));
        TextView empty = text(message, 13, MUTED, false);
        empty.setPadding(dp(12), 0, 0, 0);
        panel.addView(empty);
        content.addView(panel);
        return heading;
    }

    private void showNursingShortcuts() {
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Accesos rápidos")
                .setItems(NURSING_LINKS, (dialog, item) -> showNursingDetail(NURSING_LINKS[item]))
                .setNegativeButton("Volver", null).show();
    }

    private void showNursingDetail(String label) {
        nursingDialog(label, "Aún no hay datos para mostrar");
    }

    private void nursingDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("Volver", null).show();
    }

    private void counters() {
        LinearLayout row = new LinearLayout(this);
        countersRow = row;
        row.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels = {"Citas hoy", "Confirmadas", "Pendientes"};
        int[] colors = {TEAL, Color.rgb(35,73,176), Color.rgb(154,91,0)};
        for (int i = 0; i < labels.length; i++) {
            LinearLayout card = card(22, 8, 12);
            card.setGravity(Gravity.CENTER);
            TextView number = text("0", 24, colors[i], true);
            number.setGravity(Gravity.CENTER);
            card.addView(number);
            TextView caption = text(labels[i], 12, MUTED, true);
            caption.setGravity(Gravity.CENTER);
            card.addView(caption);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(76), 1);
            if (i > 0) lp.leftMargin = dp(10);
            row.addView(card, lp);
        }
        content.addView(row, sectionTop(18));
    }

    private LinearLayout nextCard;
    private TextView nextStatus, nextPatient, nextTime;
    private LinearLayout nextPatientRow, nextActions;
    private void nextConsultation() {
        nextCard = card(26, 18, 18);
        nextCard.setBackground(roundGradient(26, 0xff17b5be, 0xff0b5f70));
        LinearLayout topRow = new LinearLayout(this);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = text("PRÓXIMA CONSULTA", 12, 0xe6ffffff, true);
        label.setLetterSpacing(.06f);
        topRow.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
        nextStatus = text("", 11, 0xff0b7a5a, true);
        nextStatus.setPadding(dp(10), dp(4), dp(10), dp(4));
        nextStatus.setBackground(roundGradient(12, 0xffd6f5ea, 0xffd6f5ea));
        topRow.addView(nextStatus);
        nextCard.addView(topRow);

        nextPatientRow = new LinearLayout(this);
        nextPatientRow.setGravity(Gravity.CENTER_VERTICAL);
        ImageView patientIcon = new ImageView(this);
        patientIcon.setImageResource(R.drawable.paciente);
        patientIcon.setContentDescription("Paciente de la próxima consulta");
        patientIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        patientIcon.setBackground(circle(Color.WHITE));
        patientIcon.setClipToOutline(true);
        patientIcon.setOutlineProvider(ovalOutline());
        nextPatientRow.addView(patientIcon, new LinearLayout.LayoutParams(dp(54), dp(54)));
        LinearLayout patientText = new LinearLayout(this);
        patientText.setOrientation(LinearLayout.VERTICAL);
        patientText.setPadding(dp(12), 0, 0, 0);
        nextPatient = text("Aún no hay una próxima cita para mostrar", 16, Color.WHITE, true);
        nextTime = text("", 13, 0xffe5ffff, false);
        patientText.addView(nextPatient);
        patientText.addView(nextTime, sectionTop(3));
        nextPatientRow.addView(patientText, new LinearLayout.LayoutParams(0, -2, 1));
        nextCard.addView(nextPatientRow, sectionTop(12));

        nextActions = new LinearLayout(this);
        Button start = new Button(this);
        start.setText("Iniciar consulta");
        start.setAllCaps(false);
        start.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_white_teal, 0, 0, 0);
        start.setCompoundDrawablePadding(dp(8));
        start.setTextColor(TEAL);
        start.setTextSize(14);
        start.setTypeface(null, Typeface.BOLD);
        start.setBackground(roundGradient(23, Color.WHITE, Color.WHITE));
        start.setStateListAnimator(null);
        start.setOnClickListener(v -> Toast.makeText(this, "Aún no hay una consulta para iniciar.", Toast.LENGTH_SHORT).show());
        nextActions.addView(start, new LinearLayout.LayoutParams(0, dp(46), 1));
        ImageView stethoscope = new ImageView(this);
        stethoscope.setImageResource(R.drawable.ic_estetoscopio);
        stethoscope.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        stethoscope.setPadding(dp(11), dp(11), dp(11), dp(11));
        stethoscope.setContentDescription("Estetoscopio");
        GradientDrawable circle = roundGradient(30, 0x33ffffff, 0x33ffffff);
        stethoscope.setBackground(circle);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(46), dp(46));
        iconLp.leftMargin = dp(10);
        nextActions.addView(stethoscope, iconLp);
        nextCard.addView(nextActions, sectionTop(14));
        setNextEmpty();
        content.addView(nextCard, sectionTop(18));
    }

    private void quickAccess() {
        sectionHeading("Accesos rápidos", "Ver todos ›");
        LinearLayout row = new LinearLayout(this);
        String[] labels = {"Agenda", "Pacientes", "Recetas", "Historial"};
        int[] icons = {R.drawable.citasicono, R.drawable.familia, R.drawable.pildoras, R.drawable.historial};
        for (int i = 0; i < labels.length; i++) {
            LinearLayout item = card(18, 8, 10);
            item.setGravity(Gravity.CENTER);
            ImageView image = new ImageView(this);
            image.setImageResource(icons[i]);
            image.setContentDescription(labels[i]);
            image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            item.addView(image, new LinearLayout.LayoutParams(dp(48), dp(48)));
            TextView name = text(labels[i], 11, NAVY, true);
            name.setGravity(Gravity.CENTER);
            item.addView(name, sectionTop(8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(88), 1);
            if (i > 0) lp.leftMargin = dp(10);
            row.addView(item, lp);
            item.setContentDescription(labels[i]);
        }
        content.addView(row, sectionTop(10));
    }

    private LinearLayout agendaContainer;
    private void agendaSection() {
        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.addView(text("Agenda", 17, NAVY, true), new LinearLayout.LayoutParams(0, -2, 1));
        todayChip = chip("Hoy", true);
        weekChip = chip("Semana", false);
        todayChip.setOnClickListener(v -> { filtro = "Hoy"; renderAgenda(); });
        weekChip.setOnClickListener(v -> { filtro = "Semana"; renderAgenda(); });
        heading.addView(todayChip);
        LinearLayout.LayoutParams weekLp = new LinearLayout.LayoutParams(-2, dp(32));
        weekLp.leftMargin = dp(6);
        heading.addView(weekChip, weekLp);
        agendaHeading = heading;
        content.addView(heading, sectionTop(18));
        agendaContainer = new LinearLayout(this);
        agendaContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(agendaContainer, sectionTop(10));
    }

    private void renderAgenda() {
        if (agendaContainer == null) return;
        agendaContainer.removeAllViews();
        updateFilterChips();
        LocalDate today = LocalDate.now();
        int shown = 0, confirmed = 0, pending = 0;
        for (Cita cita : citas) {
            LocalDate date = parseDate(cita.fecha);
            boolean inRange = date != null && (filtro.equals("Semana")
                    ? !date.isBefore(today) && !date.isAfter(today.plusDays(6))
                    : date.equals(today));
            if (cita.estado.equalsIgnoreCase("CONFIRMADA")) confirmed++;
            if (cita.estado.equalsIgnoreCase("PENDIENTE") || cita.estado.equalsIgnoreCase("PROGRAMADA")) pending++;
            if (!inRange) continue;
            shown++;
            LinearLayout item = card(22, 12, 12);
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout dateBlock = card(14, 5, 5);
            dateBlock.setBackground(roundGradient(14, 0xffe3f6f7, 0xffe3f6f7));
            dateBlock.addView(text(formatTime(cita.hora), 14, TEAL, true));
            dateBlock.addView(text(shortDate(cita.fecha), 10, MUTED, true));
            row.addView(dateBlock, new LinearLayout.LayoutParams(dp(64), dp(50)));
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(10), 0, 0, 0);
            info.addView(text(cita.paciente, 14, NAVY, true));
            info.addView(text("Consulta médica", 12, MUTED, false), sectionTop(3));
            row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            TextView status = text(cita.estado, 10, statusText(cita.estado), true);
            status.setPadding(dp(8), dp(5), dp(8), dp(5));
            status.setBackground(roundGradient(14, statusFill(cita.estado), statusFill(cita.estado)));
            row.addView(status);
            item.addView(row);
            item.setOnClickListener(v -> Toast.makeText(this, cita.paciente, Toast.LENGTH_SHORT).show());
            LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(-1, -2);
            itemLp.bottomMargin = dp(12);
            agendaContainer.addView(item, itemLp);
        }
        setCounter(0, String.valueOf(todayCount(today)));
        setCounter(1, String.valueOf(confirmed));
        setCounter(2, String.valueOf(pending));
        if (shown == 0) {
            TextView empty = text(filtro.equals("Hoy") ? "No tienes citas hoy" : "Aún no hay citas para mostrar", 14, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(18), 0, dp(18));
            agendaContainer.addView(empty);
            if (filtro.equals("Hoy")) {
                TextView viewWeek = text("Ver citas de la semana", 12, TEAL, true);
                viewWeek.setGravity(Gravity.CENTER);
                viewWeek.setPadding(dp(12), dp(8), dp(12), dp(12));
                viewWeek.setOnClickListener(v -> { filtro = "Semana"; renderAgenda(); });
                agendaContainer.addView(viewWeek);
            }
        }
        setNext(findClosestFutureAppointment());
    }

    private int todayCount(LocalDate today) {
        int count = 0;
        for (Cita cita : citas) if (today.equals(parseDate(cita.fecha))) count++;
        return count;
    }

    private void setCounter(int index, String value) {
        if (countersRow != null && countersRow.getChildCount() > index) {
            ((TextView) ((LinearLayout) countersRow.getChildAt(index)).getChildAt(0)).setText(value);
        }
    }

    private void setNext(Cita cita) {
        if (cita == null) { setNextEmpty(); return; }
        nextStatus.setVisibility(View.VISIBLE);
        nextPatientRow.setVisibility(View.VISIBLE);
        nextActions.setVisibility(View.VISIBLE);
        nextTime.setVisibility(View.VISIBLE);
        nextPatient.setText(cita.paciente);
        nextTime.setText(formatLongDate(cita.fecha) + " · " + formatTime(cita.hora));
        nextStatus.setText("● " + cita.estado);
        nextStatus.setTextColor(statusText(cita.estado));
        nextStatus.setBackground(roundGradient(12, statusFill(cita.estado), statusFill(cita.estado)));
    }

    private void setNextEmpty() {
        nextStatus.setVisibility(View.GONE);
        nextPatientRow.setVisibility(View.VISIBLE);
        nextPatient.setText("Aún no hay una próxima cita para mostrar");
        nextTime.setVisibility(View.GONE);
        nextActions.setVisibility(View.GONE);
    }

    private Cita findClosestFutureAppointment() {
        LocalDateTime now = LocalDateTime.now();
        Cita closest = null;
        LocalDateTime closestDateTime = null;
        for (Cita cita : citas) {
            if ("CANCELADA".equalsIgnoreCase(cita.estado)) continue;
            try {
                LocalDate date = parseDate(cita.fecha);
                LocalTime time = LocalTime.parse(cita.hora.trim().substring(0, 5));
                LocalDateTime dateTime = LocalDateTime.of(date, time);
                if (!dateTime.isBefore(now) && (closestDateTime == null || dateTime.isBefore(closestDateTime))) {
                    closest = cita;
                    closestDateTime = dateTime;
                }
            } catch (Exception ignored) { }
        }
        return closest;
    }

    private void cargarCitas() {
        new Thread(() -> {
            try {
                JSONArray rows = new JSONArray(ApiClient.solicitar(this, "/api/personal/agenda", null));
                for (int i=0;i<rows.length();i++) {
                    String[] parts = rows.getString(i).split("\\n", 2);
                    String[] dateTime = parts[0].split(" · ", 2);
                    String[] patientStatus = parts.length > 1 ? parts[1].split(" · ", 2) : new String[]{"", ""};
                    citas.add(new Cita(dateTime[0].trim(), dateTime.length > 1 ? dateTime[1].trim() : "",
                            patientStatus[0].trim(), patientStatus.length > 1 ? patientStatus[1].trim() : "PROGRAMADA"));
                }
                runOnUiThread(this::renderAgenda);
            } catch (Exception error) {
                runOnUiThread(() -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void sectionHeading(String title, String link) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(text(title, 17, NAVY, true), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(text(link, 13, TEAL, true));
        content.addView(row, sectionTop(18));
    }

    private LinearLayout bottomBar(FrameLayout root, boolean nursing) {
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER);
        bar.setClipChildren(false);
        bar.setClipToPadding(false);
        bar.setBackground(roundTop(28, Color.WHITE));
        bar.setElevation(dp(12));
        String[] labels = nursing ? new String[]{"Inicio", "Pacientes", "+", "Tareas", "Perfil"} : new String[]{"Inicio", "Agenda", "+", "Pacientes", "Perfil"};
        int[] icons = {R.drawable.icono_inicio, nursing ? R.drawable.familia : R.drawable.citasicono, 0, nursing ? R.drawable.outline_assignment_24 : R.drawable.familia, R.drawable.perfil};
        for (int i=0;i<labels.length;i++) {
            final int item = i;
            LinearLayout tab = new LinearLayout(this);
            tab.setGravity(Gravity.CENTER);
            tab.setOrientation(LinearLayout.VERTICAL);
            if (i == 2) {
                TextView plus = text("+", 28, Color.WHITE, true);
                plus.setGravity(Gravity.CENTER);
                GradientDrawable plusBg = roundGradient(40, 0xff2fd0d8, 0xff0c7a89);
                plusBg.setStroke(dp(5), 0xffeaf8f8);
                plus.setBackground(plusBg);
                LinearLayout.LayoutParams plusLp = new LinearLayout.LayoutParams(dp(64), dp(64));
                if (!nursing) tab.setTranslationY(-dp(28));
                tab.addView(plus, plusLp);
                plus.setContentDescription("Agregar");
            } else {
                ImageView image = new ImageView(this);
                image.setImageResource(icons[i]);
                image.setContentDescription(labels[i]);
                tab.addView(image, new LinearLayout.LayoutParams(dp(24), dp(24)));
                TextView name = text(labels[i], 11, i == 0 ? TEAL : MUTED, true);
                name.setMaxLines(1);
                name.setGravity(Gravity.CENTER);
                name.setIncludeFontPadding(false);
                LinearLayout.LayoutParams caption = new LinearLayout.LayoutParams(-1, -2);
                caption.topMargin = dp(4);
                tab.addView(name, caption);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(64), 1);
            if (i == 0) tab.setBackground(roundGradient(18, 0xffd9f3f4, 0xffd9f3f4));
            tab.setOnClickListener(v -> {
                if (item == 4) showProfile();
                else if (nursing) {
                    if (item == 2) showNursingShortcuts();
                    else scrollView.smoothScrollTo(0, item == 0 ? 0 :
                            (item == 1 ? nursingPatients : nursingTasks).getTop());
                }
                else if (item == 1) scrollToAgenda();
                else if (item == 2) Toast.makeText(this, "Aún no hay una consulta para iniciar.", Toast.LENGTH_SHORT).show();
                else if (item == 3) Toast.makeText(this, "Pacientes", Toast.LENGTH_SHORT).show();
            });
            tab.setPadding(dp(2), 0, dp(2), 0);
            bar.addView(tab, lp);
        }
        FrameLayout.LayoutParams barParams = new FrameLayout.LayoutParams(-1, dp(84), Gravity.BOTTOM);
        root.addView(bar, barParams);
        return bar;
    }

    private void showProfile() {
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Perfil")
                .setMessage(sessionName()).setNegativeButton("Volver", null)
                .setPositiveButton("Cerrar sesión", (d, w) -> {
                    getSharedPreferences("sesion_meditrack", MODE_PRIVATE).edit().clear().apply(); finish();
                }).show();
    }

    private void scrollToAgenda() {
        if (agendaHeading != null) scrollView.smoothScrollTo(0, agendaHeading.getTop());
    }
    private String sessionName() { return getSharedPreferences("sesion_meditrack", MODE_PRIVATE).getString("nombres", ""); }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL), bold ? Typeface.BOLD : Typeface.NORMAL);
        return view;
    }
    private LinearLayout card(int radius, int padH, int padV) {
        LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL);
        view.setPadding(dp(padH), dp(padV), dp(padH), dp(padV));
        view.setBackground(roundGradient(radius, Color.WHITE, Color.WHITE)); view.setElevation(dp(4)); return view;
    }
    private GradientDrawable roundGradient(int radius, int start, int end) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start,end});
        d.setCornerRadius(dp(radius)); return d;
    }
    private GradientDrawable roundTop(int radius, int color) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color);
        d.setCornerRadii(new float[]{dp(radius),dp(radius),dp(radius),dp(radius),0,0,0,0}); return d;
    }
    private GradientDrawable circle(int color) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setShape(GradientDrawable.OVAL); return d; }
    private LinearLayout.LayoutParams sectionTop(int margin) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.topMargin=dp(margin); return p; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private Button chip(String title, boolean selected) {
        Button button = new Button(this); button.setText(title); button.setTextSize(11); button.setAllCaps(false);
        button.setTypeface(null,Typeface.BOLD); button.setTextColor(selected ? Color.WHITE : MUTED);
        button.setBackground(roundGradient(16, selected ? 0xff0c7a89 : Color.WHITE, selected ? 0xff0c7a89 : Color.WHITE));
        button.setMinHeight(dp(32)); button.setMinimumHeight(dp(32)); button.setPadding(dp(12),0,dp(12),0); button.setStateListAnimator(null); return button;
    }
    private void updateFilterChips() {
        if (todayChip == null || weekChip == null) return;
        boolean today = filtro.equals("Hoy");
        styleChip(todayChip, today);
        styleChip(weekChip, !today);
    }
    private void styleChip(Button button, boolean selected) {
        int color = selected ? Color.WHITE : MUTED;
        button.setTextColor(color);
        button.setBackground(roundGradient(16, selected ? 0xff0c7a89 : Color.WHITE, selected ? 0xff0c7a89 : Color.WHITE));
    }
    private LocalDate parseDate(String value) {
        try { return LocalDate.parse(value.substring(0,10), DateTimeFormatter.ISO_LOCAL_DATE); }
        catch(Exception ignored) { return null; }
    }
    private String shortDate(String value) { try { return LocalDate.parse(value.substring(0,10)).format(DateTimeFormatter.ofPattern("dd/MM", new Locale("es"))); } catch(Exception e) { return value; } }
    private String formatTime(String value) { try { return LocalTime.parse(value.trim().substring(0,5)).format(DateTimeFormatter.ofPattern("HH:mm")); } catch(Exception e) { return value; } }
    private String formatLongDate(String value) {
        try { return LocalDate.parse(value.substring(0,10)).format(DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es"))); }
        catch(Exception e) { return value; }
    }
    private ViewOutlineProvider ovalOutline() {
        return new ViewOutlineProvider() {
            @Override public void getOutline(View view, Outline outline) { outline.setOval(0,0,view.getWidth(),view.getHeight()); }
        };
    }
    private class SoftSpotsView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        SoftSpotsView() { super(PanelPersonal.this); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float density = getResources().getDisplayMetrics().density;
            paint.setShader(new RadialGradient(getWidth()-dp(45), dp(15), dp(340), new int[]{0x66b5e8ec,0x00b5e8ec}, null, Shader.TileMode.CLAMP));
            canvas.drawCircle(getWidth()-dp(45), dp(15), dp(340), paint);
            paint.setShader(new RadialGradient(dp(10), getHeight()/2f, dp(320), new int[]{0x55cdeff0,0x00cdeff0}, null, Shader.TileMode.CLAMP));
            canvas.drawCircle(dp(10), getHeight()/2f, dp(320), paint);
            paint.setShader(null);
        }
    }
    private int statusFill(String state) {
        if (state.equalsIgnoreCase("CONFIRMADA")) return 0xffd6f5ea;
        if (state.equalsIgnoreCase("PENDIENTE")) return 0xffffefcf;
        if (state.equalsIgnoreCase("CANCELADA")) return 0xffffe3de;
        return 0xffe3edff;
    }
    private int statusText(String state) {
        if (state.equalsIgnoreCase("CONFIRMADA")) return 0xff0b7a5a;
        if (state.equalsIgnoreCase("PENDIENTE")) return 0xff9a5b00;
        if (state.equalsIgnoreCase("CANCELADA")) return 0xffb83a26;
        return 0xff2349b0;
    }
}
