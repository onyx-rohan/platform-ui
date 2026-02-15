package com.onyx.platform.frontend.components;

import com.onyx.platform.backend.entities.CreateNewPassword;
import com.onyx.platform.backend.utils.Utils;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.IntPredicate;

public class CreateNewPasswordField extends CustomField<CreateNewPassword> {
    private final @NotNull PasswordField password = new PasswordField("Password");
    private final @NotNull PasswordField confirmPassword = new PasswordField("Confirm Password");

    private @NotNull Span minCharacterValidation = new Span("Minimum 8 Characters Long");
    private @NotNull Span maxCharacterValidation = new Span("Maximum 32 Characters Long");
    private @NotNull Span atLeastOneUppercaseCharacterValidation = new Span("At least one (1) Uppercase Character");
    private @NotNull Span atLeastOneLowercaseCharacterValidation = new Span("At least one (1) Lowercase Character");
    private @NotNull Span atLeastOneNumericCharacterValidation = new Span("At least one (1) Numeric Character");
    private @NotNull Span atLeastOneSpecialCharacterValidation = new Span("At least one (1) Special Character (#?!@$%^&*-)");
    private @NotNull Span passwordsMatchValidation = new Span("Passwords Match");

    public CreateNewPasswordField() {
        password.setMinLength(8);
        password.setMaxLength(32);
        password.setRevealButtonVisible(true);
        password.setRequiredIndicatorVisible(true);
        password.setAllowedCharPattern(Utils.PASSWORD_REGEX);
        password.addValueChangeListener(event -> {

            // Validation for Password Length being Between 8 - 32 Characters Long
            if(event.getValue().length() < password.getMinLength()) {
                minCharacterValidation = validationPassed(minCharacterValidation);
            } else if(event.getValue().length() > password.getMaxLength()) {
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
            if(event.getValue().equals(password.getValue())) {
                passwordsMatchValidation = validationPassed(passwordsMatchValidation);
            } else {
                passwordsMatchValidation = validationFailed(passwordsMatchValidation);
            }
        });

        @NotNull VerticalLayout passwordValidation = new VerticalLayout();
        passwordValidation.add(
                initializeValidationBadge(minCharacterValidation),
                initializeValidationBadge(maxCharacterValidation),
                initializeValidationBadge(atLeastOneUppercaseCharacterValidation),
                initializeValidationBadge(atLeastOneLowercaseCharacterValidation),
                initializeValidationBadge(atLeastOneNumericCharacterValidation),
                initializeValidationBadge(atLeastOneSpecialCharacterValidation),
                initializeValidationBadge(passwordsMatchValidation)

        );
        passwordValidation.setAlignItems(FlexComponent.Alignment.START);
        passwordValidation.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.FontWeight.EXTRALIGHT,
                LumoUtility.BorderColor.PRIMARY,
                LumoUtility.TextAlignment.LEFT,
                LumoUtility.TextColor.BODY
        );

        final @NotNull HorizontalLayout layout = new HorizontalLayout(password, confirmPassword, passwordValidation);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setWidthFull();
        this.add(layout);
    }

    @Override
    protected CreateNewPassword generateModelValue() {
        return new CreateNewPassword(
                password.getValue(),
                confirmPassword.getValue());
    }

    @Override
    protected void setPresentationValue(@NotNull CreateNewPassword createNewPassword) {
        password.setValue(createNewPassword.getNewPassword());
        confirmPassword.setValue(createNewPassword.getNewPassword());

        minCharacterValidation = new Span("Minimum 8 Characters Long");
        maxCharacterValidation = new Span("Maximum 32 Characters Long");
        atLeastOneUppercaseCharacterValidation = new Span("At least one (1) Uppercase Character");
        atLeastOneLowercaseCharacterValidation = new Span("At least one (1) Lowercase Character");
        atLeastOneNumericCharacterValidation = new Span("At least one (1) Numeric Character");
        atLeastOneSpecialCharacterValidation = new Span("At least one (1) Special Character (#?!@$%^&*-)");
        passwordsMatchValidation = new Span("Passwords Match");
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

    public @NotNull Optional<@NotNull String> validatePassword() {
        if(checkValidations()) {
            return Optional.of(password.getValue());
        } else {
            return Optional.empty();
        }
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
