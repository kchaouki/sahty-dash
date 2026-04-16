package com.sahty.fs.views.superadmin;

import com.sahty.fs.entity.Organism;
import com.sahty.fs.repository.OrganismRepository;
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
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        g.addColumn(Organism::getName).setHeader("Nom").setFlexGrow(1);
        g.addColumn(Organism::getCode).setHeader("Code");
        g.addColumn(Organism::getType).setHeader("Type");
        g.addColumn(Organism::getCountry).setHeader("Pays");
        g.addColumn(Organism::getContactPhone).setHeader("Téléphone");
        g.addColumn(Organism::getContactEmail).setHeader("Email");
        g.addColumn(new ComponentRenderer<>(o -> {
            Button btn = new Button(VaadinIcon.EDIT.create());
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btn.addClickListener(e -> openDialog(o));
            return btn;
        })).setHeader("Action");
        return g;
    }

    private void openDialog(Organism existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouvel Organisme" : "Modifier Organisme");
        Organism org = existing != null ? existing : new Organism();

        TextField name = new TextField("Nom"); name.setValue(nvl(org.getName())); name.setRequired(true);
        TextField code = new TextField("Code"); code.setValue(nvl(org.getCode()));
        TextField type = new TextField("Type (CNSS, CNOPS, AMO...)"); type.setValue(nvl(org.getType()));
        TextField country = new TextField("Pays"); country.setValue(nvl(org.getCountry()));
        TextField phone = new TextField("Téléphone"); phone.setValue(nvl(org.getContactPhone()));
        EmailField email = new EmailField("Email"); email.setValue(nvl(org.getContactEmail()));

        FormLayout form = new FormLayout(name, code, type, country, phone, email);
        dialog.add(form);

        Button save = new Button("Enregistrer", e -> {
            org.setName(name.getValue()); org.setCode(code.getValue());
            org.setType(type.getValue()); org.setCountry(country.getValue());
            org.setContactPhone(phone.getValue()); org.setContactEmail(email.getValue());
            org.setActive(true);
            organismRepository.save(org);
            grid.setItems(organismRepository.findAll());
            dialog.close();
            Notification.show("Organisme enregistré", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new Button("Annuler", ev -> dialog.close()), save);
        dialog.open();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
