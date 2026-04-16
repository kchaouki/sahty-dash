package com.sahty.fs.views.superadmin;

import com.sahty.fs.entity.Tenant;
import com.sahty.fs.entity.TenantGroup;
import com.sahty.fs.repository.TenantGroupRepository;
import com.sahty.fs.repository.TenantRepository;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
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

import java.util.HashSet;
import java.util.List;

@Route(value = "superadmin/groups", layout = MainLayout.class)
@PageTitle("Groupes de Clients | Sahty EMR")
@RolesAllowed({"SUPER_ADMIN"})
public class SuperAdminGroupsView extends VerticalLayout {

    private final TenantGroupRepository groupRepository;
    private final TenantRepository tenantRepository;
    private Grid<TenantGroup> grid;

    public SuperAdminGroupsView(TenantGroupRepository groupRepository, TenantRepository tenantRepository) {
        this.groupRepository = groupRepository;
        this.tenantRepository = tenantRepository;

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Groupes de Clients");

        Button addBtn = new Button("Nouveau Groupe", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openGroupDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(addBtn);

        grid = buildGrid();
        add(title, toolbar, grid);
    }

    private Grid<TenantGroup> buildGrid() {
        Grid<TenantGroup> grid = new Grid<>(TenantGroup.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setSizeFull();

        grid.addColumn(TenantGroup::getName).setHeader("Nom du Groupe").setFlexGrow(1);
        grid.addColumn(TenantGroup::getDescription).setHeader("Description").setFlexGrow(2);
        grid.addColumn(g -> g.getTenants() != null ? g.getTenants().size() + " client(s)" : "0")
                .setHeader("Clients").setWidth("120px").setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(g -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openGroupDialog(g));

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.addClickListener(e -> {
                groupRepository.delete(g);
                refreshGrid();
                Notification.show("Groupe supprimé", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });

            return new HorizontalLayout(editBtn, deleteBtn);
        })).setHeader("Actions").setWidth("120px").setFlexGrow(0);

        refreshGrid();
        return grid;
    }

    private void openGroupDialog(TenantGroup existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouveau Groupe" : "Modifier le Groupe");
        dialog.setWidth("600px");

        FormLayout form = new FormLayout();
        TextField nameField = new TextField("Nom du Groupe");
        nameField.setRequired(true);
        if (existing != null) nameField.setValue(existing.getName());

        TextArea descField = new TextArea("Description");
        descField.setWidthFull();
        if (existing != null && existing.getDescription() != null) descField.setValue(existing.getDescription());

        MultiSelectListBox<Tenant> tenantsBox = new MultiSelectListBox<>();
        List<Tenant> allTenants = tenantRepository.findAll();
        tenantsBox.setItems(allTenants);
        tenantsBox.setItemLabelGenerator(Tenant::getName);
        tenantsBox.setHeight("200px");
        if (existing != null && existing.getTenants() != null) {
            tenantsBox.setValue(new HashSet<>(existing.getTenants()));
        }

        form.add(nameField, descField, tenantsBox);
        form.setColspan(descField, 2);
        form.setColspan(tenantsBox, 2);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (nameField.isEmpty()) {
                Notification.show("Le nom est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            TenantGroup group = existing != null ? existing : new TenantGroup();
            group.setName(nameField.getValue());
            group.setDescription(descField.getValue());
            group.setTenants(new HashSet<>(tenantsBox.getValue()));
            groupRepository.save(group);
            dialog.close();
            refreshGrid();
            Notification.show("Groupe enregistré", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(groupRepository.findAll());
    }
}
