package com.onyx.platform.frontend.views.admin.business;

import com.onyx.platform.backend.enums.DueCycle;
import com.onyx.platform.backend.models.*;
import com.onyx.platform.backend.services.BusinessService;
import com.onyx.platform.backend.services.SubscriptionService;
import com.onyx.platform.frontend.MainLayout;
import com.onyx.platform.frontend.components.ManageEntityLayout;
import com.onyx.platform.frontend.views.business.payout.PayoutsView;
import com.onyx.platform.frontend.views.admin.user.ManageUsersView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.onyx.platform.frontend.components.ComponentGenerator.*;
import static com.onyx.platform.frontend.components.ComponentGenerator.GENERATE_DECIMAL_FIELD_RO;

/**
 * Page for Admins to Edit, Delete, or Create a Business
 */
@Slf4j
@RolesAllowed({ "admin" })
@Route(value = "manage-businesses", layout = MainLayout.class)
@PageTitle("Manage Businesses | Onyx Softworks")
public class ManageBusinessesView extends ManageEntityLayout<Business> implements HasUrlParameter<@NotNull Long> {
	private static final @NotNull Dialog subscriptionManager = new Dialog();
	private static final @NotNull Binder<Subscription> subscriptionBinder = new Binder<>(Subscription.class);

	private final @NotNull BusinessService businessService;
	private final @NotNull SubscriptionService subscriptionService;

	private @NotNull Business business = new Business();

