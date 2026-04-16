package com.sahty.fs.views.patient;

import com.sahty.fs.entity.*;
import com.sahty.fs.service.AdmissionService;
import com.sahty.fs.service.PrescriptionService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Route(value = "admissions/:admissionId", layout = MainLayout.class)
@PageTitle("Dossier d'Admission | Sahty EMR")
@PermitAll
public class AdmissionDossierView extends VerticalLayout implements HasUrlParameter<String> {

    private final AdmissionService admissionService;
    private final PrescriptionService prescriptionService;
    private Admission admission;

    public AdmissionDossierView(AdmissionService admissionService, PrescriptionService prescriptionService) {
        this.admissionService = admissionService;
        this.prescriptionService = prescriptionService;
        setSizeFull();
        setPadding(true);
    }

    @Override
    public void setParameter(BeforeEvent event, String admissionId) {
        Optional<Admission> found = admissionService.findById(admissionId);
        if (found.isEmpty()) {
            Notification.show("Admission non trouvée", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.forwardTo(AdmissionListView.class);
            return;
        }
        this.admission = found.get();
        buildUI();
    }

    private void buildUI() {
        removeAll();

        RouterLink backLink = new RouterLink("← Admissions", AdmissionListView.class);

        VerticalLayout header = buildHeader();

        TabSheet tabs = buildTabs();
        tabs.setSizeFull();

        add(backLink, header, tabs);
    }

    private VerticalLayout buildHeader() {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.getStyle().set("background", "var(--lumo-base-color)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        card.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");

        Patient patient = admission.getPatient();
        H2 title = new H2(patient != null ? patient.getFullName() : "Patient");

        Span nda = new Span("NDA: " + nvl(admission.getNda()));
        nda.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span status = new Span(admission.getStatus().name());
        status.getElement().getThemeList().add("badge");
        if (admission.getStatus() == Admission.AdmissionStatus.EN_COURS) {
            status.getElement().getThemeList().add("success");
        } else if (admission.getStatus() == Admission.AdmissionStatus.ANNULE) {
            status.getElement().getThemeList().add("error");
        }

        HorizontalLayout titleRow = new HorizontalLayout(title, nda, status);

        HorizontalLayout infoRow = new HorizontalLayout();
        infoRow.add(
            infoChip("Service", admission.getService() != null ? admission.getService().getName() : "-"),
            infoChip("Médecin", nvl(admission.getDoctorName())),
            infoChip("Lit", nvl(admission.getBedLabel())),
            infoChip("Admission", admission.getAdmissionDate() != null
                    ? admission.getAdmissionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-")
        );

        HorizontalLayout actions = new HorizontalLayout();
        if (admission.getStatus() == Admission.AdmissionStatus.EN_COURS) {
            Button closeBtn = new Button("Clôturer l'Admission", VaadinIcon.CHECK_CIRCLE.create());
            closeBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            closeBtn.addClickListener(e -> confirmClose());
            actions.add(closeBtn);

            Button cancelBtn = new Button("Annuler", VaadinIcon.CLOSE.create());
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            cancelBtn.addClickListener(e -> confirmCancel());
            actions.add(cancelBtn);
        }

        card.add(titleRow, infoRow, actions);
        return card;
    }

    private TabSheet buildTabs() {
        TabSheet tabs = new TabSheet();

        tabs.add(new Tab("Examen Clinique"), buildClinicalExamTab());
        tabs.add(new Tab("Observations"), buildObservationsTab());
        tabs.add(new Tab("Prescriptions"), buildPrescriptionsTab());
        tabs.add(new Tab("Transfusions"), buildTransfusionsTab());
        tabs.add(new Tab("Interventions"), buildInterventionsTab());
        tabs.add(new Tab("Biologie"), buildBiologyTab());

        return tabs;
    }

    private VerticalLayout buildClinicalExamTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        Button addBtn = new Button("Nouvel Examen Clinique", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openClinicalExamDialog());

        if (!admission.getClinicalExams().isEmpty()) {
            Grid<ClinicalExam> grid = new Grid<>(ClinicalExam.class, false);
            grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
            grid.addColumn(e -> e.getCreatedAt() != null
                    ? e.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                    .setHeader("Date").setWidth("160px").setFlexGrow(0);
            grid.addColumn(e -> e.getTemperature() != null ? e.getTemperature() + " °C" : "-")
                    .setHeader("Temp.").setWidth("80px").setFlexGrow(0);
            grid.addColumn(e -> e.getHeartRate() != null ? e.getHeartRate() + " bpm" : "-")
                    .setHeader("FC").setWidth("90px").setFlexGrow(0);
            grid.addColumn(e -> (e.getSystolicBP() != null && e.getDiastolicBP() != null)
                    ? e.getSystolicBP() + "/" + e.getDiastolicBP() + " mmHg" : "-")
                    .setHeader("PA").setWidth("120px").setFlexGrow(0);
            grid.addColumn(e -> e.getOxygenSaturation() != null ? e.getOxygenSaturation() + " %" : "-")
                    .setHeader("SpO2").setWidth("80px").setFlexGrow(0);
            grid.addColumn(e -> e.getRespiratoryRate() != null ? e.getRespiratoryRate() + "/min" : "-")
                    .setHeader("FR").setWidth("80px").setFlexGrow(0);
            grid.addColumn(e -> e.getWeight() != null ? e.getWeight() + " kg" : "-")
                    .setHeader("Poids").setWidth("80px").setFlexGrow(0);
            grid.addColumn(e -> e.getHeight() != null ? e.getHeight() + " cm" : "-")
                    .setHeader("Taille").setWidth("80px").setFlexGrow(0);
            grid.addColumn(ClinicalExam::getPainScore).setHeader("Douleur").setWidth("80px").setFlexGrow(0);
            grid.addColumn(ClinicalExam::getGeneralState).setHeader("État Général").setFlexGrow(1);
            grid.setItems(admission.getClinicalExams());
            grid.setHeight("300px");
            layout.add(addBtn, grid);
        } else {
            layout.add(addBtn, new Paragraph("Aucun examen clinique enregistré."));
        }

        return layout;
    }

    private void openClinicalExamDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nouvel Examen Clinique");
        dialog.setWidth("700px");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));

