package com.onyx.platform.frontend.views.admin.business;

import com.onyx.platform.backend.models.*;
import com.onyx.platform.backend.services.BusinessService;
import com.onyx.platform.backend.services.ProductService;
import com.onyx.platform.frontend.MainLayout;
import com.onyx.platform.frontend.components.ManageEntityLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;

import java.util.Currency;
import java.util.Locale;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static com.onyx.platform.frontend.components.ComponentGenerator.*;

/**
 * Page for Admins to Edit a Business' Products
 */
@Slf4j
@RolesAllowed({ "admin" })
@Route(value = "manage-business/:businessId/products", layout = MainLayout.class)
@PageTitle("Manage Business' Products | Onyx Softworks")
public class ManageBusinessProductsView extends ManageEntityLayout<ProductUsage> implements BeforeEnterObserver {
	private final @NotNull BusinessService businessService;
	private final @NotNull ProductService productService;

	private @NotNull Business business = new Business();

	@Inject
	public ManageBusinessProductsView(@NotNull BusinessService businessService, @NotNull ProductService productService) {
		super();

		AuditLog.info(String.format("Moved to manage-business/%s/products", business.getId()));
		this.businessService = businessService;
		this.productService = productService;

		addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void beforeEnter(@NotNull BeforeEnterEvent event) {
		if(event.getRouteParameters().get("businessId").isPresent()) {
			final @NotNull Long businessId = Long.valueOf(event.getRouteParameters().get("businessId").get());
			final @NotNull Optional<@NotNull Business> optional = businessService.get(businessId);
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

	public void manage(@NotNull Business business, @NotNull ProductUsage productUsage) {
		this.business = business;

		updateRouteParameter();
		generateEditor(productUsage);
	}

	private void updateRouteParameter() {
		final @NotNull String deepLinkingUrl = RouteConfiguration.forSessionScope()
			.getUrl(this.getClass());
		this.getUI().ifPresent(ui -> ui.getPage()
			.getHistory()
			.replaceState(null, deepLinkingUrl));
	}

	private void addHeaderContent() {
		final @NotNull H2 businessTitle = new H2(business.getName());

		final @NotNull Button addProductUsage = new Button(new Icon(VaadinIcon.PLUS), event -> {
			if(editor.isOpened()) {
				editor.close();
			}

			openEditor(new ProductUsage(), "Add New Business Product");
		});
		addProductUsage.addThemeVariants(ButtonVariant.LUMO_ICON);
		addProductUsage.setTooltipText("Add a New Business Product to this Business");

		final @NotNull HorizontalLayout titleSection = new HorizontalLayout(businessTitle, addProductUsage);
		titleSection.getStyle().set("margin-left", "auto");
		titleSection.setWidthFull();

		this.add(titleSection);
	}

	private void addBodyContent() {
		this.add(grid);
	}

	private void addFooterContent() {

	}

	@Override
	protected @NotNull VerticalLayout generateEditor(@NotNull ProductUsage productUsage) {
		final @NotNull ProductOffering productOffering = productUsage.getProductOffering();
		final @NotNull Product product = productOffering.getProduct();

		final @NotNull H3 title = new H3(product.getName() + " Product");

		final @NotNull NumberField usage = GENERATE_NUMBER_FIELD(0.0D,
				productUsage.getLimit().doubleValue(),
				productUsage.getUsage().doubleValue(),
				"Business Product Usage");
		binder.forField(usage)
			.asRequired("Business Product Usage is required to have a value >= 0")
			.bind(pU -> pU.getUsage().doubleValue(), (pU, dbl) -> pU.setUsage(dbl.longValue()));

		final @NotNull NumberField limit = GENERATE_NUMBER_FIELD(0.0D,
				productUsage.getLimit().doubleValue(),
				productUsage.getLimit().doubleValue(),
				"Business Product Limit");
		binder.forField(limit)
			.asRequired("Business Product Limit is required to have a value >= 1")
			.bind(pU -> pU.getLimit().doubleValue(), (pU, dbl) -> pU.setLimit(dbl.longValue()));

		final @NotNull ComboBox<@NotNull Product> productBox = GENERATE_COMBOBOX_FIELD("Business Product",
				productService.getAvailableProducts(),
				product,
				Product::getName);
		binder.forField(productBox)
			.asRequired("Business Product is required")
			.bind(pU -> pU.getProductOffering().getProduct(),
				(pU, p) -> pU.getProductOffering().setProduct(p));

		final @NotNull ComboBox<@NotNull ProductOffering> productOfferingBox = GENERATE_COMBOBOX_FIELD("Business Product Offering",
				productService.getOfferingsByProduct(productBox.getValue()),
				productOffering,
				p0 -> p0.getTier().name());
		binder.forField(productOfferingBox)
			.asRequired("Business Product Offering is required based on the currently selected Product")
			.bind(ProductUsage::getProductOffering, ProductUsage::setProductOffering);

		final @NotNull Button reset = new Button("Reset", event -> binder.refreshFields());
		final @NotNull Button save = new Button("Save", event -> {
			try {
				@NotNull String successMessage = "Successfully Updated the Business' Products";
				if(editor.getHeaderTitle().equals("Add New Business Product")) {
					successMessage = "Successfully Saved the New Business Product";
					AuditLog.info(String.format("%s (Business %s; Product-Usage %s)", successMessage, business.getId(), productUsage.getId()));
					businessService.create(business, productUsage);
				} else {
					AuditLog.info(String.format("%s (%s)", successMessage, productUsage.getId()));
					businessService.upsert(productUsage);
				}

				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			} catch (EntityExistsException e) {
				@NotNull Notification notification = Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});

		return new VerticalLayout(title, usage, limit, productBox, productOfferingBox, new HorizontalLayout(reset, save));
	}

	@Override
	protected void generateGrid() {
		grid.setItems(business.getProducts());
		grid.addColumn(ProductUsage::getId);
		grid.addColumn(productUsage -> productUsage.getProductOffering().getProduct().getName())
				.setFrozen(true).setHeader("Name");
		grid.addColumn(ProductUsage::getUsage).setHeader("Usage");
		grid.addColumn(ProductUsage::getLimit).setHeader("Limit");
		grid.addColumn(buildProductOfferingRenderer()).setHeader("Product");
		grid.addColumn(buildActionsRenderer()).setFrozen(true).setHeader("Actions");
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductUsage> buildProductOfferingRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productUsage) -> {
			final @NotNull ProductOffering productOffering = productUsage.getProductOffering();

			div.add(
					GENERATE_COLUMN(
							new HorizontalLayout(
									GENERATE_H3_HEADING(productOffering.getTier().name()),
									GENERATE_DECIMAL_FIELD_RO(productOffering.getPrice(), Currency.getInstance(Locale.US))
							),
							GENERATE_TEXT_FIELD_RO(productUsage.getResetCycle().getDescription()),
							GENERATE_PARAGRAPH_FIELD(productOffering.getDescription())
					)
			);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductUsage> buildActionsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productUsage) -> {
			final @NotNull String productName = productUsage.getProductOffering().getProduct().getName() +
				", " + productUsage.getProductOffering().getTier() + " Tier";

			final @NotNull Button edit = new Button(new Icon(VaadinIcon.EDIT), event -> {
				if (editor.isOpened()) {
					editor.close();
				}

				openEditor(productUsage, "Edit " + productName);
			});
			edit.addThemeVariants(ButtonVariant.LUMO_ICON);
			edit.setTooltipText("Edit this Business Product for this Business");

			final @NotNull Button physicalDelete = createPhyicalDeleteButton(productUsage, productName);

			div.add(edit, physicalDelete);
		});
	}

	private @NotNull Button createPhyicalDeleteButton(@NotNull ProductUsage productUsage, @NotNull String productName) {
		final @NotNull Icon physicalDeleteIcon = new Icon(VaadinIcon.CLOSE_CIRCLE);
		physicalDeleteIcon.setColor("red");
		final @NotNull Button physicalDelete = new Button(physicalDeleteIcon, event -> {
			final @NotNull ConfirmDialog confirm = new ConfirmDialog();
			confirm.setHeader("Delete Confirmation");
			confirm.setText("Are you sure you wish to delete this Business?\n" +
				"(The Business will no longer exist within the database. This action is NOT reversible)");
			confirm.setCancelable(true);
			confirm.addCancelListener(cancelEvent -> confirm.close());

			confirm.setConfirmText("Delete");
			confirm.addConfirmListener(confirmEvent -> {
				businessService.delete(productUsage);
				confirm.close();

				@NotNull String successMessage = "Successfully Deleted the " + productName + " Product from Business";

				AuditLog.info(String.format("%s (Business %s; Product-Usage %s)", successMessage, business.getId(), productUsage.getId()));
				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			});
		});
		physicalDelete.addThemeVariants(ButtonVariant.LUMO_ICON);
		return physicalDelete;
	}
}
