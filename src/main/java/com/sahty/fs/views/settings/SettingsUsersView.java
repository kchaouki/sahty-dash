package com.sahty.fs.views.settings;

import com.sahty.fs.entity.User;
import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.service.UserService;
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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "settings/users", layout = MainLayout.class)
@PageTitle("Utilisateurs | Paramètres")
@RolesAllowed({"ROLE_TENANT_SUPERADMIN", "ROLE_SUPER_ADMIN"})
public class SettingsUsersView extends VerticalLayout {

    private final UserService userService;
    private final String tenantId;
    private Grid<User> grid;

    public SettingsUsersView(UserService userService) {
        this.userService = userService;
        this.tenantId = SecurityUtils.getCurrentTenantId().orElse("");

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Gestion des Utilisateurs");

        Button addBtn = new Button("Nouvel Utilisateur", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openUserDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(addBtn);

        grid = buildGrid();
        grid.setItems(userService.findByTenant(tenantId));
        grid.setSizeFull();

        add(title, toolbar, grid);
    }

    private Grid<User> buildGrid() {
        Grid<User> grid = new Grid<>(User.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(User::getUsername).setHeader("Nom d'utilisateur").setSortable(true);
        grid.addColumn(User::getLastName).setHeader("Nom").setSortable(true);
        grid.addColumn(User::getFirstName).setHeader("Prénom").setSortable(true);
        grid.addColumn(User::getEmail).setHeader("Email");
        grid.addColumn(u -> u.getSystemRole().name()).setHeader("Rôle Système");
        grid.addColumn(new ComponentRenderer<>(user -> {
            Span badge = new Span(user.isActive() ? "Actif" : "Inactif");
            badge.getElement().getThemeList().add("badge");
            if (user.isActive()) badge.getElement().getThemeList().add("success");
            else badge.getElement().getThemeList().add("error");
            return badge;
        })).setHeader("Statut");

        grid.addColumn(new ComponentRenderer<>(user -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openUserDialog(user));

            Button toggleBtn = new Button(user.isActive() ? VaadinIcon.LOCK.create() : VaadinIcon.UNLOCK.create());
            toggleBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            if (!user.isActive()) toggleBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            toggleBtn.addClickListener(e -> {
                if (user.isActive()) {
                    userService.deactivateUser(user.getId());
                }
                grid.setItems(userService.findByTenant(tenantId));
            });

            return new HorizontalLayout(editBtn, toggleBtn);
        })).setHeader("Actions");

        return grid;
    }

    private void openUserDialog(User existingUser) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingUser == null ? "Nouvel Utilisateur" : "Modifier Utilisateur");
        dialog.setWidth("600px");

        User user = existingUser != null ? existingUser : new User();

        TextField username = new TextField("Nom d'utilisateur");
        username.setValue(nvl(user.getUsername()));
        username.setRequired(true);
        username.setReadOnly(existingUser != null);

        TextField firstName = new TextField("Prénom");
        firstName.setValue(nvl(user.getFirstName()));

        TextField lastName = new TextField("Nom");
        lastName.setValue(nvl(user.getLastName()));

        EmailField email = new EmailField("Email");
        email.setValue(nvl(user.getEmail()));

        ComboBox<User.UserRole> role = new ComboBox<>("Rôle");
        role.setItems(User.UserRole.TENANT_USER, User.UserRole.TENANT_SUPERADMIN);
        role.setItemLabelGenerator(r -> switch(r) {
            case TENANT_USER -> "Utilisateur";
            case TENANT_SUPERADMIN -> "Administrateur";
            default -> r.name();
        });
        if (user.getSystemRole() != null) role.setValue(user.getSystemRole());
        else role.setValue(User.UserRole.TENANT_USER);

        PasswordField password = new PasswordField("Mot de passe");
        password.setRequired(existingUser == null);

        FormLayout form = new FormLayout(username, firstName, lastName, email, role, password);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        Button saveBtn = new Button("Enregistrer", e -> {
            user.setUsername(username.getValue().trim());
            user.setFirstName(firstName.getValue().trim());
            user.setLastName(lastName.getValue().trim().toUpperCase());
            user.setEmail(email.getValue());
            user.setSystemRole(role.getValue());

            try {
                if (existingUser == null) {
                    userService.createUser(user, password.getValue(), tenantId);
                } else {
                    userService.updateUser(user);
                    if (!password.isEmpty()) {
                        userService.changePassword(user.getId(), password.getValue());
                    }
                }
                grid.setItems(userService.findByTenant(tenantId));
                dialog.close();
                Notification.show("Utilisateur enregistré", 3000, Notification.Position.BOTTOM_END)
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
