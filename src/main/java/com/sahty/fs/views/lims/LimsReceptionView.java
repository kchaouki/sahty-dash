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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "lims/reception", layout = MainLayout.class)
@PageTitle("Réception | LIMS - Sahty EMR")
@PermitAll
public class LimsReceptionView extends VerticalLayout {

    private final LimsService limsService;
    private final String tenantId;
    private Grid<LabSample> grid;
    private List<LabSample> allSamples;

    public LimsReceptionView(LimsService limsService) {
        this.limsService = limsService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Réception des Échantillons");

        TextField searchField = new TextField();
        searchField.setPlaceholder("Rechercher par patient, NDA, numéro...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("350px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterGrid(e.getValue()));

        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> loadSamples());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, refreshBtn);

        grid = buildGrid();
        add(title, toolbar, grid);
        loadSamples();
    }

    private Grid<LabSample> buildGrid() {
        Grid<LabSample> g = new Grid<>(LabSample.class, false);
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        g.setSizeFull();

        g.addColumn(LabSample::getSampleNumber).setHeader("N° Échantillon").setWidth("160px").setFlexGrow(0);
        g.addColumn(s -> s.getPatient() != null ? s.getPatient().getFullName() : "-")
                .setHeader("Patient").setFlexGrow(1);
        g.addColumn(s -> s.getAdmission() != null ? s.getAdmission().getNda() : "-")
                .setHeader("NDA").setWidth("130px").setFlexGrow(0);
        g.addColumn(s -> s.getSpecimen() != null ? s.getSpecimen().getName() : "-")
                .setHeader("Spécimen").setWidth("140px").setFlexGrow(0);
        g.addColumn(s -> s.getContainer() != null ? s.getContainer().getName() : "-")
                .setHeader("Contenant").setWidth("130px").setFlexGrow(0);
        g.addColumn(s -> s.getCollectionDateTime() != null
                ? s.getCollectionDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-")
                .setHeader("Prélevé le").setWidth("150px").setFlexGrow(0);
        g.addColumn(s -> s.isStat() ? "URGENT" : "Normal").setHeader("Priorité").setWidth("90px").setFlexGrow(0);
        g.addColumn(new ComponentRenderer<>(s -> {
            Span badge = new Span(s.getStatus() != null ? s.getStatus().name() : "");
            badge.getElement().getThemeList().add("badge");
            if (s.getStatus() == LabSample.SampleStatus.RECEIVED) badge.getElement().getThemeList().add("success");
            else if (s.getStatus() == LabSample.SampleStatus.COLLECTED) badge.getElement().getThemeList().add("contrast");
            else if (s.getStatus() == LabSample.SampleStatus.REJECTED) badge.getElement().getThemeList().add("error");
            return badge;
        })).setHeader("Statut").setWidth("120px").setFlexGrow(0);

        g.addColumn(new ComponentRenderer<>(s -> {
            HorizontalLayout actions = new HorizontalLayout();
            if (s.getStatus() == LabSample.SampleStatus.COLLECTED) {
                Button receiveBtn = new Button("Réceptionner", VaadinIcon.CHECK.create());
                receiveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                receiveBtn.addClickListener(e -> {
                    limsService.receiveSample(s.getId());
                    loadSamples();
                    Notification.show("Échantillon réceptionné", 2000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                });

                Button rejectBtn = new Button(VaadinIcon.CLOSE.create());
                rejectBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                rejectBtn.getElement().setAttribute("title", "Rejeter");
                rejectBtn.addClickListener(e -> {
                    limsService.rejectSample(s.getId(), "Rejeté à la réception");
                    loadSamples();
                    Notification.show("Échantillon rejeté", 2000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                });
                actions.add(receiveBtn, rejectBtn);
            }
            return actions;
        })).setHeader("Actions").setWidth("200px").setFlexGrow(0);

        return g;
    }

    private void loadSamples() {
        allSamples = limsService.findPendingReceptions(tenantId);
        grid.setItems(allSamples);
    }

    private void filterGrid(String query) {
        if (allSamples == null) return;
        if (query == null || query.isBlank()) {
            grid.setItems(allSamples);
            return;
        }
        String q = query.toLowerCase();
        grid.setItems(allSamples.stream().filter(s ->
                (s.getSampleNumber() != null && s.getSampleNumber().toLowerCase().contains(q)) ||
                (s.getAdmission() != null && s.getAdmission().getNda() != null
                        && s.getAdmission().getNda().toLowerCase().contains(q)) ||
                (s.getPatient() != null && s.getPatient().getFullName().toLowerCase().contains(q))
        ).collect(Collectors.toList()));
    }
}
