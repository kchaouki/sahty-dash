package com.sahty.fs.views.patient;

import com.sahty.fs.entity.Admission;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.AdmissionService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;

@Route(value = "admissions", layout = MainLayout.class)
@PageTitle("Admissions | Sahty EMR")
@PermitAll
public class AdmissionListView extends VerticalLayout {

    private final AdmissionService admissionService;
    private final Grid<Admission> grid;
    private final ComboBox<Admission.AdmissionStatus> statusFilter;
    private String tenantId;

    public AdmissionListView(AdmissionService admissionService) {
        this.admissionService = admissionService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Gestion des Admissions");

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(Admission.AdmissionStatus.values());
        statusFilter.setItemLabelGenerator(s -> switch(s) {
            case EN_COURS -> "En Cours";
            case SORTI -> "Sorti";
            case ANNULE -> "Annulé";
        });
        statusFilter.setClearButtonVisible(true);
        statusFilter.setValue(Admission.AdmissionStatus.EN_COURS);
        statusFilter.addValueChangeListener(e -> refreshGrid());

        Button refreshBtn = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshBtn.addClickListener(e -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(statusFilter, refreshBtn);

        grid = createGrid();
        grid.setSizeFull();

        refreshGrid();
        add(title, toolbar, grid);
    }

    private Grid<Admission> createGrid() {
        Grid<Admission> grid = new Grid<>(Admission.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        grid.addColumn(Admission::getNda).setHeader("NDA").setWidth("160px").setFlexGrow(0);
        grid.addColumn(a -> a.getPatient() != null ? a.getPatient().getFullName() : "")
                .setHeader("Patient").setSortable(true).setFlexGrow(1);
        grid.addColumn(a -> a.getService() != null ? a.getService().getName() : "")
                .setHeader("Service");
        grid.addColumn(Admission::getDoctorName).setHeader("Médecin");
        grid.addColumn(a -> a.getBedLabel() != null ? a.getBedLabel() : "")
                .setHeader("Lit");
        grid.addColumn(a -> a.getAdmissionDate() != null
                ? a.getAdmissionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                .setHeader("Date Admission");
        grid.addColumn(new ComponentRenderer<>(admission -> {
            Span badge = new Span(switch(admission.getStatus()) {
                case EN_COURS -> "En Cours";
                case SORTI -> "Sorti";
                case ANNULE -> "Annulé";
            });
            badge.getElement().getThemeList().add("badge");
            switch(admission.getStatus()) {
                case EN_COURS -> badge.getElement().getThemeList().add("success");
                case SORTI -> badge.getElement().getThemeList().add("contrast");
                case ANNULE -> badge.getElement().getThemeList().add("error");
            }
            return badge;
        })).setHeader("Statut");

        grid.addColumn(new ComponentRenderer<>(admission -> {
            Button viewBtn = new Button(VaadinIcon.FOLDER_OPEN.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            viewBtn.addClickListener(e ->
                    getUI().ifPresent(ui -> ui.navigate(AdmissionDossierView.class, admission.getId())));
            return viewBtn;
        })).setHeader("Dossier").setWidth("80px").setFlexGrow(0);

        return grid;
    }

    private void refreshGrid() {
        Admission.AdmissionStatus status = statusFilter.getValue();
        if (status == Admission.AdmissionStatus.EN_COURS) {
            grid.setItems(admissionService.findActiveAdmissions(tenantId));
        } else {
            grid.setItems(admissionService.findAdmissions(tenantId, status, 0, 100).getContent());
        }
    }
}
