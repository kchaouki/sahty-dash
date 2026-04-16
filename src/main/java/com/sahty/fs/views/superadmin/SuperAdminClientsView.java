package com.sahty.fs.views.superadmin;

import com.sahty.fs.entity.Tenant;
import com.sahty.fs.service.TenantService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "super-admin/clients", layout = MainLayout.class)
@PageTitle("Clients | Super Admin")
@RolesAllowed("ROLE_SUPER_ADMIN")
public class SuperAdminClientsView extends VerticalLayout {

    private final TenantService tenantService;
    private Grid<Tenant> grid;

    public SuperAdminClientsView(TenantService tenantService) {
        this.tenantService = tenantService;

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Gestion des Clients (Tenants)");

        Button addBtn = new Button("Nouveau Client", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(addBtn);

        grid = buildGrid();
        grid.setItems(tenantService.findAll());
        grid.setSizeFull();

        add(title, toolbar, grid);
    }

    private Grid<Tenant> buildGrid() {
        Grid<Tenant> grid = new Grid<>(Tenant.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        grid.addColumn(Tenant::getCode).setHeader("Code").setWidth("120px").setFlexGrow(0);
        grid.addColumn(Tenant::getName).setHeader("Nom").setFlexGrow(1).setSortable(true);
        grid.addColumn(Tenant::getCity).setHeader("Ville");
        grid.addColumn(Tenant::getCountry).setHeader("Pays");
        grid.addColumn(Tenant::getEmail).setHeader("Email");
        grid.addColumn(Tenant::getPhone).setHeader("Téléphone");
        grid.addColumn(t -> t.getGroup() != null ? t.getGroup().getName() : "-").setHeader("Groupe");
        grid.addColumn(new ComponentRenderer<>(tenant -> {
            Span badge = new Span(tenant.getStatus().name());
            badge.getElement().getThemeList().add("badge");
            switch(tenant.getStatus()) {
                case ACTIVE -> badge.getElement().getThemeList().add("success");
                case SUSPENDED -> badge.getElement().getThemeList().add("warning");
                case INACTIVE -> badge.getElement().getThemeList().add("error");
            }
            return badge;
        })).setHeader("Statut");

        grid.addColumn(new ComponentRenderer<>(tenant -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openDialog(tenant));

            Button suspendBtn = new Button(VaadinIcon.PAUSE.create());
            suspendBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY,
                    ButtonVariant.LUMO_ERROR);
            suspendBtn.setEnabled(tenant.getStatus() == Tenant.TenantStatus.ACTIVE);
            suspendBtn.addClickListener(e -> {
                tenantService.suspend(tenant.getId());
                grid.setItems(tenantService.findAll());
            });

            return new HorizontalLayout(editBtn, suspendBtn);
        })).setHeader("Actions");

        return grid;
    }

    private void openDialog(Tenant existingTenant) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingTenant == null ? "Nouveau Client" : "Modifier Client");
        dialog.setWidth("700px");

        Tenant tenant = existingTenant != null ? existingTenant : new Tenant();

        TextField code = new TextField("Code");
        code.setValue(nvl(tenant.getCode()));
        code.setRequired(true);
        code.setReadOnly(existingTenant != null);

        TextField name = new TextField("Nom");
        name.setValue(nvl(tenant.getName()));
        name.setRequired(true);

        TextField address = new TextField("Adresse");
        address.setValue(nvl(tenant.getAddress()));

        TextField city = new TextField("Ville");
        city.setValue(nvl(tenant.getCity()));

        TextField country = new TextField("Pays");
        country.setValue(nvl(tenant.getCountry()));
        country.setValue(country.getValue().isEmpty() ? "MA" : country.getValue());

        TextField phone = new TextField("Téléphone");
        phone.setValue(nvl(tenant.getPhone()));

        EmailField email = new EmailField("Email");
        email.setValue(nvl(tenant.getEmail()));

        FormLayout form = new FormLayout(code, name, address, city, country, phone, email);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));
        form.setColspan(address, 2);

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        Button saveBtn = new Button("Enregistrer", e -> {
            tenant.setCode(code.getValue().trim().toUpperCase());
            tenant.setName(name.getValue().trim());
            tenant.setAddress(address.getValue());
            tenant.setCity(city.getValue());
            tenant.setCountry(country.getValue());
            tenant.setPhone(phone.getValue());
            tenant.setEmail(email.getValue());

            try {
                if (existingTenant == null) {
                    tenantService.create(tenant);
                } else {
                    tenantService.update(tenant);
                }
                grid.setItems(tenantService.findAll());
                dialog.close();
                Notification.show("Client enregistré avec succès", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
