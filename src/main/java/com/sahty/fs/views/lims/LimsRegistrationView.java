package com.sahty.fs.views.lims;

import com.sahty.fs.entity.lims.LabSample;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.lims.LimsService;
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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;

@Route(value = "lims/registration", layout = MainLayout.class)
@PageTitle("Laboratoire - Enregistrement | Sahty EMR")
@PermitAll
public class LimsRegistrationView extends VerticalLayout {

    private final LimsService limsService;
    private final String tenantId;

    public LimsRegistrationView(LimsService limsService) {
        this.limsService = limsService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Laboratoire - Gestion des Échantillons");

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add(new Tab("Enregistrement"), buildRegistrationTab());
        tabs.add(new Tab("Prélèvement"), buildCollectionTab());
        tabs.add(new Tab("Réception"), buildReceptionTab());
        tabs.add(new Tab("Analytés"), buildAnalytesTab());

        add(title, tabs);
    }

    private VerticalLayout buildRegistrationTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        Button registerBtn = new Button("Enregistrer un Échantillon", VaadinIcon.PLUS.create());
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Grid<LabSample> grid = buildSampleGrid();
        grid.setItems(limsService.findPendingCollections(tenantId));
        grid.setSizeFull();

        layout.add(registerBtn, grid);
        return layout;
    }

    private VerticalLayout buildCollectionTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        Grid<LabSample> grid = buildSampleGrid();
        grid.setItems(limsService.findPendingCollections(tenantId));

        // Add collect action column
        grid.addColumn(new ComponentRenderer<>(sample -> {
            Button btn = new Button("Prélever", VaadinIcon.FLASK.create());
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            btn.setEnabled(sample.getStatus() == LabSample.SampleStatus.REGISTERED);
            btn.addClickListener(e -> {
                limsService.collectSample(sample.getId());
                grid.getDataProvider().refreshAll();
            });
            return btn;
        })).setHeader("Action");

        grid.setSizeFull();
        layout.add(grid);
        return layout;
    }

    private VerticalLayout buildReceptionTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        Grid<LabSample> grid = buildSampleGrid();
        grid.setItems(limsService.findPendingReceptions(tenantId));

        grid.addColumn(new ComponentRenderer<>(sample -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button acceptBtn = new Button("Accepter", VaadinIcon.CHECK.create());
            acceptBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            acceptBtn.addClickListener(e -> {
                limsService.receiveSample(sample.getId());
                grid.getDataProvider().refreshAll();
            });

            Button rejectBtn = new Button("Rejeter", VaadinIcon.CLOSE.create());
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            rejectBtn.addClickListener(e -> {
                limsService.rejectSample(sample.getId(), "Rejeté à la réception");
                grid.getDataProvider().refreshAll();
            });

            actions.add(acceptBtn, rejectBtn);
            return actions;
        })).setHeader("Actions");

        grid.setSizeFull();
        layout.add(grid);
        return layout;
    }

    private VerticalLayout buildAnalytesTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.add(new com.vaadin.flow.component.html.Paragraph("Configuration des analytés - à implémenter"));
        return layout;
    }

    private Grid<LabSample> buildSampleGrid() {
        Grid<LabSample> grid = new Grid<>(LabSample.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        grid.addColumn(LabSample::getSampleNumber).setHeader("N° Échantillon").setWidth("160px").setFlexGrow(0);
        grid.addColumn(s -> s.getPatient() != null ? s.getPatient().getFullName() : "")
                .setHeader("Patient").setFlexGrow(1);
        grid.addColumn(s -> s.getSpecimen() != null ? s.getSpecimen().getName() : "")
                .setHeader("Type d'Échantillon");
        grid.addColumn(s -> s.getCreatedAt() != null
                ? s.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                .setHeader("Date Enregistrement");
        grid.addColumn(new ComponentRenderer<>(sample -> {
            Span badge = new Span(sample.getStatus().name());
            badge.getElement().getThemeList().add("badge");
            switch (sample.getStatus()) {
                case REGISTERED -> badge.getElement().getThemeList().add("contrast");
                case COLLECTED -> badge.getElement().getThemeList().add("warning");
                case RECEIVED -> badge.getElement().getThemeList().add("primary");
                case RESULTED, VALIDATED -> badge.getElement().getThemeList().add("success");
                case REJECTED -> badge.getElement().getThemeList().add("error");
                default -> {}
            }
            return badge;
        })).setHeader("Statut");

        grid.addColumn(s -> s.isStat() ? "URGENCE" : "Normal").setHeader("Priorité");

        return grid;
    }
}
