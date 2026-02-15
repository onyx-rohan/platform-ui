package com.onyx.platform.frontend.views.product;

import com.onyx.platform.backend.models.PaymentMethod;
import com.onyx.platform.backend.models.Product;
import com.onyx.platform.backend.models.ProductOffering;
import com.onyx.platform.backend.services.AuthService;
import com.onyx.platform.backend.services.ProductService;
import com.onyx.platform.frontend.MainLayout;
import com.onyx.platform.frontend.views.admin.product.ManageProductOfferingsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Details of a specific Product Offering
 * <ul>
 *     <li>Showcases Valid Payment Methods and Cycle, Description, Pricing, and Offering Limitations</li>
 *     <li>Admins can Target a specific Product Offering to Manage</li>
 * </ul>
 */
@Slf4j
@PermitAll
@AnonymousAllowed
@Route(value = "product/:productId/offerings", layout = MainLayout.class)
@PageTitle("Product Offerings | Onyx Softworks")
public class ProductOfferingsView extends VerticalLayout implements BeforeEnterObserver {
	private @NotNull Product product = new Product();

	private final @NotNull ProductService productService;

	private final @NotNull Grid<@NotNull ProductOffering> grid = new Grid<>(ProductOffering.class, false);

	@Inject
	public ProductOfferingsView(@NotNull ProductService productService) {
		this.productService = productService;

		addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void beforeEnter(@NotNull BeforeEnterEvent event) {
		if(event.getRouteParameters().get("productId").isPresent()) {
			final @NotNull Long productId = Long.valueOf(event.getRouteParameters().get("productId").get());
			final @NotNull Optional<@NotNull Product> optional = productService.getIfAvailable(productId);
			if (optional.isPresent()) {
				product = optional.get();
				final @NotNull List<@NotNull ProductOffering> productOfferings = productService.getOfferingsByProduct(product);
				grid.setItems(productOfferings);
				grid.addColumn(buildLabelsRenderer());
				for (final @NotNull ProductOffering productOffering : productOfferings) {
					grid.addColumn(buildOfferingsRenderer()).setHeader(productOffering.getTier().toString());
				}
			} else {
				event.rerouteToError(
						IllegalArgumentException.class,
						this.getTranslation("product.id.not.found", event.getLocation().getPath())
				);
			}
		}
	}

	public void updateRouteParameter() {
		final @NotNull String deepLinkingUrl = RouteConfiguration.forSessionScope()
			.getUrl(getClass());
		this.getUI().ifPresent(ui -> ui.getPage()
			.getHistory()
			.replaceState(null, deepLinkingUrl));
	}

	private void addHeaderContent() {
		final @NotNull HorizontalLayout titleSection = new HorizontalLayout();
		titleSection.add(new H2(product.getName()));

		if(AuthService.isAdmin()) {
			titleSection.add(new Button("Manage Product Offering",
					new Icon(VaadinIcon.USER_STAR), event -> this.getUI()
					.flatMap(ui -> ui.navigate(ManageProductOfferingsView.class))
					.ifPresent(manageProductOfferingsView -> {
						manageProductOfferingsView.manage(product);
						titleSection.getStyle().set("margin-left", "auto");
					})
			));
		}

		this.add(titleSection);
	}

	private void addBodyContent() {
		this.add(grid);
	}

	private void addFooterContent() {

	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductOffering> buildLabelsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productOffering) -> {
			div.setText("""
				Description
				Cycle
				Limit
				Price
				""");
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductOffering> buildOfferingsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productOffering) -> {
			final @NotNull TextArea description = new TextArea();
			description.setWidthFull();
			description.setReadOnly(true);
			description.setValue(productOffering.getDescription());
			description.getStyle().remove("--vaadin-input-field-border-width");

			final @NotNull TextField cycle = new TextField();
			cycle.setWidthFull();
			cycle.setReadOnly(true);
			cycle.setValue(productOffering.getResetCycle().getDescription());
			cycle.getStyle().remove("--vaadin-input-field-border-width");

			final @NotNull NumberField limit = new NumberField();
			limit.setWidthFull();
			limit.setReadOnly(true);
			limit.setValue(productOffering.getLimit().doubleValue());
			limit.getStyle().remove("--vaadin-input-field-border-width");

			final @NotNull BigDecimalField price = new BigDecimalField();
			price.setWidthFull();
			price.setReadOnly(true);
			price.setPrefixComponent(new Span(Currency.getInstance(Locale.US).getSymbol(Locale.ROOT)));
			price.setSuffixComponent(new Span(Currency.getInstance(Locale.US).getCurrencyCode()));
			price.setValue(productOffering.getPrice().setScale(2, RoundingMode.FLOOR));
			price.getStyle().remove("--vaadin-input-field-border-width");

			final @NotNull HorizontalLayout paymentMethods = new HorizontalLayout();
			paymentMethods.getStyle().set("line-height", "var(--lumo-line-height-m)");
			paymentMethods.setAlignItems(FlexComponent.Alignment.CENTER);
			for(final @NotNull PaymentMethod paymentMethod: productOffering.getPaymentMethods()) {
				@NotNull Icon icon = new Icon(VaadinIcon.CREDIT_CARD);
				if(!"CARD".equals(paymentMethod.getMethod())) {
					icon = new Icon(VaadinIcon.CASH);
				}

				paymentMethods.add(icon);
			}

			div.add(description, cycle, limit, price, paymentMethods);
		});
	}
}
