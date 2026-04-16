package com.sahty.fs.views.pharmacy;

import com.sahty.fs.entity.pharmacy.Product;
import com.sahty.fs.entity.pharmacy.StockItem;
import com.sahty.fs.entity.pharmacy.StockLocation;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.pharmacy.PharmacyService;
import com.sahty.fs.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "pharmacy", layout = MainLayout.class)
@PageTitle("Pharmacie | Sahty EMR")
@PermitAll
public class PharmacyDashboardView extends VerticalLayout {

    private final PharmacyService pharmacyService;
    private String tenantId;

    public PharmacyDashboardView(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Module Pharmacie");

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add(new Tab("Inventaire"), buildInventoryTab());
        tabs.add(new Tab("Catalogue Produits"), buildCatalogTab());
        tabs.add(new Tab("Entrées de Stock"), buildStockEntryTab());
        tabs.add(new Tab("Sorties / Dispensation"), buildDispensationTab());
        tabs.add(new Tab("Emplacements"), buildLocationsTab());
        tabs.add(new Tab("Fournisseurs"), buildSuppliersTab());
        tabs.add(new Tab("Bons de Commande"), buildPurchaseOrderTab());

        add(title, tabs);
    }

    private VerticalLayout buildInventoryTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        H3 sectionTitle = new H3("Stock Actuel");

        Button refreshBtn = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshBtn.addClickListener(e -> layout.getChildren().forEach(c -> {})); // refresh logic

        HorizontalLayout toolbar = new HorizontalLayout(refreshBtn);

        Grid<Product> grid = new Grid<>(Product.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.addColumn(Product::getName).setHeader("Produit").setFlexGrow(1);
        grid.addColumn(p -> p.getProductType() != null ? p.getProductType().name() : "").setHeader("Type");
        grid.addColumn(Product::getDciName).setHeader("DCI");
        grid.addColumn(p -> pharmacyService.getTotalAvailable(tenantId, p.getId()) + " " + nvl(p.getDosageUnit()))
                .setHeader("Quantité Disponible");
        grid.addColumn(Product::getForm).setHeader("Forme");

        CallbackDataProvider<Product, Void> provider = DataProvider.fromCallbacks(
                q -> pharmacyService.searchProducts(tenantId, "", q.getOffset() / q.getLimit(), q.getLimit()).stream(),
                q -> (int) pharmacyService.searchProducts(tenantId, "", 0, Integer.MAX_VALUE).getTotalElements()
        );
        grid.setDataProvider(provider);
        grid.setSizeFull();

        layout.add(sectionTitle, toolbar, grid);
        return layout;
    }

    private VerticalLayout buildCatalogTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Rechercher un produit...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());

        Button addBtn = new Button("Nouveau Produit", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openProductDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, addBtn);
        toolbar.setWidthFull();
        toolbar.expand(searchField);

        Grid<Product> grid = new Grid<>(Product.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(Product::getName).setHeader("Nom").setFlexGrow(1);
        grid.addColumn(p -> p.getProductType() != null ? p.getProductType().name() : "").setHeader("Type");
        grid.addColumn(Product::getDciName).setHeader("DCI");
        grid.addColumn(Product::getForm).setHeader("Forme");
        grid.addColumn(Product::getDosage).setHeader("Dosage");
        grid.addColumn(p -> {
            Span badge = new Span(p.isActive() ? "Actif" : "Inactif");
            badge.getElement().getThemeList().add("badge");
            if (p.isActive()) badge.getElement().getThemeList().add("success");
            else badge.getElement().getThemeList().add("error");
            return badge;
        }).setHeader("Statut");

        searchField.addValueChangeListener(e -> {
            CallbackDataProvider<Product, Void> provider = DataProvider.fromCallbacks(
                    q -> pharmacyService.searchProducts(tenantId, e.getValue(), q.getOffset() / q.getLimit(), q.getLimit()).stream(),
                    q -> (int) pharmacyService.searchProducts(tenantId, e.getValue(), 0, Integer.MAX_VALUE).getTotalElements()
            );
            grid.setDataProvider(provider);
        });

        CallbackDataProvider<Product, Void> provider = DataProvider.fromCallbacks(
                q -> pharmacyService.searchProducts(tenantId, "", q.getOffset() / q.getLimit(), q.getLimit()).stream(),
                q -> (int) pharmacyService.searchProducts(tenantId, "", 0, Integer.MAX_VALUE).getTotalElements()
        );
        grid.setDataProvider(provider);
        grid.setSizeFull();

        layout.add(toolbar, grid);
        return layout;
    }

    private VerticalLayout buildStockEntryTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.add(new H3("Entrées de Stock"));
        layout.add(new Paragraph("Module d'entrée de stock - Bon de livraison / Réception commande"));
        return layout;
    }

    private VerticalLayout buildDispensationTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.add(new H3("Sorties / Dispensation"));
        layout.add(new Paragraph("Module de dispensation FIFO/FEFO"));
        return layout;
    }

    private VerticalLayout buildLocationsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        Button addBtn = new Button("Nouvel Emplacement", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Grid<StockLocation> grid = new Grid<>(StockLocation.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(StockLocation::getName).setHeader("Nom").setFlexGrow(1);
        grid.addColumn(l -> l.getScope().name()).setHeader("Portée");
        grid.addColumn(l -> l.getType().name()).setHeader("Type");
        grid.addColumn(l -> l.isActive() ? "Actif" : "Inactif").setHeader("Statut");
        grid.setItems(pharmacyService.findLocations(tenantId));
        grid.setSizeFull();

        layout.add(addBtn, grid);
        return layout;
    }

    private VerticalLayout buildSuppliersTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.add(new H3("Gestion des Fournisseurs"));
        layout.add(new Paragraph("Module fournisseurs - à implémenter"));
        return layout;
    }

    private VerticalLayout buildPurchaseOrderTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.add(new H3("Bons de Commande"));
        layout.add(new Paragraph("Module bons de commande - à implémenter"));
        return layout;
    }

    private void openProductDialog(Product product) {
        ProductFormDialog dialog = new ProductFormDialog(product, p -> {
            pharmacyService.saveProduct(p);
        });
        dialog.open();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
