package com.sahty.fs.views.patient;

import com.sahty.fs.entity.Admission;
import com.sahty.fs.entity.HospitalService;
import com.sahty.fs.entity.Patient;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.AdmissionService;
import com.sahty.fs.service.HospitalServiceService;
import com.sahty.fs.service.PatientService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "admissions", layout = MainLayout.class)
@PageTitle("Admissions | Sahty EMR")
@PermitAll
public class AdmissionListView extends VerticalLayout {

    private final AdmissionService admissionService;
    private final PatientService patientService;
    private final HospitalServiceService hospitalServiceService;
    private final Grid<Admission> grid;
    private final ComboBox<Admission.AdmissionStatus> statusFilter;
    private String tenantId;

    public AdmissionListView(AdmissionService admissionService,
                              PatientService patientService,
                              HospitalServiceService hospitalServiceService) {
        this.admissionService = admissionService;
        this.patientService = patientService;
        this.hospitalServiceService = hospitalServiceService;
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

        Button newBtn = new Button("Nouvelle Admission", VaadinIcon.PLUS.create());
        newBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newBtn.addClickListener(e -> openAdmissionDialog());

        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(newBtn, statusFilter, refreshBtn);
        toolbar.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

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
        if (status == null || status == Admission.AdmissionStatus.EN_COURS) {
            grid.setItems(admissionService.findActiveAdmissions(tenantId));
        } else {
            grid.setItems(admissionService.findAdmissions(tenantId, status, 0, 200).getContent());
        }
    }

    private void openAdmissionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nouvelle Admission");
        dialog.setWidth("700px");

        // Patient search combo
        ComboBox<Patient> patientBox = new ComboBox<>("Patient");
        patientBox.setItemLabelGenerator(p -> p.getLastName() + " " + p.getFirstName()
                + (p.getIpp() != null ? " [" + p.getIpp() + "]" : ""));
        patientBox.setWidth("100%");
        patientBox.setRequired(true);
        patientBox.setItems(
            query -> patientService.searchPatients(tenantId,
                query.getFilter().orElse(""),
                query.getOffset() / Math.max(query.getLimit(), 1),
                query.getLimit()).stream()
        );

        // Service combo
        ComboBox<HospitalService> serviceBox = new ComboBox<>("Service");
        serviceBox.setItemLabelGenerator(HospitalService::getName);
        serviceBox.setWidth("100%");
        List<HospitalService> services = hospitalServiceService.findByTenant(tenantId);
        serviceBox.setItems(services);

        // Type combo
        ComboBox<Admission.AdmissionType> typeBox = new ComboBox<>("Type d'Admission");
        typeBox.setItems(Admission.AdmissionType.values());
        typeBox.setItemLabelGenerator(t -> switch(t) {
            case HOSPITALISATION -> "Hospitalisation";
            case AMBULATOIRE -> "Ambulatoire";
            case URGENCE -> "Urgence";
            case CHIRURGIE -> "Chirurgie";
        });
        typeBox.setValue(Admission.AdmissionType.HOSPITALISATION);

        // Arrival mode
        ComboBox<Admission.ArrivalMode> arrivalBox = new ComboBox<>("Mode d'Arrivée");
        arrivalBox.setItems(Admission.ArrivalMode.values());
        arrivalBox.setItemLabelGenerator(a -> switch(a) {
            case AMBULANCE -> "Ambulance";
            case PERSONNEL -> "Personnelle";
            case TRANSFERT -> "Transfert";
            case SMUR -> "SMUR";
        });

        TextField doctorField = new TextField("Médecin Responsable");
        TextField bedField = new TextField("Lit");
        TextField reasonField = new TextField("Motif d'Admission");
        DateTimePicker admissionDatePicker = new DateTimePicker("Date d'Admission");
        admissionDatePicker.setValue(LocalDateTime.now());

        FormLayout form = new FormLayout(patientBox, serviceBox, typeBox, arrivalBox,
                doctorField, bedField, reasonField, admissionDatePicker);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("450px", 2));
        form.setColspan(patientBox, 2);
        form.setColspan(reasonField, 2);

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button saveBtn = new Button("Admettre", e -> {
            if (patientBox.getValue() == null) {
                Notification.show("Veuillez sélectionner un patient", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                Admission admission = new Admission();
                admission.setType(typeBox.getValue());
                admission.setArrivalMode(arrivalBox.getValue());
                admission.setDoctorName(doctorField.getValue());
                admission.setBedLabel(bedField.getValue());
                admission.setReason(reasonField.getValue());
                admission.setAdmissionDate(admissionDatePicker.getValue());
                if (serviceBox.getValue() != null) {
                    admission.setService(serviceBox.getValue());
                }
                admissionService.createAdmission(admission, patientBox.getValue().getId(), tenantId);
                dialog.close();
                refreshGrid();
                Notification.show("Admission créée avec succès", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }
}
