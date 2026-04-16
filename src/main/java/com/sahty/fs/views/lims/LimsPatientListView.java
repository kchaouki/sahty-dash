package com.sahty.fs.views.lims;

import com.sahty.fs.entity.Patient;
import com.sahty.fs.entity.lims.LabSample;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.PatientService;
import com.sahty.fs.service.lims.LimsService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "lims/patients", layout = MainLayout.class)
@PageTitle("Patients LIMS | Sahty EMR")
@PermitAll
public class LimsPatientListView extends VerticalLayout {

    private final PatientService patientService;
    private final LimsService limsService;
    private final String tenantId;
    private Grid<Patient> patientGrid;
    private String currentSearch = "";

    public LimsPatientListView(PatientService patientService, LimsService limsService) {
        this.patientService = patientService;
        this.limsService = limsService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Patients — Module Laboratoire");

        TextField searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom, IPP, téléphone...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("350px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> {
            currentSearch = e.getValue();
            refreshGrid();
        });

        HorizontalLayout toolbar = new HorizontalLayout(searchField);
        toolbar.setWidthFull();

        patientGrid = buildPatientGrid();

        add(title, toolbar, patientGrid);
    }

    private Grid<Patient> buildPatientGrid() {
        Grid<Patient> g = new Grid<>(Patient.class, false);
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        g.setSizeFull();

        g.addColumn(p -> p.getLastName() + " " + p.getFirstName()).setHeader("Patient").setFlexGrow(1);
        g.addColumn(Patient::getIpp).setHeader("IPP").setWidth("120px").setFlexGrow(0);
        g.addColumn(p -> p.getDateOfBirth() != null
                ? p.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-")
                .setHeader("Date de Naissance").setWidth("140px").setFlexGrow(0);
        g.addColumn(p -> p.getGender() != null ? p.getGender().name() : "-")
                .setHeader("Sexe").setWidth("70px").setFlexGrow(0);
        g.addColumn(Patient::getPhone).setHeader("Téléphone").setWidth("130px").setFlexGrow(0);

        g.addColumn(new ComponentRenderer<>(p -> {
            Button viewBtn = new Button("Voir Analyses", VaadinIcon.FLASK.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            viewBtn.addClickListener(e -> g.setDetailsVisible(p, !g.isDetailsVisible(p)));
            return viewBtn;
        })).setHeader("Actions").setWidth("150px").setFlexGrow(0);

        g.setItemDetailsRenderer(new ComponentRenderer<>(patient -> buildSampleDetails(patient)));

        CallbackDataProvider<Patient, Void> provider = DataProvider.fromCallbacks(
                q -> patientService.searchPatients(tenantId, currentSearch, q.getOffset() / Math.max(q.getLimit(), 1), q.getLimit()).stream(),
                q -> (int) patientService.searchPatients(tenantId, currentSearch, 0, Integer.MAX_VALUE).getTotalElements()
        );
        g.setDataProvider(provider);

        return g;
    }

    private VerticalLayout buildSampleDetails(Patient patient) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(false);

        List<LabSample> samples = limsService.findSamplesByPatient(patient.getId());

        if (samples.isEmpty()) {
            layout.add(new H4("Aucune analyse pour ce patient."));
            return layout;
        }

        layout.add(new H4("Analyses du patient (" + samples.size() + ")"));

        Grid<LabSample> sampleGrid = new Grid<>(LabSample.class, false);
        sampleGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        sampleGrid.addColumn(LabSample::getSampleNumber).setHeader("N° Échantillon").setWidth("160px").setFlexGrow(0);
        sampleGrid.addColumn(s -> s.getSpecimen() != null ? s.getSpecimen().getName() : "-")
                .setHeader("Type").setWidth("150px").setFlexGrow(0);
        sampleGrid.addColumn(s -> s.getCreatedAt() != null
                ? s.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-")
                .setHeader("Date").setWidth("150px").setFlexGrow(0);
        sampleGrid.addColumn(new ComponentRenderer<>(s -> {
            Span badge = new Span(s.getStatus() != null ? s.getStatus().name() : "");
            badge.getElement().getThemeList().add("badge");
            switch (s.getStatus()) {
                case VALIDATED -> badge.getElement().getThemeList().add("success");
                case REJECTED -> badge.getElement().getThemeList().add("error");
                case RESULTED, IN_PROGRESS -> badge.getElement().getThemeList().add("contrast");
                default -> {}
            }
            return badge;
        })).setHeader("Statut").setWidth("130px").setFlexGrow(0);
        sampleGrid.addColumn(s -> s.getResults() != null ? s.getResults().size() + " résultat(s)" : "0")
                .setHeader("Résultats").setWidth("120px").setFlexGrow(0);
        sampleGrid.setItems(samples);
        sampleGrid.setHeight("250px");

        layout.add(sampleGrid);
        return layout;
    }

    private void refreshGrid() {
        patientGrid.getDataProvider().refreshAll();
    }
}
