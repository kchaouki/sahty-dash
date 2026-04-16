package com.sahty.fs.views.patient;

import com.sahty.fs.entity.Patient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

public class PatientFormView extends VerticalLayout {

    private final TextField firstName = new TextField("Prénom");
    private final TextField lastName = new TextField("Nom");
    private final DatePicker dateOfBirth = new DatePicker("Date de Naissance");
    private final ComboBox<Patient.Gender> gender = new ComboBox<>("Sexe");
    private final TextField phone = new TextField("Téléphone");
    private final TextField homePhone = new TextField("Téléphone Domicile");
    private final EmailField email = new EmailField("Email");
    private final TextField address = new TextField("Adresse");
    private final TextField city = new TextField("Ville");
    private final TextField zipCode = new TextField("Code Postal");
    private final TextField country = new TextField("Pays");
    private final TextField nationality = new TextField("Nationalité");
    private final ComboBox<String> maritalStatus = new ComboBox<>("Situation Familiale");
    private final TextField profession = new TextField("Profession");
    private final ComboBox<String> bloodGroup = new ComboBox<>("Groupe Sanguin");
    private final TextField fatherName = new TextField("Nom du Père");
    private final TextField motherName = new TextField("Nom de la Mère");
    private final TextField fatherPhone = new TextField("Téléphone Père");
    private final TextField motherPhone = new TextField("Téléphone Mère");

    private final Button saveButton = new Button("Enregistrer");
    private final Patient patient;
    private final Consumer<Patient> onSave;

    public PatientFormView(Patient existingPatient, Consumer<Patient> onSave) {
        this.patient = existingPatient != null ? existingPatient : new Patient();
        this.onSave = onSave;

        configureFields();
        buildLayout();
        bindData();
    }

    private void configureFields() {
        firstName.setRequired(true);
        lastName.setRequired(true);
        dateOfBirth.setLocale(java.util.Locale.FRANCE);

        gender.setItems(Patient.Gender.values());
        gender.setItemLabelGenerator(g -> g == Patient.Gender.M ? "Masculin" : g == Patient.Gender.F ? "Féminin" : "Autre");

        maritalStatus.setItems("Célibataire", "Marié(e)", "Divorcé(e)", "Veuf/Veuve");
        bloodGroup.setItems("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> save());
    }

    private void buildLayout() {
        TabSheet tabs = new TabSheet();

        // Identity tab
        FormLayout identityForm = new FormLayout();
        identityForm.add(lastName, firstName, dateOfBirth, gender, phone, homePhone, email);
        identityForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        tabs.add(new Tab("Identité"), identityForm);

        // Address tab
        FormLayout addressForm = new FormLayout();
        addressForm.add(address, city, zipCode, country, nationality, profession, maritalStatus, bloodGroup);
        addressForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        tabs.add(new Tab("Adresse & Infos"), addressForm);

        // Family tab
        FormLayout familyForm = new FormLayout();
        familyForm.add(fatherName, fatherPhone, motherName, motherPhone);
        familyForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        tabs.add(new Tab("Famille"), familyForm);

        add(tabs);
        setSizeFull();
        setPadding(false);
    }

    private void bindData() {
        firstName.setValue(nvl(patient.getFirstName()));
        lastName.setValue(nvl(patient.getLastName()));
        if (patient.getDateOfBirth() != null) dateOfBirth.setValue(patient.getDateOfBirth());
        if (patient.getGender() != null) gender.setValue(patient.getGender());
        phone.setValue(nvl(patient.getPhone()));
        homePhone.setValue(nvl(patient.getHomePhone()));
        email.setValue(nvl(patient.getEmail()));
        address.setValue(nvl(patient.getAddress()));
        city.setValue(nvl(patient.getCity()));
        zipCode.setValue(nvl(patient.getZipCode()));
        country.setValue(nvl(patient.getCountry()));
        nationality.setValue(nvl(patient.getNationality()));
        profession.setValue(nvl(patient.getProfession()));
        if (patient.getMaritalStatus() != null) maritalStatus.setValue(patient.getMaritalStatus());
        if (patient.getBloodGroup() != null) bloodGroup.setValue(patient.getBloodGroup());
        fatherName.setValue(nvl(patient.getFatherName()));
        motherName.setValue(nvl(patient.getMotherName()));
        fatherPhone.setValue(nvl(patient.getFatherPhone()));
        motherPhone.setValue(nvl(patient.getMotherPhone()));
    }

    private void save() {
        if (firstName.isEmpty() || lastName.isEmpty()) {
            firstName.setInvalid(true);
            lastName.setInvalid(true);
            return;
        }
        patient.setFirstName(firstName.getValue().trim());
        patient.setLastName(lastName.getValue().trim().toUpperCase());
        patient.setDateOfBirth(dateOfBirth.getValue());
        patient.setGender(gender.getValue());
        patient.setPhone(phone.getValue());
        patient.setHomePhone(homePhone.getValue());
        patient.setEmail(email.getValue());
        patient.setAddress(address.getValue());
        patient.setCity(city.getValue());
        patient.setZipCode(zipCode.getValue());
        patient.setCountry(country.getValue());
        patient.setNationality(nationality.getValue());
        patient.setProfession(profession.getValue());
        patient.setMaritalStatus(maritalStatus.getValue());
        patient.setBloodGroup(bloodGroup.getValue());
        patient.setFatherName(fatherName.getValue());
        patient.setMotherName(motherName.getValue());
        patient.setFatherPhone(fatherPhone.getValue());
        patient.setMotherPhone(motherPhone.getValue());
        onSave.accept(patient);
    }

    public Button getSaveButton() { return saveButton; }

    private String nvl(String s) { return s != null ? s : ""; }
}
