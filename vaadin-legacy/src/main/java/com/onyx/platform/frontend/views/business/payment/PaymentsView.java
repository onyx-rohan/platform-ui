package com.onyx.platform.frontend.views.business.payment;

import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.Business;
import com.onyx.platform.backend.models.Payment;
import com.onyx.platform.backend.services.BusinessService;
import com.onyx.platform.backend.services.PaymentService;
import com.onyx.platform.frontend.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static com.onyx.platform.frontend.components.ComponentGenerator.*;

/**
 * View a Business' Payments and to Make a New Payment
 */
@Slf4j
@RolesAllowed({ "business" })
@Route(value = "business/:businessId/payments", layout = MainLayout.class)
@PageTitle("Payments | Onyx Softworks")
public class PaymentsView extends VerticalLayout implements BeforeEnterObserver {
	private @NotNull Business business = new Business();

	private final @NotNull BusinessService businessService;
	private final @NotNull PaymentService paymentService;

	private final @NotNull Grid<@NotNull Payment> grid = new Grid<>(Payment.class, false);

	@Inject
	public PaymentsView(@NotNull BusinessService businessService, @NotNull PaymentService paymentService) {
		AuditLog.info(String.format("Moved to business/%s/payments", business.getId()));
		this.businessService = businessService;
		this.paymentService = paymentService;

		addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void beforeEnter(@NotNull BeforeEnterEvent event) {
		if(event.getRouteParameters().get("businessId").isPresent()) {
			final @NotNull Long businessId = Long.valueOf(event.getRouteParameters().get("businessId").get());
			final @NotNull Optional<@NotNull Business> optional = businessService.getIfAvailable(businessId);
			if (optional.isPresent()) {
				business = optional.get();
			} else {
				event.rerouteToError(
						IllegalArgumentException.class,
						this.getTranslation("business.id.not.found", event.getLocation().getPath())
				);
			}
		}
	}

	private void addHeaderContent() {
		final @NotNull HorizontalLayout titleSection = new HorizontalLayout();
		titleSection.add(new H2(business.getName()));
		titleSection.add(new Button("Pay Subscription", new Icon(VaadinIcon.DOLLAR), event -> {
			// TODO: Stripe Checkout Flow for Making a Payment towards the Subscription (Do Not Save New Stripe Token)
		}));


		this.add(titleSection);
	}

	private void addBodyContent() {
		grid.setItems(paymentService.getByBusiness(business));
		grid.addColumn(buildPaymentRenderer()).setHeader("Payments");

		this.add(grid);
	}

	private void addFooterContent() {

	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Payment> buildPaymentRenderer() {
		return new ComponentRenderer<>(Div::new, (div, payment) -> {
			final @NotNull HorizontalLayout row = new HorizontalLayout();
			row.setAlignItems(FlexComponent.Alignment.CENTER);

			@NotNull Icon paymentMethod = new Icon(VaadinIcon.CREDIT_CARD);
			if(!"CARD".equals(payment.getPaymentMethod().getMethod())) {
				paymentMethod = new Icon(VaadinIcon.CASH);
			}

			row.add(paymentMethod,
					GENERATE_COLUMN(
							GENERATE_DECIMAL_FIELD_RO(payment.getAmount(), Currency.getInstance(Locale.US)),
							GENERATE_TAX_FIELD(payment.getAmount(), payment.getTaxPercentage()),
							GENERATE_DATE_FIELD(payment.getDate())
					)
			);
			row.getStyle().set("line-height", "var(--lumo-line-height-m)");

			div.add(row);
		});
	}
}
