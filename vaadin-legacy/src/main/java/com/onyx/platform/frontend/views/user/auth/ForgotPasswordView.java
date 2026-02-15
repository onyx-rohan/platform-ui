package com.onyx.platform.frontend.views.user.auth;

import com.onyx.platform.backend.models.User;
import com.onyx.platform.backend.services.AuthService;
import com.onyx.platform.backend.services.EmailService;
import com.onyx.platform.backend.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Allows functionality for user to request a link to reset their password
 */
@Slf4j
@PermitAll
@Route(value = "forgot-password")
@PageTitle("Forgot Password | Onyx Softworks")
public class ForgotPasswordView extends VerticalLayout {
    private final @NotNull AuthService authService;
    private final @NotNull UserService userService;
    private final @NotNull EmailService emailService;

    private boolean submittedEmail = false;

    @Inject
    public ForgotPasswordView(@NotNull AuthService authService,
                              @NotNull UserService userService,
                              @NotNull EmailService emailService) {
        this.authService = authService;
        this.userService = userService;
        this.emailService = emailService;

        addHeaderContent();
        addBodyContent();
        addFooterContent();

        this.setSizeFull();
        this.setAlignItems(Alignment.CENTER);
        this.setJustifyContentMode(JustifyContentMode.CENTER);
    }

    private void addHeaderContent() {

    }

    private void addBodyContent() {
        final @NotNull EmailField email = new EmailField();
        email.setPlaceholder("Email Address");
        email.setTooltipText("Enter the email address connected to the password you forgot. We will send it a link to reset " +
                "your account's password");

        final @NotNull Button submit = new Button("Submit", new Icon(VaadinIcon.MAILBOX), event -> {
            if(submittedEmail) {
                this.getUI()
                        .flatMap(ui -> ui.navigate(LoginView.class))
                        .ifPresent(LoginView::toggleOverlay);
            } else {
                final @NotNull Optional<User> optUser = userService.get(email.getValue());

                if (optUser.isPresent()) {
                    final @NotNull Icon confirmedEmailIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
                    confirmedEmailIcon.setColor("green");
                    event.getSource().setIcon(confirmedEmailIcon);

                    final @NotNull User user = optUser.get();
                    OTPValidationView.sendOTPCode(user, "Reset Password", authService, userService, emailService);
                    this.getUI().map(ui -> ui.navigate(OTPValidationView.class));
                } else {
                    final @NotNull Icon invalidEmailIcon = new Icon(VaadinIcon.CLOSE_CIRCLE);
                    invalidEmailIcon.setColor("red");
                    event.getSource().setIcon(invalidEmailIcon);
                    @NotNull Notification notification = Notification.show("Invalid Email Address entered", 3000, Notification.Position.BOTTOM_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

                submittedEmail = true;
                event.getSource().setText("Go Back");
                event.getSource().setIcon(new Icon(VaadinIcon.ARROW_CIRCLE_LEFT));
            }
        });

        this.add(new VerticalLayout(email, submit));
    }

    private void addFooterContent() {

    }
}
