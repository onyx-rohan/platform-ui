package com.onyx.platform.frontend.views.business;

import com.onyx.platform.backend.enums.AccountStatus;
import com.onyx.platform.backend.enums.SubscriptionStatus;
import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.Business;
import com.onyx.platform.backend.models.BusinessType;
import com.onyx.platform.backend.models.Subscription;
import com.onyx.platform.backend.services.AuthService;
import com.onyx.platform.backend.services.BusinessService;
import com.onyx.platform.backend.services.EmailService;
import com.onyx.platform.backend.services.UserService;
import com.onyx.platform.frontend.MainLayout;
import com.onyx.platform.frontend.components.PhoneField;
import com.onyx.platform.frontend.views.user.auth.OTPValidationView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.onyx.platform.backend.constants.Messages.*;
import static com.onyx.platform.frontend.components.ComponentGenerator.GENERATE_STATUS_BADGE;

/**
 * Detailed View of a Business's Information
 * <ul>
 *     <li>Shows a Business' General Info: name, phone #, vat #, country, and business type</li>
 *     <li>Shows a Business' Subscription Info (if applicable): status, cycle, payment date, and price </li>
 *     <li>Functionality to Make a Payment to a Business' Subscription (if applicable)</li>
 *     <li>Functionality to Update a Business' Card on File for Payments</li>
 *     <li>Admins can Target a specific Business (or Business' Subscription) to Manage</li>
 * </ul>
 */
@Slf4j
@RolesAllowed({ "business" })
@Route(value = "business", layout = MainLayout.class)
@PageTitle("Business | Onyx Softworks")
public class BusinessView extends VerticalLayout implements HasUrlParameter<@NotNull Long> {
	private @NotNull Business business = new Business();
	private @Nullable Subscription subscription;

	private @NotNull Button editBusiness = new Button();

	private final @NotNull AuthService authService;
	private final @NotNull UserService userService;
	private final @NotNull EmailService emailService;
	private final @NotNull BusinessService businessService;

	private final @NotNull TextField name = new TextField();
	private final @NotNull PhoneField phone = new PhoneField();
	private final @NotNull TextField vatNumber = new TextField();
	private final @NotNull ComboBox<@NotNull String> country = new ComboBox<>();
	private final @NotNull ComboBox<@NotNull BusinessType> type = new ComboBox<>();

	private @NotNull TextField status = new TextField();

	private final @NotNull BigDecimalField totalPrice = new BigDecimalField();
	private final @NotNull TextField dueCycle = new TextField();
	private final @NotNull DatePicker nextDueDate = new DatePicker();

	private final @NotNull Binder<@NotNull Business> businessBinder = new Binder<>(Business.class);
	private final @NotNull Binder<@NotNull Subscription> subscriptionBinder = new Binder<>(Subscription.class);

