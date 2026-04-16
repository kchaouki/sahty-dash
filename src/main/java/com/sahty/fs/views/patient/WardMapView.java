package com.sahty.fs.views.patient;

import com.sahty.fs.entity.Bed;
import com.sahty.fs.entity.HospitalService;
import com.sahty.fs.entity.Room;
import com.sahty.fs.repository.RoomRepository;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.HospitalServiceService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "ward-map", layout = MainLayout.class)
@PageTitle("Carte des Lits | Sahty EMR")
@PermitAll
public class WardMapView extends VerticalLayout {

    private final HospitalServiceService serviceService;
    private final RoomRepository roomRepository;
    private final String tenantId;

    private FlexLayout mapContainer;
    private ComboBox<HospitalService> serviceFilter;

    public WardMapView(HospitalServiceService serviceService, RoomRepository roomRepository) {
        this.serviceService = serviceService;
        this.roomRepository = roomRepository;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Carte des Lits");

        serviceFilter = new ComboBox<>("Filtrer par Service");
        serviceFilter.setItems(serviceService.findByTenant(tenantId));
        serviceFilter.setItemLabelGenerator(HospitalService::getName);
        serviceFilter.setClearButtonVisible(true);
        serviceFilter.addValueChangeListener(e -> refreshMap(e.getValue()));

        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> refreshMap(serviceFilter.getValue()));

        HorizontalLayout toolbar = new HorizontalLayout(serviceFilter, refreshBtn);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);

        // Legend
        HorizontalLayout legend = buildLegend();

        mapContainer = new FlexLayout();
        mapContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        mapContainer.getStyle().set("gap", "16px");
        mapContainer.setWidthFull();

        add(title, toolbar, legend, mapContainer);
        refreshMap(null);
    }

    private HorizontalLayout buildLegend() {
        HorizontalLayout legend = new HorizontalLayout();
        legend.setSpacing(true);
        legend.add(legendItem("Libre", "#e8f5e9", "#4caf50"));
        legend.add(legendItem("Occupé", "#ffebee", "#f44336"));
        legend.add(legendItem("Maintenance", "#fff8e1", "#ff9800"));
        legend.add(legendItem("Réservé", "#e3f2fd", "#2196f3"));
        return legend;
    }

    private HorizontalLayout legendItem(String label, String bg, String border) {
        Div box = new Div();
        box.setWidth("20px");
        box.setHeight("20px");
        box.getStyle().set("background", bg);
        box.getStyle().set("border", "2px solid " + border);
        box.getStyle().set("border-radius", "4px");
        Span text = new Span(label);
        text.getStyle().set("font-size", "var(--lumo-font-size-s)");
        HorizontalLayout item = new HorizontalLayout(box, text);
        item.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        item.setSpacing(false);
        item.getStyle().set("gap", "6px");
        return item;
    }

    private void refreshMap(HospitalService service) {
        mapContainer.removeAll();

        List<Room> rooms;
        if (service != null) {
            rooms = roomRepository.findAll().stream()
                    .filter(r -> r.getService() != null && r.getService().getId().equals(service.getId()))
                    .toList();
        } else {
            rooms = roomRepository.findAll();
        }

        if (rooms.isEmpty()) {
            mapContainer.add(new Paragraph("Aucune chambre configurée."));
            return;
        }

        rooms.forEach(room -> mapContainer.add(buildRoomCard(room)));
    }

    private VerticalLayout buildRoomCard(Room room) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle().set("background", "var(--lumo-base-color)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        card.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        card.setWidth("220px");

        H4 roomTitle = new H4(room.getName());
        roomTitle.getStyle().set("margin", "0");
        Span serviceLabel = new Span(room.getService() != null ? room.getService().getName() : "");
        serviceLabel.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        serviceLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");

        card.add(roomTitle, serviceLabel);

        if (room.getBeds() == null || room.getBeds().isEmpty()) {
            card.add(new Span("Aucun lit"));
        } else {
            FlexLayout bedsLayout = new FlexLayout();
            bedsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
            bedsLayout.getStyle().set("gap", "8px");

            room.getBeds().forEach(bed -> bedsLayout.add(buildBedChip(bed)));
            card.add(bedsLayout);
        }

        // Stats
        long occupied = room.getBeds() == null ? 0 :
                room.getBeds().stream().filter(b -> b.getStatus() == Bed.BedStatus.OCCUPIED).count();
        long total = room.getBeds() == null ? 0 : room.getBeds().size();
        Span stats = new Span(occupied + "/" + total + " occupés");
        stats.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        stats.getStyle().set("color", "var(--lumo-secondary-text-color)");
        card.add(stats);

        return card;
    }

    private Div buildBedChip(Bed bed) {
        Div chip = new Div();
        chip.setText(bed.getLabel());
        chip.setWidth("45px");
        chip.setHeight("45px");
        chip.getStyle().set("display", "flex");
        chip.getStyle().set("align-items", "center");
        chip.getStyle().set("justify-content", "center");
        chip.getStyle().set("border-radius", "8px");
        chip.getStyle().set("font-weight", "600");
        chip.getStyle().set("font-size", "var(--lumo-font-size-s)");
        chip.getStyle().set("cursor", "default");

        switch (bed.getStatus()) {
            case AVAILABLE -> {
                chip.getStyle().set("background", "#e8f5e9");
                chip.getStyle().set("border", "2px solid #4caf50");
                chip.getStyle().set("color", "#2e7d32");
            }
            case OCCUPIED -> {
                chip.getStyle().set("background", "#ffebee");
                chip.getStyle().set("border", "2px solid #f44336");
                chip.getStyle().set("color", "#c62828");
            }
            case MAINTENANCE -> {
                chip.getStyle().set("background", "#fff8e1");
                chip.getStyle().set("border", "2px solid #ff9800");
                chip.getStyle().set("color", "#e65100");
            }
            case RESERVED -> {
                chip.getStyle().set("background", "#e3f2fd");
                chip.getStyle().set("border", "2px solid #2196f3");
                chip.getStyle().set("color", "#0d47a1");
            }
        }

        return chip;
    }
}
