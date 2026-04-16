package com.sahty.fs.views.settings;

import com.sahty.fs.entity.Bed;
import com.sahty.fs.entity.HospitalService;
import com.sahty.fs.entity.Room;
import com.sahty.fs.repository.BedRepository;
import com.sahty.fs.repository.RoomRepository;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.HospitalServiceService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "settings/rooms", layout = MainLayout.class)
@PageTitle("Chambres & Lits | Sahty EMR")
@RolesAllowed({"ROLE_TENANT_SUPERADMIN", "ROLE_SUPER_ADMIN"})
public class SettingsRoomsView extends VerticalLayout {

    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final HospitalServiceService hospitalServiceService;
    private final String tenantId;

    private Grid<Room> roomGrid;

    public SettingsRoomsView(RoomRepository roomRepository, BedRepository bedRepository,
                              HospitalServiceService hospitalServiceService) {
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
        this.hospitalServiceService = hospitalServiceService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Gestion des Chambres & Lits");

        Button addRoomBtn = new Button("Nouvelle Chambre", VaadinIcon.PLUS.create());
        addRoomBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addRoomBtn.addClickListener(e -> openRoomDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(addRoomBtn);

        roomGrid = buildRoomGrid();

        add(title, toolbar, roomGrid);
    }

    private Grid<Room> buildRoomGrid() {
        Grid<Room> grid = new Grid<>(Room.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setSizeFull();

        grid.addColumn(Room::getName).setHeader("Chambre").setWidth("150px").setFlexGrow(0);
        grid.addColumn(r -> r.getService() != null ? r.getService().getName() : "-")
                .setHeader("Service").setFlexGrow(1);
        grid.addColumn(Room::getFloor).setHeader("Étage").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Room::getCapacity).setHeader("Capacité").setWidth("90px").setFlexGrow(0);
        grid.addColumn(r -> r.getBeds() != null ? r.getBeds().size() : 0)
                .setHeader("Lits").setWidth("60px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(r -> {
            Span badge = new Span(r.isActive() ? "Active" : "Inactive");
            badge.getElement().getThemeList().add("badge");
            if (r.isActive()) badge.getElement().getThemeList().add("success");
            else badge.getElement().getThemeList().add("error");
            return badge;
        })).setHeader("Statut").setWidth("90px").setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(r -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openRoomDialog(r));

            Button addBedBtn = new Button(VaadinIcon.PLUS.create());
            addBedBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            addBedBtn.getElement().setAttribute("title", "Ajouter un lit");
            addBedBtn.addClickListener(e -> openBedDialog(r, null));

            Button toggleBtn = new Button(r.isActive() ? VaadinIcon.EYE_SLASH.create() : VaadinIcon.EYE.create());
            toggleBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            toggleBtn.addClickListener(e -> {
                r.setActive(!r.isActive());
                roomRepository.save(r);
                refreshGrid();
            });

            return new HorizontalLayout(editBtn, addBedBtn, toggleBtn);
        })).setHeader("Actions").setWidth("140px").setFlexGrow(0);

        grid.setItemDetailsRenderer(new ComponentRenderer<>(room -> buildBedDetails(room)));
        grid.setDetailsVisibleOnClick(true);

        refreshGrid();
        return grid;
    }

    private VerticalLayout buildBedDetails(Room room) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(false);

        H4 title = new H4("Lits de la chambre " + room.getName());
        layout.add(title);

        if (room.getBeds() == null || room.getBeds().isEmpty()) {
            layout.add(new Span("Aucun lit configuré."));
        } else {
            Grid<Bed> bedGrid = new Grid<>(Bed.class, false);
            bedGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
            bedGrid.addColumn(Bed::getLabel).setHeader("Label").setWidth("100px").setFlexGrow(0);
            bedGrid.addColumn(new ComponentRenderer<>(b -> {
                Span s = new Span(b.getStatus() != null ? b.getStatus().name() : "");
                s.getElement().getThemeList().add("badge");
                if (b.getStatus() == Bed.BedStatus.OCCUPIED) s.getElement().getThemeList().add("error");
                else if (b.getStatus() == Bed.BedStatus.AVAILABLE) s.getElement().getThemeList().add("success");
                else s.getElement().getThemeList().add("contrast");
                return s;
            })).setHeader("État").setWidth("130px").setFlexGrow(0);
            bedGrid.addColumn(new ComponentRenderer<>(b -> {
                Button editBtn = new Button(VaadinIcon.EDIT.create());
                editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                editBtn.addClickListener(e -> openBedDialog(room, b));
                return editBtn;
            })).setHeader("").setWidth("60px").setFlexGrow(0);
            bedGrid.setItems(room.getBeds());
            bedGrid.setHeight("200px");
            layout.add(bedGrid);
        }

        Button addBedBtn = new Button("Ajouter un Lit", VaadinIcon.PLUS.create());
        addBedBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addBedBtn.addClickListener(e -> openBedDialog(room, null));
        layout.add(addBedBtn);

        return layout;
    }

    private void openRoomDialog(Room existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouvelle Chambre" : "Modifier la Chambre");
        dialog.setWidth("500px");

        FormLayout form = new FormLayout();
        TextField nameField = new TextField("Nom / Numéro de Chambre");
        nameField.setRequired(true);
        if (existing != null) nameField.setValue(existing.getName());

        ComboBox<HospitalService> serviceField = new ComboBox<>("Service");
        List<HospitalService> services = hospitalServiceService.findByTenant(tenantId);
        serviceField.setItems(services);
        serviceField.setItemLabelGenerator(HospitalService::getName);
        if (existing != null && existing.getService() != null) serviceField.setValue(existing.getService());

        TextField floorField = new TextField("Étage");
        if (existing != null) floorField.setValue(existing.getFloor() != null ? existing.getFloor() : "");

        IntegerField capacityField = new IntegerField("Capacité");
        capacityField.setMin(1);
        capacityField.setValue(existing != null ? existing.getCapacity() : 1);

        form.add(nameField, serviceField, floorField, capacityField);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (nameField.isEmpty()) {
                Notification.show("Le nom est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Room room = existing != null ? existing : new Room();
            room.setName(nameField.getValue());
            room.setService(serviceField.getValue());
            room.setFloor(floorField.getValue());
            room.setCapacity(capacityField.getValue() != null ? capacityField.getValue() : 1);
            roomRepository.save(room);
            dialog.close();
            refreshGrid();
            Notification.show("Chambre enregistrée", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openBedDialog(Room room, Bed existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouveau Lit — " + room.getName() : "Modifier le Lit");
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();
        TextField labelField = new TextField("Label du Lit (ex: A, B, 01)");
        labelField.setRequired(true);
        if (existing != null) labelField.setValue(existing.getLabel());

        ComboBox<Bed.BedStatus> statusField = new ComboBox<>("Statut");
        statusField.setItems(Bed.BedStatus.values());
        statusField.setItemLabelGenerator(Bed.BedStatus::name);
        statusField.setValue(existing != null && existing.getStatus() != null
                ? existing.getStatus() : Bed.BedStatus.AVAILABLE);

        form.add(labelField, statusField);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (labelField.isEmpty()) {
                Notification.show("Le label est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Bed bed = existing != null ? existing : new Bed();
            bed.setLabel(labelField.getValue());
            bed.setStatus(statusField.getValue());
            bed.setRoom(room);
            bedRepository.save(bed);
            dialog.close();
            refreshGrid();
            Notification.show("Lit enregistré", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void refreshGrid() {
        roomGrid.setItems(roomRepository.findAll());
    }
}
