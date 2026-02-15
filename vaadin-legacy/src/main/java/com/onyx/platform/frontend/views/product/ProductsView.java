package com.onyx.platform.frontend.views.product;

import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.Product;
import com.onyx.platform.backend.services.AuthService;
import com.onyx.platform.backend.services.ProductService;
import com.onyx.platform.frontend.MainLayout;
import com.onyx.platform.frontend.views.admin.product.ManageProductsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Detailed view of a Product or the Main Description of all Products
 * <ul>
 *     <li>Showcases Product Description and Offerings</li>
 *     <li>Admins can Target a specific Product to Manage</li>
 * </ul>
 */
@Slf4j
@PermitAll
@AnonymousAllowed
@Route(value = "products", layout = MainLayout.class)
@PageTitle("Products | Onyx Softworks")
public class ProductsView extends VerticalLayout implements HasUrlParameter<Long> {
	private @NotNull Optional<@NotNull Product> product = Optional.empty();

	private final @NotNull ProductService productService;

	private final @NotNull TextField name = new TextField();
	private final @NotNull TextArea description = new TextArea();

	private final @NotNull Binder<@NotNull Product> binder = new Binder<>(Product.class);

	@Inject
	public ProductsView(@NotNull ProductService productService) {
		this.productService = productService;

		addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void setParameter(@NotNull BeforeEvent event, @Nullable @OptionalParameter Long productId) {
		if (productId != null) {
			AuditLog.info(String.format("Moved to products/%s", productId));
			product = productService.getIfAvailable(productId);
			if (product.isPresent()) {
				binder.forField(name).bind(Product::getName, null);
				binder.forField(description).bind(Product::getDescription, null);
				binder.readBean(product.get());
			} else {
				event.rerouteToError(
						IllegalArgumentException.class,
						this.getTranslation("product.id.not.found", event.getLocation().getPath())
				);
			}
		} else {
			AuditLog.info("Moved to products/");
		}
	}

	private void addHeaderContent() {
		if(product.isPresent()) {
			this.add(generateTitleSectionForProduct());
		} else {
			this.add(generateTitleSection());
		}
	}

	private @NotNull HorizontalLayout generateTitleSection() {
		if(AuthService.isAdmin()) {
			final @NotNull Button addProductBtn = new Button("Create New Product", new Icon(VaadinIcon.PLUS), event -> this.getUI()
					.flatMap(ui -> ui.navigate(ManageProductsView.class))
					.ifPresent(manageProductsView -> manageProductsView.manage(new Product()))
			);

			final @NotNull HorizontalLayout titleSection = new HorizontalLayout(addProductBtn);
			titleSection.getStyle().set("margin-left", "auto");
			titleSection.setWidthFull();

			return titleSection;
		}

		return new HorizontalLayout();
	}

	private @NotNull HorizontalLayout generateTitleSectionForProduct() {
		name.setReadOnly(true);
		name.addThemeVariants(TextFieldVariant.MATERIAL_ALWAYS_FLOAT_LABEL);
		name.getStyle().remove("--vaadin-input-field-border-width");

		final @NotNull Button pricing = new Button("Pricing", event -> this.getUI()
			.flatMap(ui -> ui.navigate(ProductOfferingsView.class))
			.ifPresent(ProductOfferingsView::updateRouteParameter));

		final @NotNull HorizontalLayout titleSection = new HorizontalLayout();
		titleSection.add(name, pricing);

		if(AuthService.isAdmin()) {
			product.ifPresent(value -> titleSection.add(
					new Button("Manage Product", new Icon(VaadinIcon.USER_STAR), event -> this.getUI()
							.flatMap(ui -> ui.navigate(ManageProductsView.class))
							.ifPresent(manageProductsView -> {
								manageProductsView.manage(value);
								titleSection.getStyle().set("margin-left", "auto");
							})
					)
			));
		}

		return titleSection;
	}

	private void addBodyContent() {
		if(product.isPresent()) {
			this.add(generateContentSectionForProduct());
		} else {
			this.add(generateContentSection());
		}
	}

	private @NotNull VerticalLayout generateContentSection() {
		final @NotNull Grid<@NotNull Product> grid = new Grid<>(Product.class, false);

		grid.setItems(productService.getAvailableProducts());
		grid.addColumn(buildProductRenderer());

		if(AuthService.isAdmin()) {
			grid.addColumn(buildAdminRenderer());
		}

		final @NotNull VerticalLayout contentSection = new VerticalLayout();
		contentSection.add(grid);

		return contentSection;
	}

	private @NotNull VerticalLayout generateContentSectionForProduct() {
		description.setReadOnly(true);
		description.addThemeVariants(TextAreaVariant.MATERIAL_ALWAYS_FLOAT_LABEL);
		description.getStyle().remove("--vaadin-input-field-border-width");

		final @NotNull VerticalLayout contentSection = new VerticalLayout();
		contentSection.add(description);

		return contentSection;
	}

	private void addFooterContent() {

	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Product> buildProductRenderer() {
		return new ComponentRenderer<>(Div::new, (div, product) -> {
			final @NotNull TextField name = new TextField();
			name.setWidthFull();
			name.setReadOnly(true);
			name.setValue(product.getName());
			name.getStyle().remove("--vaadin-input-field-border-width");

			final @NotNull RouterLink seeDetails = new RouterLink("See more...", ProductsView.class, product.getId());

			final @NotNull TextArea description = new TextArea();
			description.setReadOnly(true);
			description.setMaxLength(100);
			description.setValue(product.getDescription());
			description.getStyle().remove("--vaadin-input-field-border-width");

			div.add(new HorizontalLayout(name, seeDetails), new VerticalLayout(description));
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Product> buildAdminRenderer() {
		return new ComponentRenderer<>(Div::new, (div, product) -> {
			div.add(new Button("Manage Product", new Icon(VaadinIcon.USER_STAR),
					event -> this.getUI()
							.flatMap(ui -> ui.navigate(ManageProductsView.class))
							.ifPresent(manageProductsView -> manageProductsView.manage(product))
			));
		});
	}
}