	@Inject
	public BusinessView(@NotNull AuthService authService,
						@NotNull UserService userService,
						@NotNull EmailService emailService,
						@NotNull BusinessService businessService) {
		AuditLog.info(String.format("Moved to business/%s", business.getId()));
		this.authService = authService;
		this.userService = userService;
		this.emailService = emailService;
		this.businessService = businessService;

		addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void setParameter(@NotNull BeforeEvent event, @NotNull Long businessId) {
		final @NotNull Optional<@NotNull Business> optional = businessService.getIfAvailable(businessId);
		if(optional.isPresent()) {
			business = optional.get();
			businessBinder.forField(name).bind(Business::getName, Business::setName);
			businessBinder.forField(phone).bind(Business::getPhone, Business::setPhone);
			businessBinder.forField(country).bind(Business::getCountry, Business::setCountry);
			businessBinder.forField(vatNumber).bind(Business::getVatNumber, Business::setVatNumber);
			businessBinder.forField(type).bind(Business::getType, Business::setType);
			businessBinder.setBean(business);

			subscription = business.getSubscription();
			if(subscription != null) {
				subscriptionBinder.forField(status).bind(sub -> sub.getStatus().name(), null);
				subscriptionBinder.forField(totalPrice).bind(
					sub -> sub.getTotalPrice().setScale(2, RoundingMode.FLOOR),
					null);
				subscriptionBinder.forField(dueCycle).bind(sub -> sub.getDueCycle().getDescription(), null);
				subscriptionBinder.forField(nextDueDate).bind(Subscription::getNextDueDate, null);
			}

		} else {
			event.rerouteToError(
				IllegalArgumentException.class,
				this.getTranslation("business.id.not.found", event.getLocation().getPath())
			);
		}
	}

	public void editModeOn() {
		name.setReadOnly(false);
		phone.setReadOnly(false);
		country.setReadOnly(false);
		vatNumber.setReadOnly(false);
		type.setReadOnly(false);

		final @NotNull StreamResource saveIconResource = new StreamResource("floppy-disk.svg",
				() -> getClass().getResourceAsStream("/icons/floppy-disk.svg"));
		final @NotNull SvgIcon saveIcon = new SvgIcon(saveIconResource);

		editBusiness = new Button("Save", event -> {
			businessService.upsert(business);

			final @NotNull String successMessage = "Successfully Edited Business";
			AuditLog.info(String.format("%s (%s)", successMessage, business.getOwner().getId()));
			@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

			initializeEditButton();
		});
		editBusiness.setSuffixComponent(saveIcon);
		editBusiness.addThemeVariants(ButtonVariant.LUMO_ERROR);

		updateRouteParameter(Objects.requireNonNull(AuthService.getCurrentUser().getBusiness()));
	}

	public void updateRouteParameter(@NotNull Business business) {
		final @NotNull String deepLinkingUrl = RouteConfiguration.forSessionScope()
				.getUrl(getClass(), business.getId());
		this.getUI().ifPresent(ui -> ui.getPage()
				.getHistory()
				.replaceState(null, deepLinkingUrl));
	}

	private void addHeaderContent() {
		final @NotNull HorizontalLayout titleSection = new HorizontalLayout();
		titleSection.add(new H2(business.getName()));

		this.add(titleSection);
	}

	private void addBodyContent() {
		this.add(
			generateBusinessInfo(),
			new Hr(),
			generateSubscriptionInfo(),
			new Hr(),
			generateBusinessActions()
		);
	}

	private void addFooterContent() {

	}

	private @NotNull FormLayout generateBusinessInfo() {
		name.setRequired(true);
		name.setAllowedCharPattern("(^(?!!@&_\\-)[!@&_\\-\\sa-zA-Z0-9]+(?<!!@&_\\-)$)");
		name.setHelperText("This is the name of your Business operating in connection to Onyx."
			+ "This must be unique across all Businesses registered with Onyx, 5-25 characters long,"
			+ "and can not consist of #$%^*."
		);

		List<String> countries = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream()
			.map(ctry -> new Locale("ENGLISH", ctry).getDisplayCountry(Locale.ROOT))
			.toList();

		country.setReadOnly(true);
		country.setItems(countries);
		country.setHelperText("The country your Business operates out of. Your banking details and KYC verification "
			+ "is determined by this.");

		vatNumber.setReadOnly(true);
		vatNumber.setAllowedCharPattern("(^(?!\\-)[\\-a-zA-Z0-9]+(?<!\\-)$)");
		vatNumber.setHelperText("The Tax/VAT Number of your Business in its country of operation");

		type.setReadOnly(true);
		type.setItems(businessService.getAllBusinessTypes());
		type.setItemLabelGenerator(BusinessType::getType);
		type.setHelperText("The type of Business you are defined as from the above dropdown. This will help "
			+ "Onyx with identifying and streamlining functionality and products for your specific Business "
			+ "as well as any needed paperwork for your KYC verification or otherwise.");

		final @NotNull FormLayout businessInfo = new FormLayout();
		businessInfo.setWidthFull();
		businessInfo.add(new H2("Business Information"));
		businessInfo.setResponsiveSteps(new ResponsiveStep("0", 1));
		businessInfo.addFormItem(name, "Business:");
		businessInfo.addFormItem(phone, "Contact #:");
		businessInfo.addFormItem(country, "Country:");
		businessInfo.addFormItem(vatNumber, "Tax/VAT Number:");
		businessInfo.addFormItem(type, "Business Type:");

		if(business.getStatus().equals(AccountStatus.DEACTIVATED)) {
			businessInfo.setEnabled(false);
		}

		return businessInfo;
	}

	private @NotNull FormLayout generateSubscriptionInfo() {
		final @NotNull FormLayout subscriptionInfo = new FormLayout();
		subscriptionInfo.setWidthFull();

		final @NotNull HorizontalLayout heading = new HorizontalLayout();
		heading.add(new H2("Subscription"));

		if(subscription != null) {
			status = GENERATE_STATUS_BADGE(subscription);
			heading.add(status);

			totalPrice.setReadOnly(true);
			totalPrice.setPrefixComponent(new Span(Currency.getInstance(Locale.US).getSymbol(Locale.ROOT)));
			totalPrice.setSuffixComponent(new Span(Currency.getInstance(Locale.US).getCurrencyCode()));
			totalPrice.setHelperText("The total price (in USD) for your Business' Subscription-based Products.");
			totalPrice.getStyle().remove("--vaadin-input-field-border-width");

			dueCycle.setReadOnly(true);
			dueCycle.setHelperText("The frequency that your Subscription has to be paid over.");
			dueCycle.getStyle().remove("--vaadin-input-field-border-width");

			nextDueDate.setReadOnly(true);
			nextDueDate.setHelperText("The next date your Subscription is due.");
			nextDueDate.getStyle().remove("--vaadin-input-field-border-width");

			subscriptionInfo.add(heading);
			subscriptionInfo.setResponsiveSteps(new ResponsiveStep("0", 1));
			subscriptionInfo.addFormItem(totalPrice, "Total:");
			subscriptionInfo.addFormItem(dueCycle, "Payment Cycle:");
			subscriptionInfo.addFormItem(nextDueDate, "Next Due Date:");

			if (SubscriptionStatus.GRACE_PERIOD.equals(subscription.getStatus())) {
				final @NotNull LocalDate deadline = nextDueDate.getValue().plus(subscription.getGracePeriod());
				int daysLeft = Math.max(0, deadline.getDayOfYear() - LocalDate.now().getDayOfYear());

				final @NotNull DatePicker gracePeriod = new DatePicker();
				gracePeriod.setReadOnly(true);
				gracePeriod.setValue(deadline);
				gracePeriod.setHelperText("Days Left before Loss of Services/Products: " + daysLeft);
				subscriptionInfo.addFormItem(gracePeriod, "Grace Period:");
			}
		} else {
			final @NotNull Icon icon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
			icon.getStyle().set("padding", "var(--lumo-space-xs)");
			final @NotNull Span noSubscription = new Span(icon, new Span("No Subscription"));
			noSubscription.getElement().getThemeList().add("badge contrast small");

			subscriptionInfo.add(heading, noSubscription);
		}

		return subscriptionInfo;
	}

	private @NotNull VerticalLayout generateBusinessActions() {
		if(subscription == null) {
			throw new NullPointerException();
		}

		final @NotNull HorizontalLayout actionButtons = new HorizontalLayout();

		final @NotNull Button businessStatus = createBusinessStatusBtn();
		businessStatus.addThemeVariants(ButtonVariant.LUMO_ERROR);
		actionButtons.add(businessStatus);

		initializeEditButton();
		actionButtons.add(editBusiness);

		if(SubscriptionStatus.PENDING_PAYMENT.equals(subscription.getStatus()) ||
			SubscriptionStatus.GRACE_PERIOD.equals(subscription.getStatus())) {
			final @NotNull Button paySubscription = new Button("Pay Subscription", new Icon(VaadinIcon.DOLLAR), event -> {
				// TODO: Stripe Checkout Flow for Making a Payment towards the Subscription (Do Not Save New Stripe Token)
			});

			actionButtons.add(paySubscription);
		}

		final @NotNull VerticalLayout businessActions = new VerticalLayout();
		businessActions.add(new H2("Actions"));
		businessActions.add(actionButtons);

		return businessActions;
	}

	private @NotNull Button createBusinessStatusBtn() {
		boolean pendingDeactivation = business.getStatus() == AccountStatus.PENDING_DEACTIVATION;
		return new Button(pendingDeactivation ? REACTIVATE : DEACTIVATE, event -> {
			final @NotNull ConfirmDialog confirm = new ConfirmDialog();
			confirm.setHeader(pendingDeactivation ? REACTIVATION_TITLE : DEACTIVATION_TITLE);
			confirm.setText(pendingDeactivation ? REACTIVATION_DIALOG : DEACTIVATION_DIALOG);
			confirm.setCancelable(true);
			confirm.addCancelListener(cancelEvent -> confirm.close());

			confirm.setConfirmText(pendingDeactivation ? REACTIVATE : DEACTIVATE);
			confirm.addConfirmListener(confirmEvent -> {
				business.setDeleted(true);
				businessService.upsert(business);
				confirm.close();

				@NotNull String verb = pendingDeactivation ? REACTIVATE : DEACTIVATE;
				@NotNull String successMessage = "Successfully " + verb +  "ed the " + business.getName() + " Business";

				AuditLog.info(String.format("%s (%s)", successMessage, business.getId()));
				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			});
		});
	}

	private void initializeEditButton() {
		editBusiness = new Button("Edit",
			new Icon(VaadinIcon.EDIT), event -> {
				OTPValidationView.sendOTPCode(business.getOwner(), "Edit Business", authService, userService, emailService);
				AuditLog.info(String.format("Successfully sent OTP Request to Edit Business (%s)", business.getOwner()));
				this.getUI().map(ui -> ui.navigate(OTPValidationView.class));
			}
		);
	}
}