        NumberField tempField = new NumberField("Température (°C)");
        tempField.setMin(30); tempField.setMax(45);
        IntegerField hrField = new IntegerField("FC (bpm)");
        IntegerField sysBPField = new IntegerField("PA Systolique");
        IntegerField diaBPField = new IntegerField("PA Diastolique");
        IntegerField rrField = new IntegerField("Fréq. Respiratoire");
        IntegerField spo2Field = new IntegerField("SpO2 (%)");
        spo2Field.setMin(0); spo2Field.setMax(100);
        NumberField weightField = new NumberField("Poids (kg)");
        NumberField heightField = new NumberField("Taille (cm)");
        TextField painField = new TextField("Score Douleur (0-10)");

        TextArea generalStateField = new TextArea("État Général");
        generalStateField.setWidthFull();
        TextArea cardiacField = new TextArea("Examen Cardiaque");
        cardiacField.setWidthFull();
        TextArea pulmonaryField = new TextArea("Examen Pulmonaire");
        pulmonaryField.setWidthFull();
        TextArea abdominalField = new TextArea("Examen Abdominal");
        abdominalField.setWidthFull();
        TextArea neuroField = new TextArea("Examen Neurologique");
        neuroField.setWidthFull();
        TextArea otherField = new TextArea("Autres Constatations");
        otherField.setWidthFull();

