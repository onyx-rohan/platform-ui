package com.onyx.platform.frontend.components;

import com.onyx.platform.backend.entities.TaxAmount;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public class TaxField extends CustomField<TaxAmount> {
	private final @NotNull BigDecimalField amount;
	private final @NotNull TextField percentage;
	private final @NotNull Span currencySymbol;
	private final @NotNull Span currencyCode;
	private final @NotNull Span label;

	public TaxField() {
		currencySymbol = new Span();
		currencyCode = new Span();
		label = new Span("Tax:");
		label.addClassName(LumoUtility.Margin.Right.XLARGE);

		amount = new BigDecimalField();
		amount.setReadOnly(true);
		amount.setPrefixComponent(currencySymbol);
		amount.setSuffixComponent(currencyCode);
		amount.getStyle().remove("--vaadin-input-field-border-width");

		percentage = new TextField();
		percentage.setReadOnly(true);
		percentage.setPrefixComponent(new Span("("));
		percentage.setSuffixComponent(new Span(")"));
		percentage.getStyle().remove("--vaadin-input-field-border-width");

		final @NotNull HorizontalLayout layout = new HorizontalLayout(label, amount, percentage);
		layout.setSpacing(false);
		layout.setPadding(false);

		this.add(layout);
	}

	public TaxField(@NotNull BigDecimal grossAmount,
					@NotNull BigDecimal taxPercentage) {
		this();

		final @NotNull BigDecimal taxPercentageAsDecimal = taxPercentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR);
		final @NotNull BigDecimal taxAmount = grossAmount.multiply(taxPercentageAsDecimal);

		amount.setValue(taxAmount);
		percentage.setValue(taxPercentage.toPlainString() + "%");
	}

	@Override
	protected TaxAmount generateModelValue() {
		return new TaxAmount(
			amount.getValue().setScale(2, RoundingMode.FLOOR),
			BigDecimal.valueOf(Double.parseDouble(percentage.getValue().replace("%", ""))),
			Currency.getInstance(currencySymbol.getText())
		);
	}

	@Override
	protected void setPresentationValue(@NotNull TaxAmount taxAmount) {
		amount.setValue(taxAmount.getTaxAmount().setScale(2, RoundingMode.FLOOR));
		percentage.setValue(taxAmount.getTaxPercentage().toPlainString() + "%");
		currencySymbol.setText(taxAmount.getCurrency().getSymbol(Locale.ROOT));
		currencyCode.setText(taxAmount.getCurrency().getCurrencyCode());
	}
}
