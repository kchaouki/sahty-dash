package com.sahty.fs.views.patient;

import com.sahty.fs.entity.Patient;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.PatientService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Route(value = "patients", layout = MainLayout.class)
@PageTitle("Patients | Sahty EMR")
@PermitAll
public class PatientListView extends VerticalLayout {

    private final PatientService patientService;
    private final Grid<Patient> grid;
    private final TextField searchField;
    private String tenantId;

    public PatientListView(PatientService patientService) {
        this.patientService = patientService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        H2 title = new H2("Liste des Patients");

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom, IPP, téléphone...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("400px");

        Button addBtn = new Button("Nouveau Patient", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openPatientDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, addBtn);
        toolbar.setWidthFull();
        toolbar.expand(searchField);

        grid = createGrid();
        grid.setSizeFull();
        searchField.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        add(title, toolbar, grid);
    }

    private Grid<Patient> createGrid() {
        Grid<Patient> grid = new Grid<>(Patient.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);

        grid.addColumn(Patient::getIpp).setHeader("IPP").setWidth("120px").setFlexGrow(0);
        grid.addColumn(Patient::getLastName).setHeader("Nom").setSortable(true);
        grid.addColumn(Patient::getFirstName).setHeader("Prénom").setSortable(true);
        grid.addColumn(p -> p.getDateOfBirth() != null
                ? p.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")
                .setHeader("Date de Naissance");
        grid.addColumn(p -> p.getGender() != null ? (p.getGender() == Patient.Gender.M ? "Masculin" : "Féminin") : "")
                .setHeader("Sexe");
        grid.addColumn(Patient::getPhone).setHeader("Téléphone");
        grid.addColumn(new ComponentRenderer<>(patient -> {
            Span badge = new Span(patient.getIdentityStatus().name());
            badge.getElement().getThemeList().add("badge");
            if (patient.getIdentityStatus() == Patient.IdentityStatus.VERIFIED) {
                badge.getElement().getThemeList().add("success");
            } else if (patient.getIdentityStatus() == Patient.IdentityStatus.PROVISIONAL) {
                badge.getElement().getThemeList().add("warning");
            }
            return badge;
        })).setHeader("Statut Identité");

        // Actions column
        grid.addColumn(new ComponentRenderer<>(patient -> {
            Button viewBtn = new Button(VaadinIcon.EYE.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewBtn.addClickListener(e -> getUI().ifPresent(ui ->
                    ui.navigate(PatientDossierView.class, patient.getId())));

            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openPatientDialog(patient));

            HorizontalLayout actions = new HorizontalLayout(viewBtn, editBtn);
            actions.setSpacing(false);
            return actions;
        })).setHeader("Actions").setWidth("100px").setFlexGrow(0);

        // Lazy loading data provider
        CallbackDataProvider<Patient, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    String filter = searchField.getValue();
                    return patientService.searchPatients(tenantId, filter,
                            query.getOffset() / query.getLimit(), query.getLimit()).stream();
                },
                query -> {
                    String filter = searchField.getValue();
                    return (int) patientService.searchPatients(tenantId, filter, 0, Integer.MAX_VALUE).getTotalElements();
                }
        );
        grid.setDataProvider(dataProvider);

        grid.addItemDoubleClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate(PatientDossierView.class, e.getItem().getId())));

        return grid;
    }

    private void openPatientDialog(Patient patient) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeaderTitle(patient == null ? "Nouveau Patient" : "Modifier Patient");

        PatientFormView form = new PatientFormView(patient, (savedPatient) -> {
            try {
                if (savedPatient.getId() == null) {
                    patientService.createPatient(savedPatient, tenantId);
                } else {
                    patientService.updatePatient(savedPatient);
                }
                grid.getDataProvider().refreshAll();
                dialog.close();
                Notification.show("Patient enregistré avec succès", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        dialog.getFooter().add(cancelBtn, form.getSaveButton());
        dialog.add(form);
        dialog.open();
    }
}