        form.add(tempField, hrField, sysBPField, diaBPField, rrField, spo2Field, weightField, heightField, painField);
        form.add(generalStateField, cardiacField, pulmonaryField, abdominalField, neuroField, otherField);
        form.setColspan(generalStateField, 3);
        form.setColspan(cardiacField, 3);
        form.setColspan(pulmonaryField, 3);
        form.setColspan(abdominalField, 3);
        form.setColspan(neuroField, 3);
        form.setColspan(otherField, 3);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            ClinicalExam exam = new ClinicalExam();
            exam.setAdmission(admission);
            exam.setTemperature(tempField.getValue());
            exam.setHeartRate(hrField.getValue());
            exam.setSystolicBP(sysBPField.getValue());
            exam.setDiastolicBP(diaBPField.getValue());
            exam.setRespiratoryRate(rrField.getValue());
            exam.setOxygenSaturation(spo2Field.getValue());
            exam.setWeight(weightField.getValue());
            exam.setHeight(heightField.getValue());
            exam.setPainScore(painField.getValue());
            exam.setGeneralState(generalStateField.getValue());
            exam.setCardiacExam(cardiacField.getValue());
            exam.setPulmonaryExam(pulmonaryField.getValue());
            exam.setAbdominalExam(abdominalField.getValue());
            exam.setNeurologicalExam(neuroField.getValue());
            exam.setOtherFindings(otherField.getValue());
            admissionService.saveClinicalExam(exam);
            dialog.close();
            admission = admissionService.findById(admission.getId()).orElse(admission);
            buildUI();
            Notification.show("Examen enregistré", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private VerticalLayout buildObservationsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        Button addBtn = new Button("Nouvelle Observation", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openObservationDialog());

        Grid<Observation> grid = new Grid<>(Observation.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(new ComponentRenderer<>(o -> {
            Span badge = new Span(o.getType() != null ? o.getType().name() : "");
            badge.getElement().getThemeList().add("badge");
            if (o.getType() == Observation.ObservationType.MEDICAL) badge.getElement().getThemeList().add("contrast");
            return badge;
        })).setHeader("Type").setWidth("140px").setFlexGrow(0);
        grid.addColumn(o -> o.getAuthor() != null ? o.getAuthor().getFullName() : "").setHeader("Auteur").setWidth("180px").setFlexGrow(0);
        grid.addColumn(o -> o.getCreatedAt() != null
                ? o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                .setHeader("Date").setWidth("160px").setFlexGrow(0);
        grid.addColumn(Observation::getContent).setHeader("Contenu").setFlexGrow(1);
        grid.setItems(admission.getObservations());

        layout.add(addBtn, grid);
        return layout;
    }

    private void openObservationDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nouvelle Observation");
        dialog.setWidth("600px");

        FormLayout form = new FormLayout();
        ComboBox<Observation.ObservationType> typeField = new ComboBox<>("Type");
        typeField.setItems(Observation.ObservationType.values());
        typeField.setItemLabelGenerator(Observation.ObservationType::name);
        typeField.setRequired(true);

        TextArea contentField = new TextArea("Contenu");
        contentField.setMinHeight("150px");
        contentField.setWidthFull();
        contentField.setRequired(true);

        form.add(typeField, contentField);
        form.setColspan(contentField, 2);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (typeField.isEmpty() || contentField.isEmpty()) {
                Notification.show("Type et contenu sont obligatoires", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Observation obs = new Observation();
            obs.setAdmission(admission);
            obs.setType(typeField.getValue());
            obs.setContent(contentField.getValue());
            admissionService.saveObservation(obs);
            dialog.close();
            admission = admissionService.findById(admission.getId()).orElse(admission);
            buildUI();
            Notification.show("Observation enregistrée", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private VerticalLayout buildPrescriptionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        Button addBtn = new Button("Nouvelle Prescription", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openPrescriptionDialog());

        Grid<Prescription> grid = new Grid<>(Prescription.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(p -> p.getType().name()).setHeader("Type").setWidth("120px").setFlexGrow(0);
        grid.addColumn(Prescription::getMolecule).setHeader("Médicament / Acte").setFlexGrow(1);
        grid.addColumn(p -> p.getQty() != null ? p.getQty() + " " + nvl(p.getUnit()) : "-")
                .setHeader("Dose").setWidth("100px").setFlexGrow(0);
        grid.addColumn(Prescription::getRoute).setHeader("Voie").setWidth("100px").setFlexGrow(0);
        grid.addColumn(p -> p.getStartDateTime() != null
                ? p.getStartDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-")
                .setHeader("Début").setWidth("150px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(p -> {
            Span badge = new Span(p.getStatus().name());
            badge.getElement().getThemeList().add("badge");
            if (p.getStatus() == Prescription.PrescriptionStatus.ACTIVE) badge.getElement().getThemeList().add("success");
            else if (p.getStatus() == Prescription.PrescriptionStatus.STOPPED) badge.getElement().getThemeList().add("error");
            return badge;
        })).setHeader("Statut").setWidth("100px").setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(p -> {
            Button pauseBtn = new Button(VaadinIcon.PAUSE.create());
            pauseBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            pauseBtn.setEnabled(p.getStatus() == Prescription.PrescriptionStatus.ACTIVE);
            pauseBtn.addClickListener(ev -> {
                prescriptionService.pause(p.getId());
                admission = admissionService.findById(admission.getId()).orElse(admission);
                buildUI();
            });

            Button stopBtn = new Button(VaadinIcon.STOP.create());
            stopBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            stopBtn.setEnabled(p.getStatus() == Prescription.PrescriptionStatus.ACTIVE
                    || p.getStatus() == Prescription.PrescriptionStatus.PAUSED);
            stopBtn.addClickListener(ev -> {
                prescriptionService.stop(p.getId());
                admission = admissionService.findById(admission.getId()).orElse(admission);
                buildUI();
            });

            return new HorizontalLayout(pauseBtn, stopBtn);
        })).setHeader("Actions").setWidth("120px").setFlexGrow(0);

        grid.setItems(prescriptionService.findByAdmission(admission.getId()));
        layout.add(addBtn, grid);
        return layout;
    }

    private void openPrescriptionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nouvelle Prescription");
        dialog.setWidth("600px");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        ComboBox<Prescription.PrescriptionType> typeField = new ComboBox<>("Type");
        typeField.setItems(Prescription.PrescriptionType.values());
        typeField.setItemLabelGenerator(Prescription.PrescriptionType::name);
        typeField.setRequired(true);

        TextField moleculeField = new TextField("Médicament / Acte");
        moleculeField.setRequired(true);
        NumberField qtyField = new NumberField("Quantité / Dose");
        TextField unitField = new TextField("Unité");
        TextField routeField = new TextField("Voie d'administration");
        DateTimePicker startField = new DateTimePicker("Date de début");
        startField.setValue(LocalDateTime.now());
        IntegerField durationField = new IntegerField("Durée (jours)");
        TextArea notesField = new TextArea("Notes");
        notesField.setWidthFull();

        form.add(typeField, moleculeField, qtyField, unitField, routeField, startField, durationField, notesField);
        form.setColspan(notesField, 2);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (typeField.isEmpty() || moleculeField.isEmpty()) {
                Notification.show("Type et médicament sont obligatoires", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Prescription p = new Prescription();
            p.setAdmission(admission);
            p.setType(typeField.getValue());
            p.setMolecule(moleculeField.getValue());
            p.setQty(qtyField.getValue());
            p.setUnit(unitField.getValue());
            p.setRoute(routeField.getValue());
            p.setStartDateTime(startField.getValue());
            p.setDurationDays(durationField.getValue());
            p.setNotes(notesField.getValue());
            p.setStatus(Prescription.PrescriptionStatus.ACTIVE);
            prescriptionService.save(p);
            dialog.close();
            admission = admissionService.findById(admission.getId()).orElse(admission);
            buildUI();
            Notification.show("Prescription enregistrée", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private VerticalLayout buildTransfusionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        Button addBtn = new Button("Nouvelle Transfusion", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openTransfusionDialog());

        Grid<Transfusion> grid = new Grid<>(Transfusion.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.addColumn(Transfusion::getBloodProduct).setHeader("Produit Sanguin").setFlexGrow(1);
        grid.addColumn(Transfusion::getBloodGroup).setHeader("Groupe").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Transfusion::getRhFactor).setHeader("Rh").setWidth("60px").setFlexGrow(0);
        grid.addColumn(Transfusion::getBagNumber).setHeader("N° Poche").setWidth("120px").setFlexGrow(0);
        grid.addColumn(t -> t.getVolumeMl() != null ? t.getVolumeMl() + " mL" : "-")
                .setHeader("Volume").setWidth("90px").setFlexGrow(0);
        grid.addColumn(t -> t.getStartTime() != null
                ? t.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-")
                .setHeader("Début").setWidth("150px").setFlexGrow(0);
        grid.addColumn(t -> t.getEndTime() != null
                ? t.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-")
                .setHeader("Fin").setWidth("150px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(t -> {
            Span badge = new Span(t.getStatus().name());
            badge.getElement().getThemeList().add("badge");
            if (t.getStatus() == Transfusion.TransfusionStatus.COMPLETED) badge.getElement().getThemeList().add("success");
            else if (t.getStatus() == Transfusion.TransfusionStatus.ADVERSE_REACTION) badge.getElement().getThemeList().add("error");
            else if (t.getStatus() == Transfusion.TransfusionStatus.IN_PROGRESS) badge.getElement().getThemeList().add("contrast");
            return badge;
        })).setHeader("Statut").setWidth("140px").setFlexGrow(0);
        grid.addColumn(t -> t.isAdverseReaction() ? "OUI ⚠" : "Non")
                .setHeader("Réaction").setWidth("90px").setFlexGrow(0);
        grid.setItems(admission.getTransfusions());

        if (admission.getTransfusions().isEmpty()) {
            layout.add(addBtn, new Paragraph("Aucune transfusion enregistrée."));
        } else {
            layout.add(addBtn, grid);
        }
        return layout;
    }

    private void openTransfusionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nouvelle Transfusion");
        dialog.setWidth("600px");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField bloodProductField = new TextField("Produit Sanguin");
        bloodProductField.setRequired(true);
        ComboBox<String> bloodGroupField = new ComboBox<>("Groupe Sanguin");
        bloodGroupField.setItems("A", "B", "AB", "O");
        ComboBox<String> rhField = new ComboBox<>("Facteur Rh");
        rhField.setItems("RH+", "RH-");
        TextField bagNumberField = new TextField("N° de Poche");
        IntegerField volumeField = new IntegerField("Volume (mL)");
        DateTimePicker startField = new DateTimePicker("Heure Début");
        startField.setValue(LocalDateTime.now());
        DateTimePicker endField = new DateTimePicker("Heure Fin");
        TextArea preNotesField = new TextArea("Notes Pré-Transfusion");
        preNotesField.setWidthFull();
        TextArea postNotesField = new TextArea("Notes Post-Transfusion");
        postNotesField.setWidthFull();

        form.add(bloodProductField, bloodGroupField, rhField, bagNumberField, volumeField,
                startField, endField, preNotesField, postNotesField);
        form.setColspan(preNotesField, 2);
        form.setColspan(postNotesField, 2);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (bloodProductField.isEmpty()) {
                Notification.show("Le produit sanguin est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Transfusion t = new Transfusion();
            t.setAdmission(admission);
            t.setBloodProduct(bloodProductField.getValue());
            t.setBloodGroup(bloodGroupField.getValue());
            t.setRhFactor(rhField.getValue());
            t.setBagNumber(bagNumberField.getValue());
            t.setVolumeMl(volumeField.getValue());
            t.setStartTime(startField.getValue());
            t.setEndTime(endField.getValue());
            t.setPreTransfusionNotes(preNotesField.getValue());
            t.setPostTransfusionNotes(postNotesField.getValue());
            t.setStatus(Transfusion.TransfusionStatus.PLANNED);
            admissionService.saveTransfusion(t);
            dialog.close();
            admission = admissionService.findById(admission.getId()).orElse(admission);
            buildUI();
            Notification.show("Transfusion enregistrée", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private VerticalLayout buildInterventionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        Button addBtn = new Button("Nouvelle Intervention", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openInterventionDialog());

        Grid<Intervention> grid = new Grid<>(Intervention.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.addColumn(Intervention::getName).setHeader("Intervention").setFlexGrow(1);
        grid.addColumn(Intervention::getType).setHeader("Type").setWidth("120px").setFlexGrow(0);
        grid.addColumn(Intervention::getSurgeonName).setHeader("Chirurgien").setWidth("160px").setFlexGrow(0);
        grid.addColumn(Intervention::getAnesthesiologist).setHeader("Anesthésiste").setWidth("160px").setFlexGrow(0);
        grid.addColumn(Intervention::getAnesthesiaType).setHeader("Anesthésie").setWidth("120px").setFlexGrow(0);
        grid.addColumn(i -> i.getPlannedDate() != null
                ? i.getPlannedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-")
                .setHeader("Prévue le").setWidth("150px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(i -> {
            Span badge = new Span(i.getStatus().name());
            badge.getElement().getThemeList().add("badge");
            if (i.getStatus() == Intervention.InterventionStatus.COMPLETED) badge.getElement().getThemeList().add("success");
            else if (i.getStatus() == Intervention.InterventionStatus.CANCELLED) badge.getElement().getThemeList().add("error");
            else if (i.getStatus() == Intervention.InterventionStatus.IN_PROGRESS) badge.getElement().getThemeList().add("contrast");
            return badge;
        })).setHeader("Statut").setWidth("120px").setFlexGrow(0);
        grid.setItems(admission.getInterventions());

        if (admission.getInterventions().isEmpty()) {
            layout.add(addBtn, new Paragraph("Aucune intervention enregistrée."));
        } else {
            layout.add(addBtn, grid);
        }
        return layout;
    }

    private void openInterventionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nouvelle Intervention Chirurgicale");
        dialog.setWidth("650px");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField nameField = new TextField("Nom de l'Intervention");
        nameField.setRequired(true);
        TextField typeField = new TextField("Type");
        TextField surgeonField = new TextField("Chirurgien");
        TextField anesthField = new TextField("Anesthésiste");
        TextField anesthTypeField = new TextField("Type d'Anesthésie");
        TextField opRoomField = new TextField("Salle d'Opération");
        DateTimePicker plannedField = new DateTimePicker("Date Prévue");
        DateTimePicker startField = new DateTimePicker("Heure Début");
        DateTimePicker endField = new DateTimePicker("Heure Fin");
        TextArea preOpField = new TextArea("Notes Pré-Opératoires");
        preOpField.setWidthFull();
        TextArea opReportField = new TextArea("Compte-rendu Opératoire");
        opReportField.setWidthFull();
        TextArea postOpField = new TextArea("Notes Post-Opératoires");
        postOpField.setWidthFull();
        TextField complicationsField = new TextField("Complications");

        form.add(nameField, typeField, surgeonField, anesthField, anesthTypeField, opRoomField,
                plannedField, startField, endField, complicationsField, preOpField, opReportField, postOpField);
        form.setColspan(preOpField, 2);
        form.setColspan(opReportField, 2);
        form.setColspan(postOpField, 2);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (nameField.isEmpty()) {
                Notification.show("Le nom de l'intervention est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Intervention intervention = new Intervention();
            intervention.setAdmission(admission);
            intervention.setName(nameField.getValue());
            intervention.setType(typeField.getValue());
            intervention.setSurgeonName(surgeonField.getValue());
            intervention.setAnesthesiologist(anesthField.getValue());
            intervention.setAnesthesiaType(anesthTypeField.getValue());
            intervention.setOperatingRoom(opRoomField.getValue());
            intervention.setPlannedDate(plannedField.getValue());
            intervention.setStartDate(startField.getValue());
            intervention.setEndDate(endField.getValue());
            intervention.setComplications(complicationsField.getValue());
            intervention.setPreOpNotes(preOpField.getValue());
            intervention.setOperativeReport(opReportField.getValue());
            intervention.setPostOpNotes(postOpField.getValue());
            intervention.setStatus(Intervention.InterventionStatus.PLANNED);
            admissionService.saveIntervention(intervention);
            dialog.close();
            admission = admissionService.findById(admission.getId()).orElse(admission);
            buildUI();
            Notification.show("Intervention enregistrée", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private VerticalLayout buildBiologyTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        // Biology prescriptions from this admission
        var bioPrescriptions = prescriptionService.findByAdmission(admission.getId())
                .stream()
                .filter(p -> p.getType() == Prescription.PrescriptionType.BIOLOGY)
                .toList();

        Button addBtn = new Button("Demande de Biologie", VaadinIcon.FLASK.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openBiologyRequestDialog());

        if (bioPrescriptions.isEmpty()) {
            layout.add(addBtn, new Paragraph("Aucune demande de biologie pour cette admission."));
        } else {
            Grid<Prescription> grid = new Grid<>(Prescription.class, false);
            grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
            grid.addColumn(Prescription::getActName).setHeader("Analyse").setFlexGrow(1);
            grid.addColumn(Prescription::getLaboratorySection).setHeader("Section").setWidth("150px").setFlexGrow(0);
            grid.addColumn(p -> p.getStartDateTime() != null
                    ? p.getStartDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-")
                    .setHeader("Date Demande").setWidth("150px").setFlexGrow(0);
            grid.addColumn(new ComponentRenderer<>(p -> {
                Span badge = new Span(p.getStatus().name());
                badge.getElement().getThemeList().add("badge");
                if (p.getStatus() == Prescription.PrescriptionStatus.ACTIVE) badge.getElement().getThemeList().add("contrast");
                else if (p.getStatus() == Prescription.PrescriptionStatus.ELAPSED) badge.getElement().getThemeList().add("success");
                return badge;
            })).setHeader("Statut").setWidth("100px").setFlexGrow(0);
            grid.addColumn(Prescription::getNotes).setHeader("Notes").setFlexGrow(1);
            grid.setItems(bioPrescriptions);
            layout.add(addBtn, grid);
        }
        return layout;
    }

    private void openBiologyRequestDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Demande d'Analyse Biologique");
        dialog.setWidth("500px");

        FormLayout form = new FormLayout();
        TextField actNameField = new TextField("Analyse / Examen");
        actNameField.setRequired(true);
        TextField sectionField = new TextField("Section de Laboratoire");
        DateTimePicker dateField = new DateTimePicker("Date de Demande");
        dateField.setValue(LocalDateTime.now());
        TextArea notesField = new TextArea("Notes / Urgence");
        notesField.setWidthFull();

        form.add(actNameField, sectionField, dateField, notesField);
        form.setColspan(notesField, 2);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (actNameField.isEmpty()) {
                Notification.show("L'analyse est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Prescription p = new Prescription();
            p.setAdmission(admission);
            p.setType(Prescription.PrescriptionType.BIOLOGY);
            p.setActName(actNameField.getValue());
            p.setLaboratorySection(sectionField.getValue());
            p.setStartDateTime(dateField.getValue());
            p.setNotes(notesField.getValue());
            p.setStatus(Prescription.PrescriptionStatus.ACTIVE);
            prescriptionService.save(p);
            dialog.close();
            admission = admissionService.findById(admission.getId()).orElse(admission);
            buildUI();
            Notification.show("Demande enregistrée", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void confirmClose() {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Clôturer l'Admission");
        confirm.setText("Êtes-vous sûr de vouloir clôturer cette admission ? La date de sortie sera définie à maintenant.");
        confirm.setCancelable(true);
        confirm.setCancelText("Annuler");
        confirm.setConfirmText("Clôturer");
        confirm.setConfirmButtonTheme("primary");
        confirm.addConfirmListener(e -> {
            admissionService.closeAdmission(admission.getId(), LocalDateTime.now());
            Notification.show("Admission clôturée", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate(AdmissionListView.class));
        });
        confirm.open();
    }

    private void confirmCancel() {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Annuler l'Admission");
        confirm.setText("Êtes-vous sûr de vouloir annuler cette admission ?");
        confirm.setCancelable(true);
        confirm.setCancelText("Non");
        confirm.setConfirmText("Annuler l'Admission");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            admissionService.cancelAdmission(admission.getId());
            Notification.show("Admission annulée", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate(AdmissionListView.class));
        });
        confirm.open();
    }

    private HorizontalLayout infoChip(String label, String value) {
        Span chip = new Span(label + ": " + value);
        chip.getStyle().set("background", "var(--lumo-contrast-5pct)");
        chip.getStyle().set("padding", "4px 10px");
        chip.getStyle().set("border-radius", "16px");
        chip.getStyle().set("font-size", "var(--lumo-font-size-s)");
        return new HorizontalLayout(chip);
    }

    private String nvl(String s) { return s != null ? s : "-"; }
}
