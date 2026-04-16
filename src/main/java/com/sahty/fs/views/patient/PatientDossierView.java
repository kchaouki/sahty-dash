package com.sahty.fs.views.patient;

import com.sahty.fs.entity.*;
import com.sahty.fs.service.AdmissionService;
import com.sahty.fs.service.PatientService;
import com.sahty.fs.service.PrescriptionService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route(value = "patients/:patientId", layout = MainLayout.class)
@PageTitle("Dossier Patient | Sahty EMR")
@PermitAll
public class PatientDossierView extends VerticalLayout implements HasUrlParameter<String> {

    private final PatientService patientService;
    private final AdmissionService admissionService;
    private final PrescriptionService prescriptionService;
    private Patient patient;

    private H2 patientNameLabel;
    private Span ippLabel;
    private Span statusBadge;

    public PatientDossierView(PatientService patientService, AdmissionService admissionService,
                               PrescriptionService prescriptionService) {
        this.patientService = patientService;
        this.admissionService = admissionService;
        this.prescriptionService = prescriptionService;
        setSizeFull();
        setPadding(true);
    }

    @Override
    public void setParameter(BeforeEvent event, String patientId) {
        Optional<Patient> found = patientService.findById(patientId);
        if (found.isEmpty()) {
            Notification.show("Patient non trouvé", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.forwardTo(PatientListView.class);
            return;
        }
        this.patient = found.get();
        buildUI();
    }

    private void buildUI() {
        removeAll();

        RouterLink backLink = new RouterLink("← Patients", PatientListView.class);

        VerticalLayout headerCard = buildPatientHeaderCard();

        TabSheet tabSheet = buildDossierTabs();
        tabSheet.setSizeFull();

        add(backLink, headerCard, tabSheet);
    }

    private VerticalLayout buildPatientHeaderCard() {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.getStyle().set("background", "var(--lumo-base-color)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        card.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");

        patientNameLabel = new H2(patient.getFullName());
        ippLabel = new Span("IPP: " + nvl(patient.getIpp()));
        ippLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");

        statusBadge = new Span(patient.getIdentityStatus().name());
        statusBadge.getElement().getThemeList().add("badge");
        if (patient.getIdentityStatus() == Patient.IdentityStatus.VERIFIED) {
            statusBadge.getElement().getThemeList().add("success");
        }

        HorizontalLayout nameRow = new HorizontalLayout(patientNameLabel, ippLabel, statusBadge);
        nameRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        nameRow.setSpacing(true);

        HorizontalLayout infoRow = new HorizontalLayout();
        infoRow.setSpacing(true);
        infoRow.add(
                infoItem("Date de Naissance", patient.getDateOfBirth() != null
                        ? patient.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"),
                infoItem("Sexe", patient.getGender() != null
                        ? (patient.getGender() == Patient.Gender.M ? "Masculin" : "Féminin") : "-"),
                infoItem("Téléphone", nvl(patient.getPhone())),
                infoItem("Groupe Sanguin", nvl(patient.getBloodGroup())),
                infoItem("Nationalité", nvl(patient.getNationality()))
        );

        card.add(nameRow, infoRow);
        return card;
    }

    private TabSheet buildDossierTabs() {
        TabSheet tabs = new TabSheet();

        tabs.add(new Tab("Antécédents"), buildAntecedentsTab());
        tabs.add(new Tab("Allergies"), buildAllergiesTab());
        tabs.add(new Tab("Examen Clinique"), buildClinicalExamsTab());
        tabs.add(new Tab("Observations"), buildObservationsTab());
        tabs.add(new Tab("Prescriptions"), buildPrescriptionsTab());
        tabs.add(new Tab("Transfusions"), buildTransfusionsTab());
        tabs.add(new Tab("Interventions"), buildInterventionsTab());
        tabs.add(new Tab("Admissions"), buildAdmissionsTab());

        return tabs;
    }

    private VerticalLayout buildAntecedentsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        Button addBtn = new Button("Ajouter un Antécédent", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openAntecedentDialog(null, layout));

        Grid<Antecedent> grid = new Grid<>(Antecedent.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(a -> a.getType().name()).setHeader("Type").setWidth("130px").setFlexGrow(0);
        grid.addColumn(Antecedent::getDescription).setHeader("Description").setFlexGrow(1);
        grid.addColumn(Antecedent::getIcdCode).setHeader("Code CIM-10").setWidth("130px").setFlexGrow(0);
        grid.addColumn(Antecedent::getYearOnset).setHeader("Année").setWidth("80px").setFlexGrow(0);
        grid.setItems(patient.getAntecedents());

        layout.add(addBtn, grid);
        return layout;
    }

    private void openAntecedentDialog(Antecedent existing, VerticalLayout parentLayout) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouvel Antécédent" : "Modifier l'Antécédent");
        dialog.setWidth("500px");

        FormLayout form = new FormLayout();
        ComboBox<Antecedent.AntecedentType> typeField = new ComboBox<>("Type");
        typeField.setItems(Antecedent.AntecedentType.values());
        typeField.setItemLabelGenerator(Antecedent.AntecedentType::name);
        typeField.setRequired(true);

        TextArea descField = new TextArea("Description");
        descField.setRequired(true);
        descField.setWidthFull();

        TextField icdField = new TextField("Code CIM-10");
        NumberField yearField = new NumberField("Année d'apparition");
        yearField.setMin(1900);
        yearField.setMax(2100);

        if (existing != null) {
            typeField.setValue(existing.getType());
            descField.setValue(nvl(existing.getDescription()));
            icdField.setValue(nvl(existing.getIcdCode()));
            if (existing.getYearOnset() != null) yearField.setValue(existing.getYearOnset().doubleValue());
        }

        form.add(typeField, icdField, yearField, descField);
        form.setColspan(descField, 2);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (typeField.isEmpty() || descField.isEmpty()) {
                Notification.show("Type et description sont obligatoires", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Antecedent antecedent = existing != null ? existing : new Antecedent();
            antecedent.setType(typeField.getValue());
            antecedent.setDescription(descField.getValue());
            antecedent.setIcdCode(icdField.getValue());
            antecedent.setYearOnset(yearField.getValue() != null ? yearField.getValue().intValue() : null);
            antecedent.setPatient(patient);
            patientService.saveAntecedent(antecedent);
            patient = patientService.findById(patient.getId()).orElse(patient);
            dialog.close();
            // Refresh
            parentLayout.removeAll();
            parentLayout.add(buildAntecedentsTab().getChildren().toArray(com.vaadin.flow.component.Component[]::new));
            Notification.show("Antécédent enregistré", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private VerticalLayout buildAllergiesTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        Button addBtn = new Button("Ajouter une Allergie", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openAllergyDialog(null, layout));

        Grid<Allergy> grid = new Grid<>(Allergy.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(Allergy::getAllergen).setHeader("Allergène").setFlexGrow(1);
        grid.addColumn(a -> a.getAllergenType() != null ? a.getAllergenType().name() : "").setHeader("Type").setWidth("120px").setFlexGrow(0);
        grid.addColumn(a -> a.getSeverity() != null ? a.getSeverity().name() : "").setHeader("Sévérité").setWidth("120px").setFlexGrow(0);
        grid.addColumn(Allergy::getReactions).setHeader("Réactions").setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(a -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(ev -> openAllergyDialog(a, layout));
            return editBtn;
        })).setHeader("").setWidth("60px").setFlexGrow(0);
        grid.setItems(patient.getAllergies());

        layout.add(addBtn, grid);
        return layout;
    }

    private void openAllergyDialog(Allergy existing, VerticalLayout parentLayout) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouvelle Allergie" : "Modifier l'Allergie");
        dialog.setWidth("500px");

        FormLayout form = new FormLayout();
        TextField allergenField = new TextField("Allergène");
        allergenField.setRequired(true);

        ComboBox<Allergy.AllergenType> typeField = new ComboBox<>("Type d'Allergène");
        typeField.setItems(Allergy.AllergenType.values());
        typeField.setItemLabelGenerator(Allergy.AllergenType::name);

        ComboBox<Allergy.Severity> severityField = new ComboBox<>("Sévérité");
        severityField.setItems(Allergy.Severity.values());
        severityField.setItemLabelGenerator(Allergy.Severity::name);

        TextArea reactionsField = new TextArea("Réactions");
        reactionsField.setWidthFull();

        if (existing != null) {
            allergenField.setValue(nvl(existing.getAllergen()));
            typeField.setValue(existing.getAllergenType());
            severityField.setValue(existing.getSeverity());
            reactionsField.setValue(nvl(existing.getReactions()));
        }

        form.add(allergenField, typeField, severityField, reactionsField);
        form.setColspan(reactionsField, 2);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (allergenField.isEmpty()) {
                Notification.show("L'allergène est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Allergy allergy = existing != null ? existing : new Allergy();
            allergy.setAllergen(allergenField.getValue());
            allergy.setAllergenType(typeField.getValue());
            allergy.setSeverity(severityField.getValue());
            allergy.setReactions(reactionsField.getValue());
            allergy.setPatient(patient);
            patientService.saveAllergy(allergy);
            patient = patientService.findById(patient.getId()).orElse(patient);
            dialog.close();
            Notification.show("Allergie enregistrée", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private VerticalLayout buildClinicalExamsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        // Show exams across all admissions
        List<Admission> admissions = admissionService.findByPatient(patient.getId());
        boolean hasExams = admissions.stream().anyMatch(a -> !a.getClinicalExams().isEmpty());

        if (!hasExams) {
            layout.add(new Paragraph("Aucun examen clinique enregistré pour ce patient."));
        } else {
            admissions.forEach(admission -> {
                if (!admission.getClinicalExams().isEmpty()) {
                    H4 admTitle = new H4("Admission NDA: " + nvl(admission.getNda()) + " – "
                            + (admission.getAdmissionDate() != null
                                ? admission.getAdmissionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""));
                    layout.add(admTitle);

                    Grid<ClinicalExam> grid = new Grid<>(ClinicalExam.class, false);
                    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
                    grid.addColumn(e -> e.getCreatedAt() != null
                            ? e.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                            .setHeader("Date").setWidth("160px").setFlexGrow(0);
                    grid.addColumn(e -> e.getTemperature() != null ? e.getTemperature() + " °C" : "-")
                            .setHeader("Temp.").setWidth("80px").setFlexGrow(0);
                    grid.addColumn(e -> e.getHeartRate() != null ? e.getHeartRate() + " bpm" : "-")
                            .setHeader("FC").setWidth("80px").setFlexGrow(0);
                    grid.addColumn(e -> (e.getSystolicBP() != null && e.getDiastolicBP() != null)
                            ? e.getSystolicBP() + "/" + e.getDiastolicBP() + " mmHg" : "-")
                            .setHeader("PA").setWidth("110px").setFlexGrow(0);
                    grid.addColumn(e -> e.getOxygenSaturation() != null ? e.getOxygenSaturation() + " %" : "-")
                            .setHeader("SpO2").setWidth("80px").setFlexGrow(0);
                    grid.addColumn(e -> e.getWeight() != null ? e.getWeight() + " kg" : "-")
                            .setHeader("Poids").setWidth("80px").setFlexGrow(0);
                    grid.addColumn(ClinicalExam::getGeneralState).setHeader("État Général").setFlexGrow(1);
                    grid.setItems(admission.getClinicalExams());
                    grid.setHeight("200px");
                    layout.add(grid);
                }
            });
        }

        return layout;
    }

    private VerticalLayout buildObservationsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        List<Admission> admissions = admissionService.findByPatient(patient.getId());
        boolean hasObs = admissions.stream().anyMatch(a -> !a.getObservations().isEmpty());

        if (!hasObs) {
            layout.add(new Paragraph("Aucune observation enregistrée pour ce patient."));
        } else {
            admissions.forEach(admission -> {
                if (!admission.getObservations().isEmpty()) {
                    H4 admTitle = new H4("Admission NDA: " + nvl(admission.getNda()));
                    layout.add(admTitle);

                    Grid<Observation> grid = new Grid<>(Observation.class, false);
                    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
                    grid.addColumn(o -> o.getType() != null ? o.getType().name() : "").setHeader("Type").setWidth("140px").setFlexGrow(0);
                    grid.addColumn(o -> o.getAuthor() != null ? o.getAuthor().getFullName() : "").setHeader("Auteur").setWidth("180px").setFlexGrow(0);
                    grid.addColumn(o -> o.getCreatedAt() != null
                            ? o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                            .setHeader("Date").setWidth("160px").setFlexGrow(0);
                    grid.addColumn(Observation::getContent).setHeader("Contenu").setFlexGrow(1);
                    grid.setItems(admission.getObservations());
                    grid.setHeight("200px");
                    layout.add(grid);
                }
            });
        }

        return layout;
    }

    private VerticalLayout buildPrescriptionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        // All prescriptions across all admissions
        List<Admission> admissions = admissionService.findByPatient(patient.getId());

        if (admissions.isEmpty()) {
            layout.add(new Paragraph("Aucune admission pour ce patient."));
            return layout;
        }

        admissions.forEach(admission -> {
            List<Prescription> prescriptions = prescriptionService.findByAdmission(admission.getId());
            if (!prescriptions.isEmpty()) {
                H4 admTitle = new H4("Admission NDA: " + nvl(admission.getNda()) + " — "
                        + admission.getStatus().name());
                layout.add(admTitle);

                Grid<Prescription> grid = new Grid<>(Prescription.class, false);
                grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
                grid.addColumn(p -> p.getType().name()).setHeader("Type").setWidth("120px").setFlexGrow(0);
                grid.addColumn(Prescription::getMolecule).setHeader("Médicament / Acte").setFlexGrow(1);
                grid.addColumn(p -> p.getQty() != null ? p.getQty() + " " + nvl(p.getUnit()) : "-")
                        .setHeader("Dose").setWidth("100px").setFlexGrow(0);
                grid.addColumn(Prescription::getRoute).setHeader("Voie").setWidth("100px").setFlexGrow(0);
                grid.addColumn(new ComponentRenderer<>(p -> {
                    Span badge = new Span(p.getStatus().name());
                    badge.getElement().getThemeList().add("badge");
                    if (p.getStatus() == Prescription.PrescriptionStatus.ACTIVE) badge.getElement().getThemeList().add("success");
                    else if (p.getStatus() == Prescription.PrescriptionStatus.STOPPED) badge.getElement().getThemeList().add("error");
                    return badge;
                })).setHeader("Statut").setWidth("100px").setFlexGrow(0);
                grid.addColumn(p -> p.getStartDateTime() != null
                        ? p.getStartDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-")
                        .setHeader("Début").setWidth("110px").setFlexGrow(0);
                grid.setItems(prescriptions);
                grid.setHeight("200px");
                layout.add(grid);
            }
        });

        if (layout.getComponentCount() == 0) {
            layout.add(new Paragraph("Aucune prescription enregistrée pour ce patient."));
        }

        return layout;
    }

    private VerticalLayout buildTransfusionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        List<Admission> admissions = admissionService.findByPatient(patient.getId());
        boolean hasTransf = admissions.stream().anyMatch(a -> !a.getTransfusions().isEmpty());

        if (!hasTransf) {
            layout.add(new Paragraph("Aucune transfusion enregistrée pour ce patient."));
        } else {
            admissions.forEach(admission -> {
                if (!admission.getTransfusions().isEmpty()) {
                    H4 admTitle = new H4("Admission NDA: " + nvl(admission.getNda()));
                    layout.add(admTitle);

                    Grid<Transfusion> grid = new Grid<>(Transfusion.class, false);
                    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
                    grid.addColumn(Transfusion::getBloodProduct).setHeader("Produit Sanguin").setFlexGrow(1);
                    grid.addColumn(Transfusion::getBloodGroup).setHeader("Groupe").setWidth("80px").setFlexGrow(0);
                    grid.addColumn(Transfusion::getRhFactor).setHeader("Rh").setWidth("60px").setFlexGrow(0);
                    grid.addColumn(t -> t.getVolumeMl() != null ? t.getVolumeMl() + " mL" : "-").setHeader("Volume").setWidth("90px").setFlexGrow(0);
                    grid.addColumn(t -> t.getStartTime() != null
                            ? t.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-")
                            .setHeader("Début").setWidth("150px").setFlexGrow(0);
                    grid.addColumn(new ComponentRenderer<>(t -> {
                        Span badge = new Span(t.getStatus().name());
                        badge.getElement().getThemeList().add("badge");
                        if (t.getStatus() == Transfusion.TransfusionStatus.COMPLETED) badge.getElement().getThemeList().add("success");
                        else if (t.getStatus() == Transfusion.TransfusionStatus.ADVERSE_REACTION) badge.getElement().getThemeList().add("error");
                        return badge;
                    })).setHeader("Statut").setWidth("130px").setFlexGrow(0);
                    grid.setItems(admission.getTransfusions());
                    grid.setHeight("200px");
                    layout.add(grid);
                }
            });
        }

        return layout;
    }

    private VerticalLayout buildInterventionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        List<Admission> admissions = admissionService.findByPatient(patient.getId());
        boolean hasInterv = admissions.stream().anyMatch(a -> !a.getInterventions().isEmpty());

        if (!hasInterv) {
            layout.add(new Paragraph("Aucune intervention enregistrée pour ce patient."));
        } else {
            admissions.forEach(admission -> {
                if (!admission.getInterventions().isEmpty()) {
                    H4 admTitle = new H4("Admission NDA: " + nvl(admission.getNda()));
                    layout.add(admTitle);

                    Grid<Intervention> grid = new Grid<>(Intervention.class, false);
                    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
                    grid.addColumn(Intervention::getName).setHeader("Intervention").setFlexGrow(1);
                    grid.addColumn(Intervention::getType).setHeader("Type").setWidth("120px").setFlexGrow(0);
                    grid.addColumn(Intervention::getSurgeonName).setHeader("Chirurgien").setWidth("160px").setFlexGrow(0);
                    grid.addColumn(Intervention::getAnesthesiaType).setHeader("Anesthésie").setWidth("120px").setFlexGrow(0);
                    grid.addColumn(i -> i.getPlannedDate() != null
                            ? i.getPlannedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-")
                            .setHeader("Date Prévue").setWidth("120px").setFlexGrow(0);
                    grid.addColumn(new ComponentRenderer<>(i -> {
                        Span badge = new Span(i.getStatus().name());
                        badge.getElement().getThemeList().add("badge");
                        if (i.getStatus() == Intervention.InterventionStatus.COMPLETED) badge.getElement().getThemeList().add("success");
                        else if (i.getStatus() == Intervention.InterventionStatus.CANCELLED) badge.getElement().getThemeList().add("error");
                        return badge;
                    })).setHeader("Statut").setWidth("120px").setFlexGrow(0);
                    grid.setItems(admission.getInterventions());
                    grid.setHeight("200px");
                    layout.add(grid);
                }
            });
        }

        return layout;
    }

    private VerticalLayout buildAdmissionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        Grid<Admission> grid = new Grid<>(Admission.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(Admission::getNda).setHeader("NDA").setWidth("150px").setFlexGrow(0);
        grid.addColumn(a -> a.getService() != null ? a.getService().getName() : "").setHeader("Service");
        grid.addColumn(a -> a.getAdmissionDate() != null
                ? a.getAdmissionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                .setHeader("Date Admission");
        grid.addColumn(new ComponentRenderer<>(a -> {
            Span badge = new Span(a.getStatus().name());
            badge.getElement().getThemeList().add("badge");
            if (a.getStatus() == Admission.AdmissionStatus.EN_COURS) badge.getElement().getThemeList().add("success");
            else if (a.getStatus() == Admission.AdmissionStatus.ANNULE) badge.getElement().getThemeList().add("error");
            return badge;
        })).setHeader("Statut");
        grid.addColumn(a -> a.getDischargeDate() != null
                ? a.getDischargeDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                .setHeader("Date Sortie");

        grid.addItemClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate(AdmissionDossierView.class, e.getItem().getId())));

        grid.setItems(admissionService.findByPatient(patient.getId()));
        layout.add(grid);
        return layout;
    }

    private HorizontalLayout infoItem(String label, String value) {
        VerticalLayout item = new VerticalLayout();
        item.setSpacing(false);
        item.setPadding(false);
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");
        labelSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-weight", "500");
        item.add(labelSpan, valueSpan);
        return new HorizontalLayout(item);
    }

    private String nvl(String s) { return s != null ? s : "-"; }
}
