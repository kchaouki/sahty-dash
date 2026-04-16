package com.sahty.fs.views.superadmin;

import com.sahty.fs.entity.lims.*;
import com.sahty.fs.repository.lims.LabAnalyteRepository;
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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Route(value = "super-admin/lims-catalogs", layout = MainLayout.class)
@PageTitle("Catalogues LIMS | Super Admin")
@RolesAllowed("ROLE_SUPER_ADMIN")
public class LimsCatalogsView extends VerticalLayout {

    private final LabAnalyteRepository analyteRepository;
    // Note: inject other repos as needed

    public LimsCatalogsView(LabAnalyteRepository analyteRepository) {
        this.analyteRepository = analyteRepository;
        setSizeFull();
        setPadding(true);

        H2 title = new H2("Catalogues Laboratoire");

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add(new Tab("Analytés"), buildAnalytesTab());
        tabs.add(new Tab("Sections"), buildSectionsTab());
        tabs.add(new Tab("Sous-Sections"), buildSubSectionsTab());
        tabs.add(new Tab("Méthodes"), buildMethodsTab());
        tabs.add(new Tab("Spécimens"), buildSpecimensTab());
        tabs.add(new Tab("Conteneurs"), buildContainersTab());

        add(title, tabs);
    }

    private VerticalLayout buildAnalytesTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Button addBtn = new Button("Nouvel Analyté", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Grid<LabAnalyte> grid = new Grid<>(LabAnalyte.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(LabAnalyte::getName).setHeader("Nom").setFlexGrow(1);
        grid.addColumn(LabAnalyte::getCode).setHeader("Code");
        grid.addColumn(LabAnalyte::getUnit).setHeader("Unité");
        grid.addColumn(a -> a.getNormalRangeMin() + " - " + a.getNormalRangeMax()).setHeader("Valeurs Normales");
        grid.addColumn(a -> a.getSection() != null ? a.getSection().getName() : "").setHeader("Section");
        grid.addColumn(new ComponentRenderer<>(a -> {
            Button btn = new Button(VaadinIcon.EDIT.create());
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btn.addClickListener(e -> openAnalyteDialog(a, grid));
            return btn;
        })).setHeader("Action");
        grid.setItems(analyteRepository.findByIsActiveTrue());
        grid.setSizeFull();

        addBtn.addClickListener(e -> openAnalyteDialog(null, grid));
        layout.add(addBtn, grid);
        return layout;
    }

    private void openAnalyteDialog(LabAnalyte existing, Grid<LabAnalyte> grid) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Nouvel Analyté" : "Modifier Analyté");
        LabAnalyte analyte = existing != null ? existing : new LabAnalyte();

        TextField name = new TextField("Nom"); name.setValue(nvl(analyte.getName())); name.setRequired(true);
        TextField code = new TextField("Code"); code.setValue(nvl(analyte.getCode()));
        TextField unit = new TextField("Unité"); unit.setValue(nvl(analyte.getUnit()));
        TextField normalMin = new TextField("Min Normal"); normalMin.setValue(nvl(analyte.getNormalRangeMin()));
        TextField normalMax = new TextField("Max Normal"); normalMax.setValue(nvl(analyte.getNormalRangeMax()));
        TextField criticalMin = new TextField("Min Critique"); criticalMin.setValue(nvl(analyte.getCriticalRangeMin()));
        TextField criticalMax = new TextField("Max Critique"); criticalMax.setValue(nvl(analyte.getCriticalRangeMax()));
        TextField description = new TextField("Description"); description.setValue(nvl(analyte.getDescription()));

        FormLayout form = new FormLayout(name, code, unit, normalMin, normalMax, criticalMin, criticalMax, description);
        form.setColspan(description, 2);
        dialog.add(form);

        Button save = new Button("Enregistrer", e -> {
            analyte.setName(name.getValue()); analyte.setCode(code.getValue());
            analyte.setUnit(unit.getValue());
            analyte.setNormalRangeMin(normalMin.getValue()); analyte.setNormalRangeMax(normalMax.getValue());
            analyte.setCriticalRangeMin(criticalMin.getValue()); analyte.setCriticalRangeMax(criticalMax.getValue());
            analyte.setDescription(description.getValue());
            analyte.setActive(true);
            analyteRepository.save(analyte);
            grid.setItems(analyteRepository.findByIsActiveTrue());
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new Button("Annuler", ev -> dialog.close()), save);
        dialog.open();
    }

    private VerticalLayout buildSectionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new com.vaadin.flow.component.html.Paragraph("Sections de laboratoire - à implémenter"));
        return layout;
    }

    private VerticalLayout buildSubSectionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new com.vaadin.flow.component.html.Paragraph("Sous-sections - à implémenter"));
        return layout;
    }

    private VerticalLayout buildMethodsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new com.vaadin.flow.component.html.Paragraph("Méthodes analytiques - à implémenter"));
        return layout;
    }

    private VerticalLayout buildSpecimensTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new com.vaadin.flow.component.html.Paragraph("Types de spécimens - à implémenter"));
        return layout;
    }

    private VerticalLayout buildContainersTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new com.vaadin.flow.component.html.Paragraph("Types de conteneurs - à implémenter"));
        return layout;
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
