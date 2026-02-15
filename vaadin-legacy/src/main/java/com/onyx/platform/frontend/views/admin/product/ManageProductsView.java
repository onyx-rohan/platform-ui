package com.onyx.platform.frontend.views.admin.product;

import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.Product;
import com.onyx.platform.backend.services.ProductService;
import com.onyx.platform.frontend.MainLayout;
import com.onyx.platform.frontend.components.ManageEntityLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Page for Admins to Edit, Delete, or Create a Product
 */
@Slf4j
@RolesAllowed({ "admin" })
@Route(value = "manage-products", layout = MainLayout.class)
@PageTitle("Manage Products | Onyx Softworks")
public class ManageProductsView extends ManageEntityLayout<Product> implements HasUrlParameter<Long> {
	private final @NotNull ProductService productService;

	@Inject
	public ManageProductsView(@NotNull ProductService productService) {
		super();

		this.productService = productService;

		addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void setParameter(@NotNull BeforeEvent event, @Nullable @OptionalParameter Long productId) {
		if(productId != null) {
			AuditLog.info(String.format("Moved to manage-products/%s", productId));
			final @NotNull Optional<Product> optional = productService.getWithLock(productId);
			if (optional.isPresent()) {
				final @NotNull Product product = optional.get();
				generateEditor(product);
			}
			else {
				event.rerouteToError(
					IllegalArgumentException.class,
					this.getTranslation("product.id.not.found", event.getLocation().getPath())
				);
			}
		} else {
			AuditLog.info("Moved to manage-products/");
		}
	}

	public void manage(@NotNull Product product) {
		updateRouteParameter(product);
		generateEditor(product);
	}

	private void updateRouteParameter(@NotNull Product product) {
		final @NotNull String deepLinkingUrl = RouteConfiguration.forSessionScope()
			.getUrl(getClass(), product.getId());
		this.getUI().ifPresent(ui -> ui.getPage()
			.getHistory()
			.replaceState(null, deepLinkingUrl));
	}

	private void addHeaderContent() {

	}

	private void addBodyContent() {
		this.add(grid);
	}

	private void addFooterContent() {

	}

	protected void generateGrid() {
		final @NotNull List<@NotNull Product> products = productService.getAll();
		grid.setItems(products);
		grid.addColumn(Product::getId);
		grid.addColumn(Product::getName).setFrozen(true).setHeader("Name");
		grid.addColumn(buildDescriptionRenderer()).setHeader("Description");
		grid.addColumn(buildDeletedRenderer()).setHeader("Is Deleted");
		grid.addColumn(buildActionsRenderer()).setFrozen(true).setHeader("Actions");
	}

	protected @NotNull VerticalLayout generateEditor(@NotNull Product product) {
		final @NotNull TextField name = new TextField();
		name.setWidthFull();
		name.setRequired(true);
		name.setRequiredIndicatorVisible(true);
		name.setPlaceholder("Product Name");
		name.setValue(product.getName());
		binder.forField(name)
			.asRequired("Product is required to have a name")
			.bind(Product::getName, Product::setName);

		final @NotNull TextArea description = new TextArea();
		description.setWidthFull();
		description.setRequired(true);
		description.setRequiredIndicatorVisible(true);
		description.setPlaceholder("Product Description");
		description.setValue(product.getDescription());
		binder.forField(description)
			.asRequired("Product is required to have a description")
			.bind(Product::getDescription, Product::setDescription);


		final @NotNull Button reset = new Button("Reset", event -> binder.refreshFields());
		final @NotNull Button save = new Button("Save", event -> {
			try {
				@NotNull String successMessage = "Successfully Updated the " + product.getName() + " Product";
				if(editor.getHeaderTitle().equals("Add New Product")) {
					successMessage = "Successfully Saved the New Product, " + product.getName();
					productService.create(product);
				} else {
					productService.upsert(product);
				}

				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			} catch (IllegalStateException | EntityExistsException e) {
				@NotNull Notification notification = Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});

		return new VerticalLayout(name, description, new HorizontalLayout(reset, save));
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Product> buildDescriptionRenderer() {
		return new ComponentRenderer<>(Div::new, (div, product) -> {
			final @NotNull TextArea description = new TextArea();
			description.setReadOnly(true);
			description.setMaxLength(100);
			description.setValue(product.getDescription());
			description.getStyle().remove("--vaadin-input-field-border-width");

			div.add(description);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Product> buildDeletedRenderer() {
		return new ComponentRenderer<>(Div::new, (div, product) -> {
			final @NotNull Checkbox isDeleted = new Checkbox();
			isDeleted.setReadOnly(true);
			isDeleted.setValue(product.isDeleted());
			isDeleted.getStyle().remove("--vaadin-input-field-border-width");

			div.add(isDeleted);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Product> buildActionsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, product) -> {
			final @NotNull Button edit = new Button(new Icon(VaadinIcon.EDIT), event -> {
				if(editor.isOpened()) {
					editor.close();
				}

				openEditor(product, "Edit " + product.getName());
			});
			edit.addThemeVariants(ButtonVariant.LUMO_ICON);
			edit.setTooltipText("Edit this Product");

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
					product.setDeleted(true);
					productService.upsert(product);
					confirm.close();

					@NotNull String successMessage = "Successfully (Soft) Deleted the " + product.getName() + " Product";

					@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				});
			});
			softDelete.addThemeVariants(ButtonVariant.LUMO_ICON);
			softDelete.setTooltipText("Delete this Product (Non-Permanent)");

			final @NotNull Button physicalDelete = createPhysicalDeleteButton(product);

			div.add(edit, softDelete, physicalDelete);
		});
	}

	private @NotNull Button createPhysicalDeleteButton(@NotNull Product product) {
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
				productService.delete(product);
				confirm.close();

				@NotNull String successMessage = "Successfully (Hard) Deleted the " + product.getName() + " Product";

				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			});
		});
		physicalDelete.addThemeVariants(ButtonVariant.LUMO_ICON);
		physicalDelete.setTooltipText("Delete this Product (Permanently)");

		return physicalDelete;
	}
}
