package com.onyx.platform.frontend.views.business;

import com.onyx.platform.backend.models.*;
import com.onyx.platform.backend.services.BusinessService;
import com.onyx.platform.frontend.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
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
 * List of all Products a Business is Using
 * <ul>
 *     <li>Shows a Business' Product Details: tier, usage, reset cadence, limitation (</li>
 *     <li>Admins can Target a specific Business' Product to Manage</li>
 * </ul>
 */
@Slf4j
@RolesAllowed({ "business" })
@Route(value = "business/:businessId/products", layout = MainLayout.class)
@PageTitle("Business' Products | Onyx Softworks")
public class BusinessProductsView extends VerticalLayout implements BeforeEnterObserver {
	private @NotNull Business business = new Business();

	private final @NotNull BusinessService businessService;

	private final @NotNull Grid<@NotNull ProductUsage> products = new Grid<>(ProductUsage.class);

	@Inject
	public BusinessProductsView(@NotNull BusinessService businessService) {
		AuditLog.info(String.format("Moved to business-products/%s", business.getId()));
		this.businessService = businessService;

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

		this.add(titleSection);
	}

	private void addBodyContent() {
		products.setItems(business.getProducts());
		products.addColumn(buildProductRenderer());

		this.add(products);
	}

	private void addFooterContent() {

	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductUsage> buildProductRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productUsage) -> {
			final @NotNull ProductOffering productOffering = productUsage.getProductOffering();
			final @NotNull Product product = productOffering.getProduct();

			final @NotNull HorizontalLayout title = new HorizontalLayout(
					GENERATE_H3_HEADING(product.getName()),
					GENERATE_TEXT_FIELD_RO(productOffering.getTier().name(), productOffering.getDescription())
			);

			div.add(
					GENERATE_COLUMN(
							title,
							GENERATE_TEXT_FIELD_RO(productUsage.getUsage() + " / " + productUsage.getLimit()),
							GENERATE_TEXT_FIELD_RO(productUsage.getResetCycle().getDescription()),
							GENERATE_PARAGRAPH_FIELD(product.getDescription())
					)
			);
		});
	}
}
