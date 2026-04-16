package com.sahty.fs.views.superadmin;

import com.sahty.fs.entity.Role;
import com.sahty.fs.repository.RoleRepository;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Set;

@Route(value = "superadmin/roles", layout = MainLayout.class)
@PageTitle("Rôles Globaux | Sahty EMR")
@RolesAllowed({"SUPER_ADMIN"})
public class SuperAdminRolesView extends VerticalLayout {

    private static final List<String> ALL_PERMISSIONS = List.of(
            "emr_patients", "emr_admissions", "emr_observations", "emr_prescriptions",
            "emr_clinical_exams", "emr_transfusions", "emr_interventions",
            "ph_dashboard", "ph_catalog", "ph_stock_entry", "ph_dispensation",
            "ph_suppliers", "ph_purchase_orders", "ph_locations",
            "lims_registration", "lims_collection", "lims_reception", "lims_results", "lims_parametres",
            "settings_users", "settings_services", "settings_rooms", "settings_roles", "settings_pricing",
            "superadmin_clients", "superadmin_groups", "superadmin_roles", "superadmin_products",
            "superadmin_lims_catalogs", "superadmin_suppliers"
    );

    private final RoleRepository roleRepository;
    private Grid<Role> grid;

    public SuperAdminRolesView(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Rôles Globaux");

        Button addBtn = new Button("Nouveau Rôle Global", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openRoleDialog(null));

        grid = buildGrid();
        add(title, new HorizontalLayout(addBtn), grid);
    }

    private Grid<Role> buildGrid() {
        Grid<Role> g = new Grid<>(Role.class, false);
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        g.setSizeFull();

        g.addColumn(Role::getName).setHeader("Nom du Rôle").setFlexGrow(1);
        g.addColumn(Role::getDescription).setHeader("Description").setFlexGrow(2);
        g.addColumn(r -> r.getPermissions() != null ? r.getPermissions().size() + " permission(s)" : "0")
                .setHeader("Permissions").setWidth("150px").setFlexGrow(0);

        g.addColumn(new ComponentRenderer<>(r -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openRoleDialog(r));

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.addClickListener(e -> {
                roleRepository.delete(r);
                refreshGrid();
                Notification.show("Rôle supprimé", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            return new HorizontalLayout(editBtn, deleteBtn);
        })).setHeader("Actions").setWidth("120px").setFlexGrow(0);

        g.setItemDetailsRenderer(new ComponentRenderer<>(role -> {
            VerticalLayout detail = new VerticalLayout();
            detail.setPadding(true);
            detail.add(new H4("Permissions:"));
            if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
                detail.add(new Span("Aucune permission."));
            } else {
                HorizontalLayout badges = new HorizontalLayout();
                badges.getStyle().set("flex-wrap", "wrap");
                role.getPermissions().forEach(p -> {
                    Span badge = new Span(p);
                    badge.getElement().getThemeList().add("badge");
                    badge.getElement().getThemeList().add("contrast");
                    badges.add(badge);
                });
                detail.add(badges);
            }
            return detail;
        }));
        g.setDetailsVisibleOnClick(true);

        refreshGrid();
        return g;
    }

    private void openRoleDialog(Role existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouveau Rôle Global" : "Modifier le Rôle");
        dialog.setWidth("650px");

        FormLayout form = new FormLayout();
        TextField nameField = new TextField("Nom");
        nameField.setRequired(true);
        if (existing != null) nameField.setValue(existing.getName());

        TextArea descField = new TextArea("Description");
        descField.setWidthFull();
        if (existing != null && existing.getDescription() != null) descField.setValue(existing.getDescription());

        CheckboxGroup<String> permsGroup = new CheckboxGroup<>("Permissions");
        permsGroup.setItems(ALL_PERMISSIONS);
        permsGroup.setWidthFull();
        if (existing != null && existing.getPermissions() != null) permsGroup.setValue(existing.getPermissions());

        form.add(nameField, descField, permsGroup);
        form.setColspan(descField, 2);
        form.setColspan(permsGroup, 2);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (nameField.isEmpty()) {
                Notification.show("Le nom est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Role role = existing != null ? existing : new Role();
            role.setName(nameField.getValue());
            role.setDescription(descField.getValue());
            role.setPermissions((Set<String>) permsGroup.getValue());
            roleRepository.save(role);
            dialog.close();
            refreshGrid();
            Notification.show("Rôle enregistré", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void refreshGrid() {
        // Global roles have no tenant
        grid.setItems(roleRepository.findByTenantId(null));
    }
}
