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
        Grid<Tenant> g = new Grid<>(Tenant.class, false);
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        g.addColumn(Tenant::getDesignation).setHeader("Désignation").setFlexGrow(1).setSortable(true);
        g.addColumn(Tenant::getType).setHeader("Type").setWidth("120px").setFlexGrow(0);
        g.addColumn(Tenant::getTenancyMode).setHeader("Mode").setWidth("120px").setFlexGrow(0);
        g.addColumn(Tenant::getCity).setHeader("Ville").setWidth("110px").setFlexGrow(0);
        g.addColumn(Tenant::getCountry).setHeader("Pays").setWidth("80px").setFlexGrow(0);
        g.addColumn(Tenant::getEmail).setHeader("Email").setFlexGrow(1);
        g.addColumn(Tenant::getPhone).setHeader("Téléphone").setWidth("130px").setFlexGrow(0);
        g.addColumn(t -> t.getGroup() != null ? t.getGroup().getName() : "-").setHeader("Groupe").setWidth("120px").setFlexGrow(0);

        g.addColumn(new ComponentRenderer<>(tenant -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openDialog(tenant));
            return editBtn;
        })).setHeader("Actions").setWidth("80px").setFlexGrow(0);

        return g;
    }

    private void openDialog(Tenant existingTenant) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingTenant == null ? "Nouveau Client" : "Modifier Client");
        dialog.setWidth("700px");

        Tenant tenant = existingTenant != null ? existingTenant : new Tenant();

        TextField designation = new TextField("Désignation (Nom commercial)");
        designation.setValue(nvl(tenant.getDesignation()));
        designation.setRequired(true);

        TextField siegeSocial = new TextField("Siège Social");
        siegeSocial.setValue(nvl(tenant.getSiegeSocial()));

        TextField representantLegal = new TextField("Représentant Légal");
        representantLegal.setValue(nvl(tenant.getRepresentantLegal()));

        ComboBox<String> type = new ComboBox<>("Type");
        type.setItems("CLINIQUE", "HOPITAL", "POLYCLINIQUE", "LABORATOIRE", "CABINET", "AUTRE");
        type.setValue(tenant.getType() != null ? tenant.getType() : "CLINIQUE");

        ComboBox<String> tenancyMode = new ComboBox<>("Mode de Fonctionnement");
        tenancyMode.setItems("SHARED", "DEDICATED");
        tenancyMode.setValue(tenant.getTenancyMode() != null ? tenant.getTenancyMode() : "SHARED");

        TextField address = new TextField("Adresse");
        address.setValue(nvl(tenant.getAddress()));

        TextField city = new TextField("Ville");
        city.setValue(nvl(tenant.getCity()));

        TextField country = new TextField("Pays");
        country.setValue(tenant.getCountry() != null ? tenant.getCountry() : "MA");

        TextField phone = new TextField("Téléphone");
        phone.setValue(nvl(tenant.getPhone()));

        EmailField email = new EmailField("Email");
        email.setValue(nvl(tenant.getEmail()));

        FormLayout form = new FormLayout(
                designation, type, siegeSocial, tenancyMode,
                representantLegal, phone, email, address, city, country
        );
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));
        form.setColspan(designation, 2);
        form.setColspan(address, 2);

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button saveBtn = new Button("Enregistrer", e -> {
            if (designation.isEmpty()) {
                Notification.show("La désignation est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            tenant.setDesignation(designation.getValue().trim());
            tenant.setSiegeSocial(siegeSocial.getValue());
            tenant.setRepresentantLegal(representantLegal.getValue());
            tenant.setType(type.getValue());
            tenant.setTenancyMode(tenancyMode.getValue());
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
                Notification.show("Client enregistré", 3000, Notification.Position.BOTTOM_END)
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
