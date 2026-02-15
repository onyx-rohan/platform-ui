package com.onyx.platform.frontend.views.user.auth;

import com.onyx.platform.backend.models.User;
import com.onyx.platform.backend.services.AuthService;
import com.onyx.platform.backend.services.UserService;
import com.onyx.platform.backend.utils.Utils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntPredicate;


/**
 * Allows functionality for user to request a link to reset their password
 */
@Slf4j
@PermitAll
@Route(value = "change-password")
@PageTitle("Forgot Password | Onyx Softworks")
public class ChangePasswordView extends VerticalLayout {
    private @NotNull User user = new User();

    private final @NotNull AuthService authService;

    private final @NotNull PasswordField newPassword = new PasswordField();
    private final @NotNull PasswordField confirmPassword = new PasswordField();

    private final @NotNull Button changePassword = new Button("Change Password");

    private final @NotNull VerticalLayout passwordValidation = new VerticalLayout();

    private @NotNull Span minCharacterValidation = new Span("Minimum 8 Characters Long");
    private @NotNull Span maxCharacterValidation = new Span("Maximum 32 Characters Long");
    private @NotNull Span atLeastOneUppercaseCharacterValidation = new Span("At least one (1) Uppercase Character");
    private @NotNull Span atLeastOneLowercaseCharacterValidation = new Span("At least one (1) Lowercase Character");
    private @NotNull Span atLeastOneNumericCharacterValidation = new Span("At least one (1) Numeric Character");
    private @NotNull Span atLeastOneSpecialCharacterValidation = new Span("At least one (1) Special Character (#?!@$%^&*-)");
    private @NotNull Span passwordsMatchValidation = new Span("Passwords Match");

    @Inject
    public ChangePasswordView(@NotNull AuthService authService) {
        this.authService = authService;

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
        newPassword.setMinLength(8);
        newPassword.setMaxLength(32);
        newPassword.setRevealButtonVisible(true);
        newPassword.setRequiredIndicatorVisible(true);
        newPassword.setAllowedCharPattern(Utils.PASSWORD_REGEX);
        newPassword.addValueChangeListener(event -> {

            // Validation for Password Length being Between 8 - 32 Characters Long
            if(event.getValue().length() < newPassword.getMinLength()) {
                minCharacterValidation = validationPassed(minCharacterValidation);
            } else if(event.getValue().length() > newPassword.getMaxLength()) {
                maxCharacterValidation = validationFailed(maxCharacterValidation);
            }

            // Validation for At Least One Lowercase Letter
            if(containsLowerCase(event.getValue())) {
                atLeastOneLowercaseCharacterValidation = validationPassed(atLeastOneLowercaseCharacterValidation);
            } else {
                atLeastOneLowercaseCharacterValidation = validationFailed(atLeastOneLowercaseCharacterValidation);
            }

            // Validation for At Least One Uppercase Letter
            if(containsUpperCase(event.getValue())) {
                atLeastOneUppercaseCharacterValidation = validationPassed(atLeastOneUppercaseCharacterValidation);
            } else {
                atLeastOneUppercaseCharacterValidation = validationFailed(atLeastOneUppercaseCharacterValidation);
            }

            // Validation for At Least One Number
            if(containsNumber(event.getValue())) {
                atLeastOneNumericCharacterValidation = validationPassed(atLeastOneNumericCharacterValidation);
            } else {
                atLeastOneNumericCharacterValidation = validationFailed(atLeastOneNumericCharacterValidation);
            }

            // Validation for At Least One Special Character
            if(containsSpecialCharacter(event.getValue())) {
                atLeastOneSpecialCharacterValidation = validationPassed(atLeastOneSpecialCharacterValidation);
            } else {
                atLeastOneSpecialCharacterValidation = validationFailed(atLeastOneSpecialCharacterValidation);
            }

        });

        confirmPassword.setMinLength(8);
        confirmPassword.setMaxLength(32);
        confirmPassword.setRevealButtonVisible(true);
        confirmPassword.setRequiredIndicatorVisible(true);
        confirmPassword.setAllowedCharPattern(Utils.PASSWORD_REGEX);
        confirmPassword.addValueChangeListener(event -> {

            // Validation for the Passwords Matching
            if(event.getValue().equals(newPassword.getValue())) {
                passwordsMatchValidation = validationPassed(passwordsMatchValidation);
            } else {
                passwordsMatchValidation = validationFailed(passwordsMatchValidation);
            }
        });

        passwordValidation.add(
                initializeValidationBadge(minCharacterValidation),
                initializeValidationBadge(maxCharacterValidation),
                initializeValidationBadge(atLeastOneUppercaseCharacterValidation),
                initializeValidationBadge(atLeastOneLowercaseCharacterValidation),
                initializeValidationBadge(atLeastOneNumericCharacterValidation),
                initializeValidationBadge(atLeastOneSpecialCharacterValidation),
                initializeValidationBadge(passwordsMatchValidation)

        );
        passwordValidation.setAlignItems(Alignment.START);
        passwordValidation.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.FontWeight.EXTRALIGHT,
                LumoUtility.BorderColor.PRIMARY,
                LumoUtility.TextAlignment.LEFT,
                LumoUtility.TextColor.BODY
        );