	@Inject
	public ManageBusinessesView(@NotNull BusinessService businessService,
								@NotNull SubscriptionService subscriptionService) {
		super();

		AuditLog.info(String.format("Moved to manage-business/%s", business.getId()));
		this.businessService = businessService;
        this.subscriptionService = subscriptionService;

        addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void setParameter(@NotNull BeforeEvent event, @Nullable @OptionalParameter Long businessId) {
		if(businessId != null) {
			AuditLog.info(String.format("Moved to manage-businesses/%s", businessId));
			final @NotNull Optional<@NotNull Business> optional = businessService.getWithLock(businessId);
			if (optional.isPresent()) {
				business = optional.get();
				generateEditor(business);
			}
			else {
				event.rerouteToError(
					IllegalArgumentException.class,
					this.getTranslation("business.id.not.found", event.getLocation().getPath())
				);
			}
		} else {
			AuditLog.info("Moved to manage-businesses/");
		}
	}

	public void manage(@NotNull Business business) {
		updateRouteParameter(business);
		generateEditor(business);
	}

	private void updateRouteParameter(@NotNull Business business) {
		final @NotNull String deepLinkingUrl = RouteConfiguration.forSessionScope()
			.getUrl(getClass(), business.getId());
		this.getUI().ifPresent(ui -> ui.getPage()
			.getHistory()
			.replaceState(null, deepLinkingUrl));
	}

	private void addHeaderContent() {
		final @NotNull Button addBusinessBtn = new Button(new Icon(VaadinIcon.PLUS), event -> {
			if(editor.isOpened()) {
				editor.close();
			}

			openEditor(new Business(), "Create Business");
		});
		addBusinessBtn.addThemeVariants(ButtonVariant.LUMO_ICON);
		addBusinessBtn.setTooltipText("Creates a new Business");

		final @NotNull HorizontalLayout titleSection = new HorizontalLayout(addBusinessBtn);
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
	protected @NotNull VerticalLayout generateEditor(@NotNull Business business) {
		final @NotNull TextField name = new TextField();
		name.setWidthFull();
		name.setRequired(true);
		name.setRequiredIndicatorVisible(true);
		name.setPlaceholder("Business Name");
		name.setValue(business.getName());
		binder.forField(name)
				.asRequired("Business is required to have a name")
				.bind(Business::getName, Business::setName);

		final @NotNull TextField phone = new TextField();
		phone.setWidthFull();
		phone.setRequired(true);
		phone.setRequiredIndicatorVisible(true);
		phone.setPlaceholder("Business Phone Number");
		phone.setValue(business.getPhone());
		binder.forField(phone)
				.asRequired("Business is required to have a phone number")
				.bind(Business::getPhone, Business::setPhone);

		List<String> countries = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream()
				.map(ctry -> new Locale("ENGLISH", ctry).getDisplayCountry(Locale.ROOT))
				.toList();

		final @NotNull ComboBox<@NotNull String> country = new ComboBox<>();
		country.setWidthFull();
		country.setRequired(true);
		country.setRequiredIndicatorVisible(true);
		country.setPlaceholder("Business Country");
		country.setValue(business.getCountry());
		country.setItems(countries);
		binder.forField(country)
				.asRequired("Business is required to have a country of operations")
				.bind(Business::getCountry, Business::setCountry);

		final @NotNull TextField vatNumber = new TextField();
		vatNumber.setWidthFull();
		vatNumber.setPlaceholder("Business Tax/VAT Number");
		vatNumber.setValue(business.getPhone());
		binder.forField(phone)
				.bind(Business::getVatNumber, Business::setVatNumber);

		final @NotNull ComboBox<@NotNull BusinessType> type = new ComboBox<>();
		type.setWidthFull();
		type.setRequired(true);
		type.setRequiredIndicatorVisible(true);
		type.setPlaceholder("Business Type");
		type.setValue(business.getType());
		type.setItems(businessService.getAllBusinessTypes());
		type.setItemLabelGenerator(BusinessType::getType);
		binder.forField(type)
				.asRequired("Business is required to have a business type")
				.bind(Business::getType, Business::setType);

		final @NotNull Button reset = new Button("Reset", event -> binder.refreshFields());
		final @NotNull Button save = new Button("Save", event -> {
			try {
				@NotNull String notification = "Successfully Updated the " + business.getName() + " Business";
				if(editor.getHeaderTitle().equals("Add New Business")) {
					notification = "Successfully Saved the New Business, " + business.getName();
					businessService.create(business);
				} else {
					businessService.upsert(business);
				}

				AuditLog.info(String.format("%s (%s)", notification, business.getId()));
				Notification.show(notification, 3000, Notification.Position.BOTTOM_CENTER);
			} catch (EntityExistsException e) {
				@NotNull Notification notification = Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});

		return new VerticalLayout(name, phone, vatNumber, type, new HorizontalLayout(reset, save));
	}

	@Override
	protected void generateGrid() {
		grid.setItems(businessService.getAll());
		grid.addColumn(Business::getId);
		grid.addColumn(Business::getName).setFrozen(true).setHeader("Name");
		grid.addColumn(Business::getPhone).setHeader("Phone #");
		grid.addColumn(buildCountryRenderer()).setHeader("Country");
		grid.addColumn(buildBusinessTypeRenderer()).setHeader("Business Type");
		grid.addColumn(Business::getVatNumber).setHeader("Tax/VAT Number");
		grid.addColumn(buildOwnerRenderer()).setHeader("Owner");
		grid.addColumn(buildProductsRenderer()).setHeader("Products");
		grid.addColumn(buildPayoutsRenderer()).setHeader("Payouts");
		grid.addColumn(buildSubscriptionRenderer()).setHeader("Subscription");
		grid.addColumn(Business::getCustomerToken).setHeader("Customer Token");
		grid.addColumn(Business::getCardToken).setHeader("Card Token");
		grid.addColumn(buildDeletedRenderer()).setHeader("Is Deleted");
		grid.addColumn(buildActionsRenderer()).setFrozen(true).setHeader("Actions");
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Business> buildCountryRenderer() {
		return new ComponentRenderer<>(Div::new, (div, business) -> {
			final @NotNull TextField country = new TextField();
			country.setReadOnly(true);
			country.setValue(business.getCountry());
			country.getStyle().remove("--vaadin-input-field-border-width");

			div.add(country);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Business> buildBusinessTypeRenderer() {
		return new ComponentRenderer<>(Div::new, (div, business) -> {
			final @NotNull TextField businessType = new TextField();
			businessType.setReadOnly(true);
			businessType.setValue(business.getType().getType());
			businessType.getStyle().remove("--vaadin-input-field-border-width");

			div.add(businessType);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Business> buildOwnerRenderer() {
		return new ComponentRenderer<>(Div::new, (div, business) -> {
			final @NotNull TextField description = new TextField();
			description.setReadOnly(true);
			description.setValue(business.getOwner().getEmail());
			description.getStyle().remove("--vaadin-input-field-border-width");

			switch(business.getOwner().getStatus()) {
				case ACTIVE:
					div.getElement().setAttribute("theme", "badge success");
				case INACTIVE:
					div.getElement().setAttribute("theme", "badge contrast");
				case DEACTIVATED:
					div.getElement().setAttribute("theme", "badge error");
			}

			div.add(description);
			div.addClickListener(event -> this.getUI()
				.flatMap(ui -> ui.navigate(ManageUsersView.class))
				.ifPresent(manageUsersView -> manageUsersView.manage(business.getOwner())));
			Tooltip.forComponent(div)
				.withText(business.getOwner().getStatus().toString())
				.withPosition(Tooltip.TooltipPosition.TOP_START);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Business> buildProductsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, business) -> {
			final @NotNull ListBox<@NotNull ProductUsage> products = new ListBox<>();
			products.setReadOnly(true);
			products.setItems(business.getProducts());
			products.getStyle().remove("--vaadin-input-field-border-width");
			products.setRenderer(new ComponentRenderer<Component, ProductUsage> (productUsage -> {
				HorizontalLayout row = new HorizontalLayout();
				row.setAlignItems(FlexComponent.Alignment.CENTER);

				final @NotNull Span product = new Span(productUsage.getProductOffering().getProduct().getName());
				product.getStyle().remove("--vaadin-input-field-border-width");
				final @NotNull Span tierOffering = new Span(productUsage.getProductOffering().getTier().toString());
				tierOffering.getStyle().remove("--vaadin-input-field-border-width");
				final @NotNull Span usage = new Span(productUsage.getUsage() + " / " + productUsage.getLimit());
				usage.getStyle().remove("--vaadin-input-field-border-width");
				final @NotNull Span resetCycle = new Span(productUsage.getResetCycle().getDescription());
				resetCycle.getStyle().remove("--vaadin-input-field-border-width");

				final @NotNull VerticalLayout column = new VerticalLayout(product, tierOffering, usage, resetCycle);
				column.setPadding(false);
				column.setSpacing(false);

				row.add(column);
				row.getStyle().set("line-height", "var(--lumo-line-height-m)");
				row.addClickListener(event -> this.getUI()
					.flatMap(ui -> ui.navigate(ManageBusinessProductsView.class))
					.ifPresent(manageBusinessProductsView -> manageBusinessProductsView.manage(business, productUsage)));

				return row;
			}));

			div.add(products);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Business> buildPayoutsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, business) -> {
			final @NotNull ListBox<@NotNull Payout> payouts = new ListBox<>();
			payouts.setReadOnly(true);
			payouts.setItems(business.getPayouts());
			payouts.getStyle().remove("--vaadin-input-field-border-width");
			payouts.setRenderer(new ComponentRenderer<Component, Payout> (payout -> {
				final @NotNull HorizontalLayout row = new HorizontalLayout();
				row.setAlignItems(FlexComponent.Alignment.CENTER);
				row.add(
						GENERATE_COLUMN(
								GENERATE_DECIMAL_FIELD(payout.getAmount(), payout.getCurrency()),
								GENERATE_DATE_FIELD(payout.getDate())
						)
				);
				row.getStyle().set("line-height", "var(--lumo-line-height-m)");
				row.addClickListener(event -> this.getUI().map(ui -> ui.navigate(PayoutsView.class)));

				return row;
			}));

			div.add(payouts);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Business> buildSubscriptionRenderer() {
		return new ComponentRenderer<>(Div::new, (div, business) -> {
			final @Nullable Subscription subscription = business.getSubscription();

			switch (Objects.requireNonNull(subscription).getStatus()) {
			case ACTIVE:
				div.getElement().setAttribute("theme", "badge success");
			case DEACTIVATED:
				div.getElement().setAttribute("theme", "badge error");
			case GRACE_PERIOD:
				div.getElement().setAttribute("theme", "badge contrast");
			case PENDING_PAYMENT:
				div.getElement().setAttribute("theme", "badge");
			}

			final @NotNull BigDecimalField totalPrice = new BigDecimalField();
			totalPrice.setReadOnly(true);
			totalPrice.setValue(subscription.getTotalPrice().setScale(2, RoundingMode.FLOOR));
			totalPrice.setPrefixComponent(new Span(Currency.getInstance(Locale.US).getSymbol(Locale.ROOT)));
			totalPrice.setSuffixComponent(new Span(Currency.getInstance(Locale.US).getCurrencyCode()));
			totalPrice.getStyle().remove("--vaadin-input-field-border-width");

			final @NotNull TextField cycle = new TextField();
			cycle.setReadOnly(true);
			cycle.setValue(subscription.getDueCycle().getDescription());
			cycle.getStyle().remove("--vaadin-input-field-border-width");

			final @NotNull DatePicker date = new DatePicker();
			date.setReadOnly(true);
			date.setValue(subscription.getNextDueDate());
			date.getStyle().remove("--vaadin-input-field-border-width");

			div.add(new VerticalLayout(totalPrice, cycle, date));

			div.addClickListener(event -> toggleSubscriptionManager(business, subscription));

			Tooltip.forComponent(div)
				.withText(subscription.getStatus().toString())
				.withPosition(Tooltip.TooltipPosition.TOP_START);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Business> buildDeletedRenderer() {
		return new ComponentRenderer<>(Div::new, (div, business) -> {
			final @NotNull Checkbox isDeleted = new Checkbox();
			isDeleted.setReadOnly(true);
			isDeleted.setValue(business.isDeleted());
			isDeleted.getStyle().remove("--vaadin-input-field-border-width");

			div.add(isDeleted);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull Business> buildActionsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, business) -> {
			final @NotNull Button edit = new Button(new Icon(VaadinIcon.EDIT), event -> {
				if(editor.isOpened()) {
					editor.close();
				}

				openEditor(business, "Edit " + business.getName());
			});
			edit.addThemeVariants(ButtonVariant.LUMO_ICON);
			edit.setTooltipText("Edit this Business");

			final @NotNull Button softDelete = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE_O), event -> {
				final @NotNull ConfirmDialog confirm = new ConfirmDialog();
				confirm.setHeader("\"Soft\" Delete Confirmation");
				confirm.setText("Are you sure you wish to \"soft\" delete this Business?\n" +
					"(The Business will still exist within the database but will not be searchable by non-admin users "+
					"making this a reversible action)");
				confirm.setCancelable(true);
				confirm.addCancelListener(cancelEvent -> confirm.close());

				confirm.setConfirmText("Delete");
				confirm.addConfirmListener(confirmEvent -> {
					business.setDeleted(true);
					businessService.upsert(business);
					confirm.close();

					@NotNull String successMessage = "Successfully (Soft) Deleted the " + business.getName() + " Business";

					AuditLog.info(String.format("%s (%s)", successMessage, business.getId()));
					@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				});
			});
			softDelete.addThemeVariants(ButtonVariant.LUMO_ICON);
			softDelete.setTooltipText("Delete this Business (Non-Permanent)");

			final @NotNull Button physicalDelete = createPhysicalDeleteButton(business);

			div.add(edit, softDelete, physicalDelete);
		});
	}

	private @NotNull Button createPhysicalDeleteButton(@NotNull Business business) {
		final @NotNull Icon physicalDeleteIcon = new Icon(VaadinIcon.CLOSE_CIRCLE);
		physicalDeleteIcon.setColor("red");
		final @NotNull Button physicalDelete =  new Button(physicalDeleteIcon, event -> {
			final @NotNull ConfirmDialog confirm = new ConfirmDialog();
			confirm.setHeader("\"Physical\" Delete Confirmation");
			confirm.setText("Are you sure you wish to \"physically\" delete this Business?\n" +
				"(The Business will no longer exist within the database. This action is NOT reversible)");
			confirm.setCancelable(true);
			confirm.addCancelListener(cancelEvent -> confirm.close());

			confirm.setConfirmText("Delete");
			confirm.addConfirmListener(confirmEvent -> {
				businessService.delete(business);
				confirm.close();

				@NotNull String successMessage = "Successfully (Hard) Deleted the " + business.getName() + " Business";

				AuditLog.info(String.format("%s (%s)", successMessage, business.getId()));
				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			});
		});
		physicalDelete.addThemeVariants(ButtonVariant.LUMO_ICON);
		physicalDelete.setTooltipText("Delete this Business (Permanently)");

		return physicalDelete;
	}

	private void toggleSubscriptionManager(@NotNull Business business, @NotNull Subscription subscription) {
		if(subscriptionManager.isOpened()) {
			subscriptionManager.close();
		} else {
			final @NotNull Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), (e) -> subscriptionManager.close());
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
			closeButton.setTooltipText("Close this Window");

			subscriptionManager.getHeader().add(closeButton);
			subscriptionManager.setHeaderTitle("Subscription Manager");

			final @NotNull TextField status = GENERATE_STATUS_BADGE(subscription);
			status.setValue("Business: " + business.getName());

			final @NotNull BigDecimalField price = GENERATE_DECIMAL_FIELD_RO(subscription.getTotalPrice(), Currency.getInstance(Locale.US));
			subscriptionBinder.forField(price).bind(Subscription::getTotalPrice, Subscription::setTotalPrice);

			final @NotNull DatePicker nextDueDate = GENERATE_DATE_FIELD(subscription.getNextDueDate());
			subscriptionBinder.forField(nextDueDate).bind(Subscription::getNextDueDate, Subscription::setNextDueDate);

			final @NotNull ComboBox<DueCycle> dueCycles = GENERATE_COMBOBOX_FIELD(List.of(DueCycle.values()),
					subscription.getDueCycle(),
					DueCycle::getDescription);
			subscriptionBinder.forField(dueCycles).bind(Subscription::getDueCycle, Subscription::setDueCycle);

			final @NotNull FormLayout editorLayout = new FormLayout();
			editorLayout.add(status);
			editorLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
			editorLayout.addFormItem(price, "Price: ");
			editorLayout.addFormItem(nextDueDate, "Next Due Date: ");
			editorLayout.addFormItem(dueCycles, "Due Cycle: ");

			final @NotNull Button reset = new Button("Reset", event -> subscriptionBinder.refreshFields());
			final @NotNull Button save = new Button("Save", event -> {
				try {
					@NotNull String successMessage = "Successfully Updated " + business.getName() + "'s Subscription";
					subscriptionService.upsert(subscription);

					AuditLog.info(String.format("%s (%s)", successMessage, business.getId()));
					@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (EntityExistsException ignored) {
					// TODO: Handle this exception
				}
			});

			final @NotNull HorizontalLayout buttonLayout = new HorizontalLayout(reset, save);
			final @NotNull VerticalLayout subscriptionLayout = new VerticalLayout(editorLayout, buttonLayout);

			subscriptionManager.add(subscriptionLayout);
			subscriptionManager.open();
		}
	}
}
