package com.onyx.platform.frontend.components;

import com.onyx.platform.backend.models.Subscription;
import com.onyx.platform.backend.models.User;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import com.vaadin.flow.data.renderer.ComponentRenderer;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class ComponentGenerator {
    public static @NotNull BigDecimalField GENERATE_DECIMAL_FIELD(@NotNull BigDecimal amount,
                                                                  @NotNull Currency currency) {
        final @NotNull BigDecimalField decimalField = new BigDecimalField();
        decimalField.setReadOnly(true);
        decimalField.setValue(amount.setScale(2, RoundingMode.FLOOR));
        decimalField.setPrefixComponent(new Span(currency.getSymbol()));
        decimalField.setSuffixComponent(new Span(currency.getCurrencyCode()));
        decimalField.getStyle().remove("--vaadin-input-field-border-width");

        return decimalField;
    }

    public static @NotNull NumberField GENERATE_NUMBER_FIELD(@NotNull Double min,
                                                             @NotNull Double max,
                                                             @NotNull Double val,
                                                             @NotNull String placeholder) {
        final @NotNull NumberField numberField =  new NumberField();
        numberField.setMin(min);
        numberField.setMax(max);
        numberField.setStep(1.0);
        numberField.setWidthFull();
        numberField.setRequired(true);
        numberField.setStepButtonsVisible(true);
        numberField.setRequiredIndicatorVisible(true);
        numberField.setPlaceholder(placeholder);
        numberField.setValue(val);

        return numberField;
    }

    public static @NotNull TextField GENERATE_TEXT_FIELD(@NotNull String text) {
        final @NotNull TextField textField = new TextField();
        textField.setValue(text);

        return textField;
    }

    public static @NotNull Paragraph GENERATE_PARAGRAPH_FIELD(@NotNull String text) {
        final @NotNull Paragraph paragraph = new Paragraph(text);
        paragraph.getStyle().remove("--vaadin-input-field-border-width");

        return paragraph;
    }

    public static @NotNull TaxField GENERATE_TAX_FIELD(@NotNull BigDecimal amount,
                                                       @NotNull BigDecimal taxPercentage) {
        return new TaxField(amount, taxPercentage);
    }

    public static @NotNull H3 GENERATE_H3_HEADING(@NotNull String text) {
        final @NotNull H3 h3 = new H3(text);
        h3.getStyle().remove("--vaadin-input-field-border-width");

        return h3;
    }

    public static @NotNull DatePicker GENERATE_DATE_FIELD(@NotNull LocalDate date) {
        final @NotNull DatePicker datePicker = new DatePicker();
        datePicker.setReadOnly(true);
        datePicker.setValue(date);
        datePicker.getStyle().remove("--vaadin-input-field-border-width");

        return datePicker;
    }

    public static @NotNull VerticalLayout GENERATE_COLUMN(@NotNull Component... components) {
        final @NotNull VerticalLayout column = new VerticalLayout(components);
        column.setPadding(false);
        column.setSpacing(false);

        return column;
    }

    public static @NotNull <T> ComboBox<T> GENERATE_COMBOBOX_FIELD(@NotNull List<T> items,
                                                                   @NonNull T defaultItem,
                                                                   @NotNull ItemLabelGenerator<T> itemLabelGenerator) {
        final @NotNull ComboBox<@NotNull T> comboBox = new ComboBox<>();
        comboBox.setWidthFull();
        comboBox.setRequired(true);
        comboBox.setRequiredIndicatorVisible(true);
        comboBox.setValue(defaultItem);
        comboBox.setItems(items);
        comboBox.setItemLabelGenerator(itemLabelGenerator);

        return comboBox;
    }

    public static @NotNull <T> ComboBox<T> GENERATE_COMBOBOX_FIELD(@NotNull String placeholder,
                                                                   @NotNull List<T> items,
                                                                   @NonNull T defaultItem,
                                                                   @NotNull ItemLabelGenerator<T> itemLabelGenerator) {
        final @NotNull ComboBox<@NotNull T> comboBox = new ComboBox<>();
        comboBox.setWidthFull();
        comboBox.setRequired(true);
        comboBox.setRequiredIndicatorVisible(true);
        comboBox.setPlaceholder(placeholder);
        comboBox.setValue(defaultItem);
        comboBox.setItems(items);
        comboBox.setItemLabelGenerator(itemLabelGenerator);

        return comboBox;
    }

    public static @NotNull BigDecimalField GENERATE_DECIMAL_FIELD_RO(@NotNull BigDecimal amount,
                                                                     @NotNull Currency currency) {
        final @NotNull BigDecimalField decimalField = GENERATE_DECIMAL_FIELD(amount, currency);
        decimalField.setReadOnly(true);
        decimalField.getStyle().remove("--vaadin-input-field-border-width");

        return decimalField;
    }

    public static @NotNull TextField GENERATE_TEXT_FIELD_RO(@NotNull String text) {
        final @NotNull TextField textField = GENERATE_TEXT_FIELD(text);
        textField.setReadOnly(true);
        textField.getStyle().remove("--vaadin-input-field-border-width");

        return textField;
    }

    public static @NotNull TextField GENERATE_TEXT_FIELD_RO(@NotNull String text, @NotNull String toolTipText) {
        final @NotNull TextField textField = GENERATE_TEXT_FIELD_RO(text);
        textField.setTooltipText(toolTipText);

        return textField;
    }

    public static @NotNull TextField GENERATE_STATUS_BADGE(@NotNull Subscription subscription) {
        final @NotNull TextField status = new TextField();

        @NotNull Icon badge = switch (subscription.getStatus()) {
            case ACTIVE -> {
                status.getElement().getThemeList().add("badge success small");
                yield VaadinIcon.CHECK_CIRCLE_O.create();
            }
            case DEACTIVATED -> {
                status.getElement().getThemeList().add("badge error small");
                status.setTooltipText("To regain access to Services/Products; contact Onyx Softworks Support");
                yield VaadinIcon.LOCK.create();
            }
            case GRACE_PERIOD -> {
                status.getElement().getThemeList().add("badge contrast small");
                status.setTooltipText("To prevent loss of access to Services/Products; make a Payment before "
                        + "your allotted Grace Period expires");
                yield VaadinIcon.EXCLAMATION_CIRCLE_O.create();
            }
            case PENDING_PAYMENT -> {
                status.getElement().getThemeList().add("badge small");
                status.setTooltipText("To regain access to Services/Products; make a Payment "
                        + "for the outstanding Subscription Fee.");
                yield VaadinIcon.CLOCK.create();
            }
        };

        badge.getStyle().set("padding", "var(--lumo-space-xs)");

        status.setReadOnly(true);
        status.setSuffixComponent(badge);
        status.getStyle().remove("--vaadin-input-field-border-width");

        return status;
    }

    public static @NotNull TextField GENERATE_STATUS_BADGE(@NotNull User user) {
        final @NotNull TextField status = new TextField();

        @NotNull Icon badge = switch (user.getStatus()) {
            case ACTIVE -> {
                status.getElement().getThemeList().add("badge success small");
                yield VaadinIcon.CHECK_CIRCLE_O.create();
            }
            case DEACTIVATED -> {
                status.getElement().getThemeList().add("badge error small");
                yield VaadinIcon.LOCK.create();
            }
            case INACTIVE -> {
                status.getElement().getThemeList().add("badge contrast small");
                yield VaadinIcon.EXCLAMATION_CIRCLE_O.create();
            }
            case PENDING_DEACTIVATION -> {
                status.getElement().getThemeList().add("badge small");
                yield VaadinIcon.CLOCK.create();
            }
        };

        badge.getStyle().set("padding", "var(--lumo-space-xs)");

        status.setReadOnly(true);
        status.setSuffixComponent(badge);
        status.getStyle().remove("--vaadin-input-field-border-width");

        return status;
    }
}
