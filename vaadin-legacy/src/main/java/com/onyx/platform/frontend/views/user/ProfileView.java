package com.onyx.platform.frontend.views.user;

import com.onyx.platform.backend.enums.AccountStatus;
import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.User;
import com.onyx.platform.backend.services.AuthService;
import com.onyx.platform.backend.services.EmailService;
import com.onyx.platform.backend.services.UserService;
import com.onyx.platform.frontend.MainLayout;
import com.onyx.platform.frontend.components.PhoneField;
import com.onyx.platform.frontend.views.business.BusinessView;
import com.onyx.platform.frontend.views.admin.user.ManageUsersView;
import com.onyx.platform.frontend.views.user.auth.ChangePasswordView;
import com.onyx.platform.frontend.views.user.auth.OTPValidationView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.onyx.platform.backend.constants.Messages.*;
import static com.onyx.platform.frontend.components.ComponentGenerator.GENERATE_STATUS_BADGE;

/**
 * Detailed View of a User's Profile
 * <ul>
 *     <li>Shows a User's General Info: name, phone #, vat #, country, and business type</li>
 * </ul>
 */
@Slf4j
@PermitAll
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profile | Onyx Softworks")
public class ProfileView extends VerticalLayout implements HasUrlParameter<Long> {
	private @NotNull User user = new User();

	private @NotNull Button editUser = new Button();

	private final @NotNull AuthService authService;
	private final @NotNull UserService userService;
	private final @NotNull EmailService emailService;

	private final @NotNull TextField firstName = new TextField();
	private final @NotNull TextField lastName = new TextField();
	private final @NotNull EmailField email = new EmailField();
	private final @NotNull PhoneField phone = new PhoneField();
	private final @NotNull PasswordField password = new PasswordField();
	private final @NotNull HorizontalLayout passwordSection = new HorizontalLayout();
	private final @NotNull ComboBox<@NotNull String> country = new ComboBox<>();
	private final @NotNull Binder<@NotNull User> userBinder = new Binder<>(User.class);

