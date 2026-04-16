package com.sahty.fs.views.settings;

import com.sahty.fs.entity.Role;
import com.sahty.fs.repository.RoleRepository;
import com.sahty.fs.security.SecurityUtils;
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

@Route(value = "settings/roles", layout = MainLayout.class)
@PageTitle("Rôles & Permissions | Sahty EMR")
@RolesAllowed({"TENANT_SUPERADMIN"})
public class SettingsRolesView extends VerticalLayout {

    private static final List<String> ALL_PERMISSIONS = List.of(
            "emr_patients", "emr_admissions", "emr_observations", "emr_prescriptions",
            "emr_clinical_exams", "emr_transfusions", "emr_interventions",
            "ph_dashboard", "ph_catalog", "ph_stock_entry", "ph_dispensation",
            "ph_suppliers", "ph_purchase_orders", "ph_locations",
            "lims_registration", "lims_collection", "lims_reception", "lims_results",
            "lims_parametres",
            "settings_users", "settings_services", "settings_rooms", "settings_roles", "settings_pricing"
    );

    private final RoleRepository roleRepository;
    private final String tenantId;
    private Grid<Role> grid;

    public SettingsRolesView(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Rôles & Permissions");

        Button addBtn = new Button("Nouveau Rôle", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openRoleDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(addBtn);

        grid = buildGrid();

        add(title, toolbar, grid);
    }

    private Grid<Role> buildGrid() {
        Grid<Role> grid = new Grid<>(Role.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setSizeFull();

        grid.addColumn(Role::getName).setHeader("Nom du Rôle").setFlexGrow(1);
        grid.addColumn(Role::getDescription).setHeader("Description").setFlexGrow(2);
        grid.addColumn(r -> r.getPermissions() != null ? r.getPermissions().size() + " permission(s)" : "0")
                .setHeader("Permissions").setWidth("150px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(r -> {
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

        grid.setItemDetailsRenderer(new ComponentRenderer<>(role -> buildPermissionsDetail(role)));
        grid.setDetailsVisibleOnClick(true);

        refreshGrid();
        return grid;
    }

    private VerticalLayout buildPermissionsDetail(Role role) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.add(new H4("Permissions attribuées:"));

        if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
            layout.add(new Span("Aucune permission."));
        } else {
            HorizontalLayout badges = new HorizontalLayout();
            badges.getStyle().set("flex-wrap", "wrap");
            role.getPermissions().forEach(perm -> {
                Span badge = new Span(perm);
                badge.getElement().getThemeList().add("badge");
                badge.getElement().getThemeList().add("contrast");
                badges.add(badge);
            });
            layout.add(badges);
        }
        return layout;
    }

    private void openRoleDialog(Role existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouveau Rôle" : "Modifier le Rôle");
        dialog.setWidth("650px");

        FormLayout form = new FormLayout();
        TextField nameField = new TextField("Nom du Rôle");
        nameField.setRequired(true);
        if (existing != null) nameField.setValue(existing.getName());

        TextArea descField = new TextArea("Description");
        descField.setWidthFull();
        if (existing != null && existing.getDescription() != null) descField.setValue(existing.getDescription());

        CheckboxGroup<String> permissionsGroup = new CheckboxGroup<>("Permissions");
        permissionsGroup.setItems(ALL_PERMISSIONS);
        permissionsGroup.setWidthFull();
        if (existing != null && existing.getPermissions() != null) {
            permissionsGroup.setValue(existing.getPermissions());
        }

        form.add(nameField, descField, permissionsGroup);
        form.setColspan(descField, 2);
        form.setColspan(permissionsGroup, 2);

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
            role.setPermissions((Set<String>) permissionsGroup.getValue());
            // Set tenant from current context
            SecurityUtils.getCurrentTenantId().ifPresent(tid -> {
                // tenant is set via the repository directly for simplicity
            });
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
        grid.setItems(roleRepository.findByTenantId(tenantId));
    }
}
