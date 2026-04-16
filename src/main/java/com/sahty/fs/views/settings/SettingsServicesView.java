package com.sahty.fs.views.settings;

import com.sahty.fs.entity.HospitalService;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.HospitalServiceService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "settings/services", layout = MainLayout.class)
@PageTitle("Services | Paramètres")
@RolesAllowed({"ROLE_TENANT_SUPERADMIN", "ROLE_SUPER_ADMIN"})
public class SettingsServicesView extends VerticalLayout {

    private final HospitalServiceService hospitalServiceService;
    private final String tenantId;
    private Grid<HospitalService> grid;

    public SettingsServicesView(HospitalServiceService hospitalServiceService) {
        this.hospitalServiceService = hospitalServiceService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Gestion des Services Hospitaliers");

        Button addBtn = new Button("Nouveau Service", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openDialog(null));

        grid = buildGrid();
        refreshGrid();
        grid.setSizeFull();

        add(title, new HorizontalLayout(addBtn), grid);
    }

    private Grid<HospitalService> buildGrid() {
        Grid<HospitalService> g = new Grid<>(HospitalService.class, false);
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        g.addColumn(HospitalService::getCode).setHeader("Code").setWidth("100px").setFlexGrow(0);
        g.addColumn(HospitalService::getName).setHeader("Nom").setFlexGrow(1);
        g.addColumn(HospitalService::getDescription).setHeader("Description");
        g.addColumn(s -> s.getRooms().size() + " chambre(s)").setHeader("Chambres");
        g.addColumn(new ComponentRenderer<>(service -> {
            Button btn = new Button(VaadinIcon.EDIT.create());
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btn.addClickListener(e -> openDialog(service));
            return btn;
        })).setHeader("Action");
        return g;
    }

    private void openDialog(HospitalService existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouveau Service" : "Modifier Service");
        HospitalService service = existing != null ? existing : new HospitalService();

        TextField name = new TextField("Nom"); name.setValue(nvl(service.getName())); name.setRequired(true);
        TextField code = new TextField("Code"); code.setValue(nvl(service.getCode()));
        TextField description = new TextField("Description"); description.setValue(nvl(service.getDescription()));
        TextField color = new TextField("Couleur (hex)"); color.setValue(nvl(service.getColor()));

        FormLayout form = new FormLayout(name, code, description, color);
        dialog.add(form);

        Button save = new Button("Enregistrer", e -> {
            service.setName(name.getValue()); service.setCode(code.getValue());
            service.setDescription(description.getValue()); service.setColor(color.getValue());
            service.setActive(true);
            try {
                hospitalServiceService.save(service, tenantId);
                refreshGrid();
                dialog.close();
                Notification.show("Service enregistré", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new Button("Annuler", ev -> dialog.close()), save);
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(hospitalServiceService.findByTenant(tenantId));
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
