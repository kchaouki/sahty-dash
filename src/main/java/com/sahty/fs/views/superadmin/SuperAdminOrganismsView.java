package com.sahty.fs.views.superadmin;

import com.sahty.fs.entity.Organism;
import com.sahty.fs.repository.OrganismRepository;
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

@Route(value = "super-admin/organismes", layout = MainLayout.class)
@PageTitle("Organismes | Super Admin")
@RolesAllowed("ROLE_SUPER_ADMIN")
public class SuperAdminOrganismsView extends VerticalLayout {

    private final OrganismRepository organismRepository;
    private Grid<Organism> grid;

    public SuperAdminOrganismsView(OrganismRepository organismRepository) {
        this.organismRepository = organismRepository;
        setSizeFull();
        setPadding(true);

        H2 title = new H2("Organismes d'Assurance");

        Button addBtn = new Button("Nouvel Organisme", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openDialog(null));

        grid = buildGrid();
        grid.setItems(organismRepository.findAll());
        grid.setSizeFull();

        add(title, new HorizontalLayout(addBtn), grid);
    }

    private Grid<Organism> buildGrid() {
        Grid<Organism> g = new Grid<>(Organism.class, false);
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        g.addColumn(Organism::getDesignation).setHeader("Désignation").setFlexGrow(1);
        g.addColumn(Organism::getCode).setHeader("Code").setWidth("100px").setFlexGrow(0);
        g.addColumn(Organism::getType).setHeader("Type").setWidth("120px").setFlexGrow(0);
        g.addColumn(Organism::getSubType).setHeader("Sous-type").setWidth("120px").setFlexGrow(0);
        g.addColumn(Organism::getCategory).setHeader("Catégorie").setWidth("120px").setFlexGrow(0);
        g.addColumn(Organism::getCountry).setHeader("Pays").setWidth("80px").setFlexGrow(0);
        g.addColumn(Organism::getContactPhone).setHeader("Téléphone").setWidth("130px").setFlexGrow(0);
        g.addColumn(Organism::getContactEmail).setHeader("Email").setFlexGrow(1);
        g.addColumn(new ComponentRenderer<>(o -> {
            Button btn = new Button(VaadinIcon.EDIT.create());
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btn.addClickListener(e -> openDialog(o));
            return btn;
        })).setHeader("Action").setWidth("70px").setFlexGrow(0);
        return g;
    }

    private void openDialog(Organism existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouvel Organisme" : "Modifier Organisme");
        dialog.setWidth("650px");
        Organism org = existing != null ? existing : new Organism();

        TextField designation = new TextField("Désignation");
        designation.setValue(nvl(org.getDesignation()));
        designation.setRequired(true);

        TextField code = new TextField("Code");
        code.setValue(nvl(org.getCode()));

        ComboBox<String> type = new ComboBox<>("Type");
        type.setItems("CNSS", "CNOPS", "AMO", "RAMED", "MUTUELLE", "ASSURANCE", "AUTRE");
        type.setValue(org.getType() != null ? org.getType() : "CNSS");
        type.setAllowCustomValue(true);
        type.addCustomValueSetListener(e -> type.setValue(e.getDetail()));

        TextField subType = new TextField("Sous-type");
        subType.setValue(nvl(org.getSubType()));

        TextField category = new TextField("Catégorie");
        category.setValue(nvl(org.getCategory()));

        TextField country = new TextField("Pays");
        country.setValue(nvl(org.getCountry()));

        TextField phone = new TextField("Téléphone");
        phone.setValue(nvl(org.getContactPhone()));

        EmailField email = new EmailField("Email");
        email.setValue(nvl(org.getContactEmail()));

        FormLayout form = new FormLayout(designation, code, type, subType, category, country, phone, email);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));
        form.setColspan(designation, 2);
        dialog.add(form);

        Button save = new Button("Enregistrer", e -> {
            if (designation.isEmpty()) {
                Notification.show("La désignation est obligatoire", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            org.setDesignation(designation.getValue().trim());
            org.setCode(code.getValue());
            org.setType(type.getValue());
            org.setSubType(subType.getValue());
            org.setCategory(category.getValue());
            org.setCountry(country.getValue());
            org.setContactPhone(phone.getValue());
            org.setContactEmail(email.getValue());
            org.setActive(true);
            organismRepository.save(org);
            grid.setItems(organismRepository.findAll());
            dialog.close();
            Notification.show("Organisme enregistré", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Annuler", ev -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
