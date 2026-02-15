package com.onyx.platform.frontend.views.business.payout;

import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.Business;
import com.onyx.platform.backend.models.Payout;
import com.onyx.platform.backend.services.BusinessService;
import com.onyx.platform.backend.services.PayoutService;
import com.onyx.platform.frontend.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static com.onyx.platform.frontend.components.ComponentGenerator.*;

/**
 * View a Business' Payouts and Issue a New Payout
 */
@Slf4j
@RolesAllowed({ "business" })
@Route(value = "business/:businessId/payouts", layout = MainLayout.class)
@PageTitle("Payouts | Onyx Softworks")
public class PayoutsView extends VerticalLayout implements BeforeEnterObserver {
	private @NotNull Business business = new Business();

	private final @NotNull BusinessService businessService;
	private final @NotNull PayoutService payoutService;

	private final @NotNull Grid<@NotNull Payout> grid = new Grid<>(Payout.class, false);

	@Inject
	public PayoutsView(@NotNull BusinessService businessService, @NotNull PayoutService payoutService) {
		AuditLog.info(String.format("Moved to business/%s/payouts", business.getId()));
		this.businessService = businessService;
		this.payoutService = payoutService;

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
		titleSection.add(new Button("Make Payout", new Icon(VaadinIcon.MONEY_WITHDRAW), event -> {
			// TODO: Make a Payout utilizing Stripe Payments API from Onyx Account to Business Account
		}));


		this.add(titleSection);
	}

	private void addBodyContent() {
		grid.setItems(payoutService.getByBusiness(business));
		grid.addColumn(buildPayoutRenderer()).setHeader("Payouts");

		this.add(grid);
	}

	private void addFooterContent() {

	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Payout> buildPayoutRenderer() {
		return new ComponentRenderer<>(Div::new, (div, payout) -> {
			final @NotNull DatePicker date = new DatePicker();
			date.setReadOnly(true);
			date.setValue(payout.getDate());
			date.getStyle().remove("--vaadin-input-field-border-width");

			div.add(
					GENERATE_COLUMN(
							GENERATE_DECIMAL_FIELD(payout.getAmount(), payout.getCurrency()),
							GENERATE_DATE_FIELD(payout.getDate())
					)
			);
		});
	}
}
