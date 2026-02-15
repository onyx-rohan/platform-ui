package com.onyx.platform.frontend.components;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.onyx.platform.backend.constants.CaribbeanCountryCodes.CaribbeanCountryCodes;

public class PhoneField extends CustomField<String> {
    private final @NotNull ComboBox<String> countryCode;
    private final @NotNull TextField phoneNumber;

    private final @NotNull String PHONE_REGEX = "((?<country>[+1]?)[-\\s]?(?<area>[0-9]{1,3}?)[-\\s]?(?<prefix>[0-9]{3})[-\\s]?(?<line>[0-9]{4,6}))";

    public PhoneField() {
        countryCode = new ComboBox<>();
        countryCode.setItems(CaribbeanCountryCodes.keySet());

        phoneNumber = new TextField();
        phoneNumber.setMinLength(7);
        phoneNumber.setMaxLength(9);
        phoneNumber.setAllowedCharPattern(PHONE_REGEX);
        phoneNumber.setHelperText("Must be between 7 and 9 characters long");

        final @NotNull HorizontalLayout layout = new HorizontalLayout(this.countryCode, this.phoneNumber);
        layout.setSpacing(false);
        layout.setPadding(false);


        this.add(layout);
    }

    public PhoneField(@NotNull String countryCode, @NotNull String phoneNumber) {
        this();

        this.countryCode.setValue(countryCode);
        this.phoneNumber.setValue(phoneNumber);
    }

    @Override
    protected String generateModelValue() {
        return  "+1 (" + countryCode.getValue() + ") " + phoneNumber.getValue();
    }

    @Override
    protected void setPresentationValue(String phoneNumber) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phoneNumber);
        if (matcher.matches()) {
            this.countryCode.setValue(matcher.group("country") + " " + matcher.group("area"));
            this.phoneNumber.setValue(matcher.group("prefix") + " " + matcher.group("line"));
        }
    }
}
