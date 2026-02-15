package com.onyx.platform.frontend.views.user.auth;

import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.services.AuthService;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Route("login")
@PageTitle("Login | Onyx Softworks")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final @NotNull AuthService authService;

    private final @NotNull LoginForm loginForm = new LoginForm();
    private final @NotNull LoginOverlay loginOverlay = new LoginOverlay();

    @Inject
    public LoginView(@NotNull AuthService authService) {
        AuditLog.info("Moved to login/");
        this.authService = authService;

        initializeLogin();

        this.setSizeFull();
        this.setAlignItems(Alignment.CENTER);
        this.setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }

    public void toggleOverlay() {
        if(loginOverlay.isOpened()) {
            loginOverlay.close();
        } else {
            loginOverlay.setI18n(buildLoginI18n());
            loginOverlay.setOpened(true);
        }
    }

    private void initializeLogin() {
        loginForm.setI18n(buildLoginI18n());
        loginForm.addLoginListener(buildLoginEventListener());
        loginForm.addForgotPasswordListener(buildForgotPasswordEventListener());

        loginOverlay.setI18n(buildLoginI18n());
        loginOverlay.addLoginListener(buildLoginEventListener());
        loginOverlay.addForgotPasswordListener(buildForgotPasswordEventListener());
        loginOverlay.getFooter().add(buildSignUpLinkFooter());
    }

    private @NotNull LoginI18n buildLoginI18n() {
        LoginI18n loginI18n = LoginI18n.createDefault();

        LoginI18n.Header i18nHeader = new LoginI18n.Header();
        i18nHeader.setTitle("Onyx Platform");
        i18nHeader.setDescription("Enter your Username and Password to Login to the Onyx Platform. Or, if you're a " +
                "new user; Sign-Up below");
        loginI18n.setHeader(i18nHeader);

        LoginI18n.Form i18nForm = new LoginI18n.Form();
        i18nForm.setTitle("Login/Signup");
        i18nForm.setUsername("Username/Email");
        i18nForm.setPassword("Password");
        i18nForm.setSubmit("Login");
        i18nForm.setForgotPassword("Forgot password");
        loginI18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = new LoginI18n.ErrorMessage();
        i18nErrorMessage.setTitle("Incorrect Username or Password");
        i18nErrorMessage.setMessage("Check that you have entered the correct username and password and try again. " +
                "Or, if you cannot remember your password; select the \"Forgot Password\" option.");
        loginI18n.setErrorMessage(i18nErrorMessage);

        return loginI18n;
    }

    private @NotNull ComponentEventListener<AbstractLogin.LoginEvent> buildLoginEventListener() {
        return listener -> {
            if(authService.authenticate(listener.getUsername(), listener.getPassword())) {
                UI.getCurrent().getPage().setLocation("/");
            } else {
                listener.getSource().setError(true);
            }
        };
    }

    private @NotNull ComponentEventListener<AbstractLogin.ForgotPasswordEvent> buildForgotPasswordEventListener() {
        return listener -> this.getUI().map(ui -> ui.navigate(ForgotPasswordView.class));
    }

    private @NotNull RouterLink buildSignUpLinkFooter() {
        return new RouterLink("Signup", SignUpView.class);
    }
}
