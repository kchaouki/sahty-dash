package com.sahty.fs.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("login")
@PageTitle("Connexion | Sahty EMR")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm;

    public LoginView() {
        addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Height.FULL);

        // Configure French login i18n
        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Header header = new LoginI18n.Header();
        header.setTitle("Sahty EMR");
        header.setDescription("Système de Gestion des Dossiers Médicaux Électroniques");
        i18n.setHeader(header);

        LoginI18n.Form form = i18n.getForm();
        form.setTitle("Connexion");
        form.setUsername("Nom d'utilisateur");
        form.setPassword("Mot de passe");
        form.setSubmit("Se connecter");
        form.setForgotPassword("Mot de passe oublié ?");
        i18n.setForm(form);

        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("Identifiants incorrects");
        errorMessage.setMessage("Vérifiez votre nom d'utilisateur et mot de passe, puis réessayez.");
        i18n.setErrorMessage(errorMessage);

        loginForm = new LoginForm();
        loginForm.setI18n(i18n);
        loginForm.setAction("login");

        H1 title = new H1("Sahty EMR");
        title.addClassNames(LumoUtility.Margin.Bottom.LARGE, LumoUtility.FontSize.XXLARGE);

        add(title, loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
