package com.sahty.fs.views;

import com.sahty.fs.security.SecurityUtils;
import com.sahty.fs.views.lims.LimsCollectionView;
import com.sahty.fs.views.lims.LimsPatientListView;
import com.sahty.fs.views.lims.LimsReceptionView;
import com.sahty.fs.views.lims.LimsRegistrationView;
import com.sahty.fs.views.patient.*;
import com.sahty.fs.views.pharmacy.PharmacyDashboardView;
import com.sahty.fs.views.settings.SettingsRolesView;
import com.sahty.fs.views.settings.SettingsRoomsView;
import com.sahty.fs.views.settings.SettingsServicesView;
import com.sahty.fs.views.settings.SettingsUsersView;
import com.sahty.fs.views.superadmin.LimsCatalogsView;
import com.sahty.fs.views.superadmin.SuperAdminClientsView;
import com.sahty.fs.views.superadmin.SuperAdminGroupsView;
import com.sahty.fs.views.superadmin.SuperAdminOrganismsView;
import com.sahty.fs.views.superadmin.SuperAdminRolesView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        H1 viewTitle = new H1("Sahty EMR");
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Avatar avatar = new Avatar();
        SecurityUtils.getCurrentUserEntity().ifPresent(u -> avatar.setName(u.getFullName()));

        Button logoutBtn = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
        logoutBtn.addClickListener(e -> authContext.logout());

        HorizontalLayout header = new HorizontalLayout(toggle, viewTitle);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(viewTitle);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        HorizontalLayout userArea = new HorizontalLayout(avatar, logoutBtn);
        userArea.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.add(userArea);

        addToNavbar(true, header);
    }

    private void addDrawerContent() {
        Span appName = new Span("Sahty EMR");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());
        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        // EMR Section
        if (SecurityUtils.hasPermission("emr_patients")) {
            SideNavItem emrSection = new SideNavItem("Dossiers Médicaux");
            emrSection.setPrefixComponent(VaadinIcon.HOSPITAL.create());
            emrSection.addItem(new SideNavItem("Patients", PatientListView.class, VaadinIcon.USERS.create()));
            emrSection.addItem(new SideNavItem("Admissions", AdmissionListView.class, VaadinIcon.BED.create()));
            emrSection.addItem(new SideNavItem("Salle d'Attente", WaitingRoomView.class, VaadinIcon.CLOCK.create()));
            emrSection.addItem(new SideNavItem("Carte des Lits", WardMapView.class, VaadinIcon.GRID_BIG.create()));
            nav.addItem(emrSection);
        }

        // Pharmacy Section
        if (SecurityUtils.hasPermission("ph_dashboard")) {
            SideNavItem pharmSection = new SideNavItem("Pharmacie");
            pharmSection.setPrefixComponent(VaadinIcon.PILL.create());
            pharmSection.addItem(new SideNavItem("Tableau de Bord", PharmacyDashboardView.class, VaadinIcon.DASHBOARD.create()));
            nav.addItem(pharmSection);
        }

        // LIMS Section
        if (SecurityUtils.hasPermission("lims_registration") || SecurityUtils.hasPermission("lims_parametres")) {
            SideNavItem limsSection = new SideNavItem("Laboratoire");
            limsSection.setPrefixComponent(VaadinIcon.FLASK.create());
            if (SecurityUtils.hasPermission("lims_registration")) {
                limsSection.addItem(new SideNavItem("Patients LIMS", LimsPatientListView.class, VaadinIcon.USERS.create()));
                limsSection.addItem(new SideNavItem("Enregistrement", LimsRegistrationView.class, VaadinIcon.PLUS_CIRCLE.create()));
            }
            if (SecurityUtils.hasPermission("lims_collection")) {
                limsSection.addItem(new SideNavItem("Prélèvement", LimsCollectionView.class, VaadinIcon.EYEDROPPER.create()));
            }
            if (SecurityUtils.hasPermission("lims_reception")) {
                limsSection.addItem(new SideNavItem("Réception", LimsReceptionView.class, VaadinIcon.INBOX.create()));
            }
            if (SecurityUtils.hasPermission("lims_parametres")) {
                limsSection.addItem(new SideNavItem("Catalogues", LimsCatalogsView.class, VaadinIcon.BOOK.create()));
            }
            nav.addItem(limsSection);
        }

        // Settings
        if (SecurityUtils.isTenantSuperAdmin()) {
            SideNavItem settingsSection = new SideNavItem("Paramètres");
            settingsSection.setPrefixComponent(VaadinIcon.COG.create());
            settingsSection.addItem(new SideNavItem("Utilisateurs", SettingsUsersView.class, VaadinIcon.USERS.create()));
            settingsSection.addItem(new SideNavItem("Services", SettingsServicesView.class, VaadinIcon.HOSPITAL.create()));
            settingsSection.addItem(new SideNavItem("Chambres & Lits", SettingsRoomsView.class, VaadinIcon.BED.create()));
            settingsSection.addItem(new SideNavItem("Rôles & Permissions", SettingsRolesView.class, VaadinIcon.SHIELD.create()));
            nav.addItem(settingsSection);
        }

        // Super Admin
        if (SecurityUtils.isSuperAdmin()) {
            SideNavItem superAdminSection = new SideNavItem("Super Admin");
            superAdminSection.setPrefixComponent(VaadinIcon.SHIELD.create());
            superAdminSection.addItem(new SideNavItem("Clients", SuperAdminClientsView.class, VaadinIcon.BUILDING.create()));
            superAdminSection.addItem(new SideNavItem("Groupes", SuperAdminGroupsView.class, VaadinIcon.GROUP.create()));
            superAdminSection.addItem(new SideNavItem("Organismes", SuperAdminOrganismsView.class, VaadinIcon.INSTITUTION.create()));
            superAdminSection.addItem(new SideNavItem("Rôles Globaux", SuperAdminRolesView.class, VaadinIcon.KEY.create()));
            superAdminSection.addItem(new SideNavItem("Catalogues LIMS", com.sahty.fs.views.superadmin.LimsCatalogsView.class, VaadinIcon.FLASK.create()));
            nav.addItem(superAdminSection);
        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        SecurityUtils.getCurrentUserEntity().ifPresent(u ->
                layout.add(new Span("v1.0.0 | " + u.getFullName()))
        );
        return layout;
    }
}