        final @NotNull FormLayout changePasswordForm = new FormLayout(newPassword, confirmPassword, passwordValidation);
        changePasswordForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        changePasswordForm.setWidthFull();

        changePassword.addClickListener(event -> {
            if(checkValidations()) {
                user = AuthService.getCurrentUser();
                user.setPassword(confirmPassword.getValue());

                User.add(user);
                authService.logout();
            } else {
                @NotNull Notification notification = Notification.show("Please address your Validation Errors first", 3000, Notification.Position.BOTTOM_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        this.setAlignItems(Alignment.CENTER);
        this.add(changePasswordForm, changePassword);
        this.setAlignSelf(Alignment.END, changePassword);
    }

    private void addFooterContent() {

    }

    private @NotNull Span initializeValidationBadge(@NotNull Span badge) {
        final @NotNull Icon icon = VaadinIcon.THIN_SQUARE.create();
        icon.getStyle().set("padding", "var(--lumo-space-s");
        icon.getElement().getThemeList().add("badge contrast");
        badge = new Span(icon, badge);
        return badge;
    }

    private @NotNull Span validationFailed(@NotNull Span badge) {
        final @NotNull Icon icon = VaadinIcon.CLOSE_SMALL.create();
        icon.getStyle().set("padding", "var(--lumo-space-s");
        icon.getElement().getThemeList().add("badge error");
        return new Span(icon, badge);
    }

    private @NotNull Span validationPassed(@NotNull Span badge) {
        final @NotNull Icon icon = VaadinIcon.CHECK_SQUARE_O.create();
        icon.getStyle().set("padding", "var(--lumo-space-s");
        icon.getElement().getThemeList().add("badge success");
        return new Span(icon, badge);
    }

    private boolean checkValidations() {
        return minCharacterValidation.getElement().getThemeList().contains("badge success") &&
                maxCharacterValidation.getElement().getThemeList().contains("badge success") &&
                atLeastOneLowercaseCharacterValidation.getElement().getThemeList().contains("badge success") &&
                atLeastOneUppercaseCharacterValidation.getElement().getThemeList().contains("badge success") &&
                atLeastOneNumericCharacterValidation.getElement().getThemeList().contains("badge success") &&
                atLeastOneSpecialCharacterValidation.getElement().getThemeList().contains("badge success") &&
                passwordsMatchValidation.getElement().getThemeList().contains("badge success");
    }

    private boolean containsLowerCase(final @NotNull String value) {
        return contains(value, ch -> Character.isLetter(ch) && Character.isLowerCase(ch));
    }

    private boolean containsUpperCase(final @NotNull String value) {
        return contains(value, ch -> Character.isLetter(ch) && Character.isUpperCase(ch));
    }

    private boolean containsNumber(final @NotNull String value) {
        return contains(value, Character::isDigit);
    }

    private boolean containsSpecialCharacter(final @NotNull String value) {
        return contains(value, ch -> String.valueOf(ch).matches("[#?!@$%^&*-]"));
    }

    private boolean contains(final @NotNull String value, final @NotNull IntPredicate predicate) {
        return value.chars().anyMatch(predicate);
    }
}