	@Inject
	public ProfileView(@NotNull AuthService authService, @NotNull UserService userService, @NotNull EmailService emailService) {
		AuditLog.info(String.format("Moved to profile/%s", user.getId()));

		this.authService = authService;
		this.userService = userService;
		this.emailService = emailService;

		addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void setParameter(@NotNull BeforeEvent event, @NotNull Long userId) {
		final @NotNull Optional<@NotNull User> optional = userService.getIfAvailable(userId);
		if(optional.isPresent()) {
			user = optional.get();
			userBinder.forField(firstName).bind(User::getFirstName, User::setFirstName);
			userBinder.forField(lastName).bind(User::getLastName, User::setLastName);
			userBinder.forField(email).bind(User::getEmail, User::setEmail);
			userBinder.forField(phone).bind(User::getPhone, User::setPhone);
			userBinder.forField(country).bind(User::getCountry, User::setCountry);
			userBinder.forField(password).bind(User::getPassword, User::setPassword);
			userBinder.setBean(user);
		} else {
			event.rerouteToError(
				IllegalArgumentException.class,
				this.getTranslation("user.id.not.found", event.getLocation().getPath())
			);
		}
	}

	public void editModeOn() {
		firstName.setReadOnly(false);
		lastName.setReadOnly(false);
		email.setReadOnly(false);
		phone.setReadOnly(false);
		country.setReadOnly(false);

		final @NotNull StreamResource saveIconResource = new StreamResource("floppy-disk.svg",
				() -> getClass().getResourceAsStream("/icons/floppy-disk.svg"));
		final @NotNull SvgIcon saveIcon = new SvgIcon(saveIconResource);

		editUser = new Button("Save", event -> {
			userService.upsert(user);

			final @NotNull String successMessage = "Successfully Edited Profile";
			AuditLog.info(String.format("%s (%s)", successMessage, user.getId()));
			@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

			initializeEditButton();
		});
		editUser.setSuffixComponent(saveIcon);
		editUser.addThemeVariants(ButtonVariant.LUMO_ERROR);

		updateRouteParameter(AuthService.getCurrentUser());
	}

	private void updateRouteParameter(@NotNull User user) {
		final @NotNull String deepLinkingUrl = RouteConfiguration.forSessionScope()
				.getUrl(getClass(), user.getId());
		this.getUI().ifPresent(ui -> ui.getPage()
				.getHistory()
				.replaceState(null, deepLinkingUrl));
	}

	private void addHeaderContent() {
		final @NotNull HorizontalLayout titleSection = new HorizontalLayout();
		titleSection.add(GENERATE_STATUS_BADGE(user));
		titleSection.add(new H2(user.getFirstName() + "'s Profile"));

		if(AuthService.isAdmin()) {
			titleSection.add(new Button("Manage User",
					new Icon(VaadinIcon.USER_STAR), event -> this.getUI()
					.flatMap(ui -> ui.navigate(ManageUsersView.class))
					.ifPresent(manageUsersView -> {
						manageUsersView.manage(user);
						titleSection.getStyle().set("margin-left", "auto");
					})
			));
		}

		this.add(titleSection);
	}

	private void addBodyContent() {
		this.add(
				generateGeneralInfo(),
				new Hr(),
				generateProfileActions()
		);

	}

	private void addFooterContent() {

	}

	private @NotNull FormLayout generateGeneralInfo() {
		firstName.setReadOnly(true);
		firstName.setAllowedCharPattern("(^(?!!@&_\\-)[!@&_\\-\\sa-zA-Z0-9]+(?<!!@&_\\-)$)");
		firstName.setHelperText("Your First Name must be between 3 and 24 characters long and cannot consist "
				+ "of #$%^*.");

		lastName.setReadOnly(true);
		lastName.setAllowedCharPattern("(^(?!!@&_\\-)[!@&_\\-\\sa-zA-Z0-9]+(?<!!@&_\\-)$)");
		lastName.setHelperText("Your Last Name must be between 3 and 24 characters long and cannot consist "
				+ "of #$%^*.");

		email.setReadOnly(true);
		email.setValue(user.getEmail());

		password.setReadOnly(true);
		final @NotNull Button changedPasswordBtn = new Button("Change Password", event ->
				this.getUI().map(ui -> ui.navigate(ChangePasswordView.class)));
		changedPasswordBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
		passwordSection.add(password, changedPasswordBtn);

		@NotNull List<@NotNull String> countries = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream()
			.map(ctry -> new Locale("ENGLISH", ctry).getDisplayCountry(Locale.ROOT))
			.toList();

		country.setReadOnly(true);
		country.setItems(countries);
		country.setHelperText("The country your Business operates out of. Your banking details and KYC verification "
			+ "is determined by this.");

		final @NotNull FormLayout generalInfo = new FormLayout();
		generalInfo.setWidthFull();
		generalInfo.add(new H2("Business Information"));
		generalInfo.setResponsiveSteps(new ResponsiveStep("0", 1));
		generalInfo.addFormItem(firstName, "First Name:");
		generalInfo.addFormItem(lastName, "Last Name:");
		generalInfo.addFormItem(email, "Email:");
		generalInfo.addFormItem(passwordSection, "Password:");
		generalInfo.addFormItem(phone, "Contact #:");
		generalInfo.addFormItem(country, "Country:");

		return generalInfo;
	}

	private @NotNull VerticalLayout generateProfileActions() {
		final @NotNull HorizontalLayout actionButtons = new HorizontalLayout();

		final @NotNull Button accountStatusBtn = createAccountStatusBtn();
		accountStatusBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
		actionButtons.add(accountStatusBtn);

		initializeEditButton();
		actionButtons.add(editUser);

		// TODO: Functionality to Manage Payment Methods

		if(user.getBusiness() != null) {
			actionButtons.add(new Button("Go to " + user.getBusiness().getName() + " Business",
				new Icon(VaadinIcon.BRIEFCASE), event -> this.getUI()
					.flatMap(ui -> ui.navigate(BusinessView.class))
					.ifPresent(businessView -> businessView.updateRouteParameter(user.getBusiness()))
			));
		}

		final @NotNull VerticalLayout businessActions = new VerticalLayout();
		businessActions.add(new H2("Actions"));
		businessActions.add(actionButtons);

		return businessActions;
	}

	private @NotNull Button createAccountStatusBtn() {
		boolean pendingDeactivation = user.getStatus() == AccountStatus.PENDING_DEACTIVATION;
		return new Button(pendingDeactivation ? REACTIVATE : DEACTIVATE, event -> {
			final @NotNull ConfirmDialog confirm = new ConfirmDialog();
			confirm.setHeader(pendingDeactivation ? REACTIVATION_TITLE : DEACTIVATION_TITLE);
			confirm.setText(pendingDeactivation ? REACTIVATION_DIALOG : DEACTIVATION_DIALOG);
			confirm.setCancelable(true);
			confirm.addCancelListener(cancelEvent -> confirm.close());

			confirm.setConfirmText(pendingDeactivation ? REACTIVATE : DEACTIVATE);
			confirm.addConfirmListener(confirmEvent -> {
				user.setDeleted(true);
				userService.upsert(user);
				confirm.close();

				@NotNull String verb = pendingDeactivation ? REACTIVATE : DEACTIVATE;
				@NotNull String successMessage = "Successfully " + verb +  "ed " + user.getFirstName() + " " + user.getLastName() + "'s Profile";

				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			});
		});
	}

	private void initializeEditButton() {
		editUser = new Button("Edit",
			new Icon(VaadinIcon.EDIT), event -> {
				OTPValidationView.sendOTPCode(user, "Edit Profile", authService, userService, emailService);
				AuditLog.info(String.format("Successfully sent OTP Request to Edit Profile (%s)", user));
				this.getUI().map(ui -> ui.navigate(OTPValidationView.class));
			}
		);
	}
}
