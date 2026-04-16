package com.sahty.fs.views.patient;

import com.sahty.fs.entity.Admission;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.AdmissionService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "waiting-room", layout = MainLayout.class)
@PageTitle("Salle d'Attente | Sahty EMR")
@PermitAll
public class WaitingRoomView extends VerticalLayout {

    private final AdmissionService admissionService;
    private final String tenantId;
    private Grid<Admission> grid;
    private List<Admission> allAdmissions;

    public WaitingRoomView(AdmissionService admissionService) {
        this.admissionService = admissionService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Salle d'Attente — Admissions en Cours");

        TextField searchField = new TextField();
        searchField.setPlaceholder("Filtrer par patient, service, médecin...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("320px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterGrid(e.getValue()));

        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> loadData());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, refreshBtn);
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // Summary bar
        HorizontalLayout summary = buildSummaryBar();

        grid = buildGrid();
        add(title, toolbar, summary, grid);
        loadData();
    }

    private HorizontalLayout buildSummaryBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.getStyle().set("background", "var(--lumo-contrast-5pct)");
        bar.getStyle().set("padding", "8px 16px");
        bar.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        long active = admissionService.countActiveAdmissions(tenantId);

        Span admissionsCount = new Span("Admissions actives: " + active);
        admissionsCount.getStyle().set("font-weight", "600");

        bar.add(admissionsCount);
        return bar;
    }

    private Grid<Admission> buildGrid() {
        Grid<Admission> g = new Grid<>(Admission.class, false);
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        g.setSizeFull();

        g.addColumn(a -> a.getPatient() != null ? a.getPatient().getFullName() : "-")
                .setHeader("Patient").setFlexGrow(1);
        g.addColumn(Admission::getNda).setHeader("NDA").setWidth("150px").setFlexGrow(0);
        g.addColumn(a -> a.getService() != null ? a.getService().getName() : "-")
                .setHeader("Service").setWidth("150px").setFlexGrow(0);
        g.addColumn(Admission::getDoctorName).setHeader("Médecin Référent").setWidth("170px").setFlexGrow(0);
        g.addColumn(Admission::getBedLabel).setHeader("Lit").setWidth("80px").setFlexGrow(0);
        g.addColumn(a -> a.getAdmissionDate() != null
                ? a.getAdmissionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-")
                .setHeader("Admis le").setWidth("150px").setFlexGrow(0);
        g.addColumn(new ComponentRenderer<>(a -> {
            if (a.getAdmissionDate() == null) return new Span("-");
            Duration duration = Duration.between(a.getAdmissionDate(), LocalDateTime.now());
            long days = duration.toDays();
            long hours = duration.toHoursPart();
            String label = days > 0 ? days + "j " + hours + "h" : hours + "h " + duration.toMinutesPart() + "min";
            Span s = new Span(label);
            if (days > 10) s.getElement().getThemeList().add("badge error");
            else if (days > 5) s.getElement().getThemeList().add("badge contrast");
            else s.getElement().getThemeList().add("badge success");
            return s;
        })).setHeader("Durée Séjour").setWidth("130px").setFlexGrow(0);
        g.addColumn(new ComponentRenderer<>(a -> {
            Span badge = new Span(a.getType() != null ? a.getType().name() : "-");
            badge.getElement().getThemeList().add("badge");
            return badge;
        })).setHeader("Type").setWidth("110px").setFlexGrow(0);
        g.addColumn(new ComponentRenderer<>(a -> {
            Button viewBtn = new Button(VaadinIcon.FOLDER_OPEN.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewBtn.getElement().setAttribute("title", "Ouvrir le dossier d'admission");
            viewBtn.addClickListener(e ->
                    getUI().ifPresent(ui -> ui.navigate(AdmissionDossierView.class, a.getId())));
            return viewBtn;
        })).setHeader("").setWidth("60px").setFlexGrow(0);

        return g;
    }

    private void loadData() {
        allAdmissions = admissionService.findActiveAdmissions(tenantId);
        grid.setItems(allAdmissions);
    }

    private void filterGrid(String query) {
        if (allAdmissions == null) return;
        if (query == null || query.isBlank()) {
            grid.setItems(allAdmissions);
            return;
        }
        String q = query.toLowerCase();
        grid.setItems(allAdmissions.stream().filter(a ->
                (a.getPatient() != null && a.getPatient().getFullName().toLowerCase().contains(q)) ||
                (a.getNda() != null && a.getNda().toLowerCase().contains(q)) ||
                (a.getService() != null && a.getService().getName().toLowerCase().contains(q)) ||
                (a.getDoctorName() != null && a.getDoctorName().toLowerCase().contains(q))
        ).collect(Collectors.toList()));
    }
}
