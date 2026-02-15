package com.onyx.platform.frontend.views.admin.product;

import com.onyx.platform.backend.enums.DueCycle;
import com.onyx.platform.backend.enums.OfferingTier;
import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.PaymentMethod;
import com.onyx.platform.backend.models.Product;
import com.onyx.platform.backend.models.ProductOffering;
import com.onyx.platform.backend.services.ProductService;
import com.onyx.platform.frontend.MainLayout;
import com.onyx.platform.frontend.components.ManageEntityLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Page for Admins to Edit, Delete, or Create a Product Offering
 */
@Slf4j
@RolesAllowed({ "admin" })
@Route(value = "manage-product/:productId/offerings", layout = MainLayout.class)
@PageTitle("Manage Product Offerings | Onyx Softworks")
public class ManageProductOfferingsView extends ManageEntityLayout<@NotNull ProductOffering> implements BeforeEnterObserver {
	private @NotNull Product product = new Product();

	private final @NotNull ProductService productService;

	private final @NotNull Grid<@NotNull ProductOffering> grid = new Grid<>(ProductOffering.class, false);

	@Inject
	public ManageProductOfferingsView(@NotNull ProductService productService) {
		super();

		AuditLog.info(String.format("Moved to manage-product/%s/offerings", product.getId()));
		this.productService = productService;

		addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void beforeEnter(@NotNull BeforeEnterEvent event) {
		if(event.getRouteParameters().get("productId").isPresent()) {
			final @NotNull Long productId = Long.valueOf(event.getRouteParameters().get("productId").get());
			final @NotNull Optional<@NotNull Product> optional = productService.getWithLock(productId);
			if (optional.isPresent()) {
				product = optional.get();
			} else {
				event.rerouteToError(
						IllegalArgumentException.class,
						this.getTranslation("product.id.not.found", event.getLocation().getPath())
				);
			}
		}
	}

	public void manage(@NotNull Product product) {
		this.product = product;
		updateRouteParameter();
	}

	private void updateRouteParameter() {
		final @NotNull String deepLinkingUrl = RouteConfiguration.forSessionScope()
			.getUrl(getClass());
		this.getUI().ifPresent(ui -> ui.getPage()
			.getHistory()
			.replaceState(null, deepLinkingUrl));
	}

	private void addHeaderContent() {
		final @NotNull H2 productTitle = new H2(product.getName());

		final @NotNull Button addProductOfferingBtn = new Button(new Icon(VaadinIcon.PLUS), event -> {
			if(editor.isOpened()) {
				editor.close();
			}

			openEditor(new ProductOffering(), "Add New Product Offering");
		});
		addProductOfferingBtn.addThemeVariants(ButtonVariant.LUMO_ICON);
		addProductOfferingBtn.setTooltipText("Add a new Product Offering to this Product");

		final @NotNull HorizontalLayout titleSection = new HorizontalLayout(productTitle, addProductOfferingBtn);
		titleSection.getStyle().set("margin-left", "auto");
		titleSection.setWidthFull();

		this.add(titleSection);
	}

	private void addBodyContent() {
		this.add(grid);
	}

	private void addFooterContent() {

	}

	protected void generateGrid() {
		grid.setItems(productService.getOfferingsByProduct(product));
		grid.addColumn(ProductOffering::getId).setFrozen(true);
		grid.addColumn(ProductOffering::getTier).setHeader("Tier");
		grid.addColumn(buildDescriptionRenderer()).setHeader("Description");
		grid.addColumn(buildLimitRenderer()).setHeader("Limit");
		grid.addColumn(buildPriceRenderer()).setHeader("Price");
		grid.addColumn(buildPaymentMethodsRenderer()).setHeader("Valid Payment Methods");
		grid.addColumn(productOffering -> productOffering.getResetCycle().getDescription()).setHeader("Reset Cycle");
		grid.addColumn(buildDeletedRenderer()).setHeader("Is Deleted");
		grid.addColumn(buildActionsRenderer()).setFrozen(true).setHeader("Actions");
	}

	protected @NotNull VerticalLayout generateEditor(@NotNull ProductOffering productOffering) {
		productOffering.setProduct(product);

		final @NotNull ComboBox<@NotNull OfferingTier> tier = new ComboBox<>();
		tier.setWidthFull();
		tier.setRequired(true);
		tier.setRequiredIndicatorVisible(true);
		tier.setPlaceholder("Offering Reset Cycle");
		tier.setValue(productOffering.getTier());
		tier.setItems(Arrays.asList(OfferingTier.values()));
		tier.setItemLabelGenerator(OfferingTier::name);
		binder.forField(tier)
			.asRequired("Product Offering is required to have an offering tier")
			.bind(ProductOffering::getTier, ProductOffering::setTier);

		final @NotNull TextArea description = new TextArea();
		description.setWidthFull();
		description.setRequired(true);
		description.setRequiredIndicatorVisible(true);
		description.setPlaceholder("Offering Description");
		description.setValue(productOffering.getDescription());
		binder.forField(description)
			.asRequired("Product Offering is required to have a description")
			.bind(ProductOffering::getDescription, ProductOffering::setDescription);

		final @NotNull ComboBox<DueCycle> cycle = new ComboBox<>();
		cycle.setWidthFull();
		cycle.setRequired(true);
		cycle.setRequiredIndicatorVisible(true);
		cycle.setPlaceholder("Offering Reset Cycle");
		cycle.setValue(productOffering.getResetCycle());
		cycle.setItems(Arrays.asList(DueCycle.values()));
		cycle.setItemLabelGenerator(DueCycle::getDescription);
		binder.forField(cycle)
			.asRequired("Product Offering is required to have a reset cycle")
			.bind(ProductOffering::getResetCycle, ProductOffering::setResetCycle);

		final @NotNull NumberField limit = new NumberField();
		limit.setMin(10.0);
		limit.setStep(1.0);
		limit.setWidthFull();
		limit.setRequired(true);
		limit.setStepButtonsVisible(true);
		limit.setRequiredIndicatorVisible(true);
		limit.setPlaceholder("Offering Volume Limitations");
		limit.setValue(productOffering.getLimit().doubleValue());
		binder.forField(limit)
			.asRequired("Product Offering is required to have a volume limit")
			.bind(pO -> pO.getLimit().doubleValue(), (pO, dbl) -> pO.setLimit(dbl.longValue()));

		final @NotNull BigDecimalField price = new BigDecimalField();
		price.setWidthFull();
		price.setRequired(true);
		price.setRequiredIndicatorVisible(true);
		price.setPlaceholder("Offering Price");
		price.setPrefixComponent(new Span(Currency.getInstance(Locale.US).getSymbol(Locale.ROOT)));
		price.setSuffixComponent(new Span(Currency.getInstance(Locale.US).getCurrencyCode()));
		price.setValue(productOffering.getPrice().setScale(2, RoundingMode.FLOOR));
		binder.forField(price)
			.asRequired("Product Offering is required to have a price")
			.bind(ProductOffering::getPrice, ProductOffering::setPrice);

		final @NotNull MultiSelectComboBox<@NotNull PaymentMethod> paymentMethods = new MultiSelectComboBox<>();
		paymentMethods.setWidthFull();
		paymentMethods.setRequired(true);
		paymentMethods.setRequiredIndicatorVisible(true);
		paymentMethods.setPlaceholder("Offering Valid Payment Methods");
		paymentMethods.setValue(productOffering.getPaymentMethods());
		paymentMethods.setItems(PaymentMethod.listAll());
		paymentMethods.setItemLabelGenerator(PaymentMethod::getMethod);
		binder.forField(paymentMethods)
			.asRequired("Product Offering is required to Payment Methods")
			.bind(ProductOffering::getPaymentMethods, ProductOffering::setPaymentMethods);

		final @NotNull Button reset = new Button("Reset", event -> binder.refreshFields());
		final @NotNull Button save = new Button("Save", event -> {
			try {
				@NotNull String successMessage = "Successfully Updated the " + product.getName() + " Tier Offering";
				if(editor.getHeaderTitle().equals("Add New Tier" + product.getName() + " Tier Offering")) {
					successMessage = "Successfully Saved the New " + product.getName() + " Tier Offering";
					productService.create(productOffering);
				} else {
					productService.upsert(productOffering);
				}

				AuditLog.info(String.format("%s (%s)", successMessage, product.getId()));
				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			} catch (EntityExistsException | IllegalStateException e) {
				@NotNull Notification notification = Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
        });

		return new VerticalLayout(description, cycle, limit, price, paymentMethods,
			new HorizontalLayout(reset, save));
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductOffering> buildDescriptionRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productOffering) -> {
			final @NotNull TextArea description = new TextArea();
			description.setReadOnly(true);
			description.setMaxLength(100);
			description.setValue(productOffering.getDescription());
			description.getStyle().remove("--vaadin-input-field-border-width");

			div.add(description);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductOffering> buildPriceRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productOffering) -> {
			final @NotNull BigDecimalField price = new BigDecimalField();
			price.setReadOnly(true);
			price.setPrefixComponent(new Span(Currency.getInstance(Locale.US).getSymbol(Locale.ROOT)));
			price.setSuffixComponent(new Span(Currency.getInstance(Locale.US).getCurrencyCode()));
			price.setValue(productOffering.getPrice().setScale(2, RoundingMode.FLOOR));
			price.getStyle().remove("--vaadin-input-field-border-width");

			div.add(price);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductOffering> buildPaymentMethodsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productOffering) -> {
			final @NotNull ListBox<@NotNull PaymentMethod> paymentMethods = new ListBox<>();
			paymentMethods.setReadOnly(true);
			paymentMethods.setItems(productOffering.getPaymentMethods());
			paymentMethods.getStyle().remove("--vaadin-input-field-border-width");
			paymentMethods.setRenderer(new ComponentRenderer<Component, PaymentMethod> (paymentMethod -> {
				final @NotNull HorizontalLayout row = new HorizontalLayout();
				row.setAlignItems(FlexComponent.Alignment.CENTER);

				for(final @NotNull PaymentMethod pM: productOffering.getPaymentMethods()) {
					@NotNull Icon icon = new Icon(VaadinIcon.CREDIT_CARD);
					if(!"CARD".equals(pM.getMethod())) {
						icon = new Icon(VaadinIcon.CASH);
					}

					row.add(icon);
				}

				return row;
			}));

			div.add(paymentMethods);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductOffering> buildLimitRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productOffering) -> {
			final @NotNull NumberField limit = new NumberField();
			limit.setReadOnly(true);
			limit.setValue(productOffering.getLimit().doubleValue());
			limit.getStyle().remove("--vaadin-input-field-border-width");

			div.add(limit);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductOffering> buildDeletedRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productOffering) -> {
			final @NotNull Checkbox isDeleted = new Checkbox();
			isDeleted.setReadOnly(true);
			isDeleted.setValue(productOffering.isDeleted());
			isDeleted.getStyle().remove("--vaadin-input-field-border-width");

			div.add(isDeleted);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull ProductOffering> buildActionsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, productOffering) -> {
			final @NotNull Button edit = new Button(new Icon(VaadinIcon.EDIT), event -> {
				if(editor.isOpened()) {
					editor.close();
				}

				openEditor(productOffering, "Edit " + productOffering.getTier() + " Tier Offering");
			});
			edit.addThemeVariants(ButtonVariant.LUMO_ICON);
			edit.setTooltipText("Edit this Product Offering");

			final @NotNull Button softDelete = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE_O), event -> {
				final @NotNull ConfirmDialog confirm = new ConfirmDialog();
				confirm.setHeader("\"Soft\" Delete Confirmation");
				confirm.setText("Are you sure you wish to \"soft\" delete this Product?\n" +
					"(The Product will still exist within the database but will not be searchable by non-admin users "+
					"making this a reversible action)");
				confirm.setCancelable(true);
				confirm.addCancelListener(cancelEvent -> confirm.close());

				confirm.setConfirmText("Delete");
				confirm.addConfirmListener(confirmEvent -> {
					productOffering.setDeleted(true);
					productService.upsert(productOffering);
					confirm.close();

					@NotNull String successMessage = "Successfully (Soft) Deleted the " + productOffering.getTier() + " Tier Offering";

					AuditLog.info(String.format("%s (%s)", successMessage, product.getId()));
					@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				});
			});
			softDelete.addThemeVariants(ButtonVariant.LUMO_ICON);
			softDelete.setTooltipText("Delete this Product Offering (Non-Permanent)");

			final Button physicalDelete = createPhysicalDeleteButton(productOffering);

			div.add(edit, softDelete, physicalDelete);
		});
	}

	private @NotNull Button createPhysicalDeleteButton(@NotNull ProductOffering productOffering) {
		final @NotNull Icon physicalDeleteIcon = new Icon(VaadinIcon.CLOSE_CIRCLE);
		physicalDeleteIcon.setColor("red");
		final @NotNull Button physicalDelete =  new Button(physicalDeleteIcon, event -> {
			final @NotNull ConfirmDialog confirm = new ConfirmDialog();
			confirm.setHeader("\"Physical\" Delete Confirmation");
			confirm.setText("Are you sure you wish to \"physically\" delete this Product?\n" +
				"(The Product will no longer exist within the database. This action is NOT reversible)");
			confirm.setCancelable(true);
			confirm.addCancelListener(cancelEvent -> confirm.close());

			confirm.setConfirmText("Delete");
			confirm.addConfirmListener(confirmEvent -> {
				productService.delete(productOffering);
				confirm.close();

				@NotNull String successMessage = "Successfully (Hard) Deleted the " + productOffering.getTier() + " Tier Offering";

				AuditLog.info(String.format("%s (%s)", successMessage, product.getId()));
				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			});
		});
		physicalDelete.addThemeVariants(ButtonVariant.LUMO_ICON);
		physicalDelete.setTooltipText("Delete this Product-Offering (Permanently)");

		return physicalDelete;
	}
}
