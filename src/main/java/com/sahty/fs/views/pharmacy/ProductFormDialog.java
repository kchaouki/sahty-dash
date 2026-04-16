package com.sahty.fs.views.pharmacy;

import com.sahty.fs.entity.pharmacy.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

public class ProductFormDialog extends Dialog {

    private final TextField name = new TextField("Nom du Produit");
    private final ComboBox<Product.ProductType> productType = new ComboBox<>("Type");
    private final TextField dciName = new TextField("DCI");
    private final TextField atcCode = new TextField("Code ATC");
    private final TextField form = new TextField("Forme Pharmaceutique");
    private final TextField dosage = new TextField("Dosage");
    private final TextField dosageUnit = new TextField("Unité de Dosage");
    private final TextField packagingUnit = new TextField("Unité de Conditionnement");
    private final IntegerField unitsPerPack = new IntegerField("Unités/Conditionnement");
    private final TextField therapeuticClass = new TextField("Classe Thérapeutique");
    private final TextArea description = new TextArea("Description");

    private final Product product;
    private final Consumer<Product> onSave;

    public ProductFormDialog(Product existingProduct, Consumer<Product> onSave) {
        this.product = existingProduct != null ? existingProduct : new Product();
        this.onSave = onSave;

        setHeaderTitle(product.getId() == null ? "Nouveau Produit" : "Modifier Produit");
        setWidth("700px");

        // Configure
        name.setRequired(true);
        productType.setItems(Product.ProductType.values());
        productType.setItemLabelGenerator(t -> switch(t) {
            case DRUG -> "Médicament";
            case CONSUMABLE -> "Consommable";
            case DEVICE -> "Dispositif Médical";
            case REAGENT -> "Réactif";
        });
        productType.setRequired(true);
        description.setMinHeight("80px");

        FormLayout formLayout = new FormLayout();
        formLayout.add(name, 2);
        formLayout.add(productType, dciName, atcCode, form, dosage, dosageUnit, packagingUnit,
                unitsPerPack, therapeuticClass);
        formLayout.add(description, 2);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        bindData();
        add(formLayout);

        Button cancelBtn = new Button("Annuler", e -> close());
        Button saveBtn = new Button("Enregistrer", e -> save());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancelBtn, saveBtn);
    }

    private void bindData() {
        name.setValue(nvl(product.getName()));
        if (product.getProductType() != null) productType.setValue(product.getProductType());
        dciName.setValue(nvl(product.getDciName()));
        atcCode.setValue(nvl(product.getAtcCode()));
        form.setValue(nvl(product.getForm()));
        dosage.setValue(nvl(product.getDosage()));
        dosageUnit.setValue(nvl(product.getDosageUnit()));
        packagingUnit.setValue(nvl(product.getPackagingUnit()));
        if (product.getUnitsPerPack() != null) unitsPerPack.setValue(product.getUnitsPerPack());
        therapeuticClass.setValue(nvl(product.getTherapeuticClass()));
        description.setValue(nvl(product.getDescription()));
    }

    private void save() {
        if (name.isEmpty() || productType.isEmpty()) {
            name.setInvalid(name.isEmpty());
            productType.setInvalid(productType.isEmpty());
            return;
        }
        product.setName(name.getValue().trim());
        product.setProductType(productType.getValue());
        product.setDciName(dciName.getValue());
        product.setAtcCode(atcCode.getValue());
        product.setForm(form.getValue());
        product.setDosage(dosage.getValue());
        product.setDosageUnit(dosageUnit.getValue());
        product.setPackagingUnit(packagingUnit.getValue());
        product.setUnitsPerPack(unitsPerPack.getValue());
        product.setTherapeuticClass(therapeuticClass.getValue());
        product.setDescription(description.getValue());
        product.setActive(true);
        onSave.accept(product);
        close();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
