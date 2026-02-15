package com.onyx.platform.frontend.views.admin.user;

import com.onyx.platform.backend.enums.UserType;
import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.User;
import com.onyx.platform.backend.services.AuthService;
import com.onyx.platform.backend.services.EmailService;
import com.onyx.platform.backend.services.UserService;
import com.onyx.platform.frontend.MainLayout;
import com.onyx.platform.frontend.components.ManageEntityLayout;
import com.onyx.platform.frontend.views.admin.business.ManageBusinessesView;
import com.onyx.platform.frontend.views.user.auth.OTPValidationView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Page for Admins to Edit, Delete, or Create Users
 */
@Slf4j
@RolesAllowed({ "admin" })
@Route(value = "manage-users", layout = MainLayout.class)
@PageTitle("Manage Users | Onyx Softworks")
public class ManageUsersView extends ManageEntityLayout<User> implements HasUrlParameter<Long> {
	private final @NotNull AuthService authService;
	private final @NotNull UserService userService;
	private final @NotNull EmailService emailService;

	private @NotNull User user = new User();

	@Inject
	public ManageUsersView(@NotNull AuthService authService,
						   @NotNull UserService userService,
                           @NotNull EmailService emailService) {
		super();

		AuditLog.info(String.format("Moved to manage-users/%s", user.getId()));
        this.authService = authService;
        this.userService = userService;
        this.emailService = emailService;

        addHeaderContent();
		addBodyContent();
		addFooterContent();
	}

	@Override
	public void setParameter(@NotNull BeforeEvent event, @Nullable @OptionalParameter Long userId) {
		if(userId != null) {
			AuditLog.info(String.format("Moved to manage-users/%s", userId));
			final @NotNull Optional<@NotNull User> optional = userService.getWithLock(userId);
			if (optional.isPresent()) {
				user = optional.get();
				generateEditor(user);
			}
			else {
				event.rerouteToError(
						IllegalArgumentException.class,
						this.getTranslation("user.id.not.found", event.getLocation().getPath())
				);
			}
		} else {
			AuditLog.info("Moved to manage-users/");
		}
	}

	public void manage(@NotNull User user) {
		updateRouteParameter(user);
		generateEditor(user);
	}

	private void updateRouteParameter(@NotNull User User) {
		final @NotNull String deepLinkingUrl = RouteConfiguration.forSessionScope()
			.getUrl(getClass(), User.getId());
		this.getUI().ifPresent(ui -> ui.getPage()
			.getHistory()
			.replaceState(null, deepLinkingUrl));
	}

	private void addHeaderContent() {
		final @NotNull Button addUser = new Button(new Icon(VaadinIcon.PLUS), event -> {
			if(editor.isOpened()) {
				editor.close();
			}

			openEditor(new User(), "Add User");
		});
		addUser.addThemeVariants(ButtonVariant.LUMO_ICON);
		addUser.setTooltipText("Add a new User");

		final @NotNull HorizontalLayout titleSection = new HorizontalLayout(addUser);
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
	protected @NotNull VerticalLayout generateEditor(@NotNull User user) {
		final @NotNull TextField firstName = new TextField();
		firstName.setWidthFull();
		firstName.setRequired(true);
		firstName.setRequiredIndicatorVisible(true);
		firstName.setPlaceholder("User First Name");
		firstName.setValue(user.getFirstName());
		binder.forField(firstName)
				.asRequired("User is required to have a First Name")
				.bind(User::getFirstName, User::setFirstName);

		final @NotNull TextField lastName = new TextField();
		lastName.setWidthFull();
		lastName.setRequired(true);
		lastName.setRequiredIndicatorVisible(true);
		lastName.setPlaceholder("User Last Name");
		lastName.setValue(user.getLastName());
		binder.forField(lastName)
				.asRequired("User is required to have a Last Name")
				.bind(User::getLastName, User::setLastName);

		final @NotNull TextField email = new TextField();
		email.setWidthFull();
		email.setRequired(true);
		email.setRequiredIndicatorVisible(true);
		email.setPlaceholder("User Email");
		email.setValue(user.getLastName());
		binder.forField(email)
				.asRequired("User is required to have an Email")
				.bind(User::getEmail, User::setEmail);

		final @NotNull TextField phone = new TextField();
		phone.setWidthFull();
		phone.setRequired(true);
		phone.setRequiredIndicatorVisible(true);
		phone.setPlaceholder("User Phone Number");
		phone.setValue(user.getPhone());
		binder.forField(phone)
				.asRequired("User is required to have a phone number")
				.bind(User::getPhone, User::setPhone);

		@NotNull List<@NotNull String> countries = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream()
				.map(ctry -> new Locale("ENGLISH", ctry).getDisplayCountry(Locale.ROOT))
				.toList();

		final @NotNull ComboBox<@NotNull String> country = new ComboBox<>();
		country.setWidthFull();
		country.setRequired(true);
		country.setRequiredIndicatorVisible(true);
		country.setPlaceholder("User Country");
		country.setValue(user.getCountry());
		country.setItems(countries);
		binder.forField(country)
				.asRequired("User is required to have a country")
				.bind(User::getCountry, User::setCountry);

		final @NotNull ComboBox<@NotNull UserType> role = new ComboBox<>();
		role.setWidthFull();
		role.setRequired(true);
		role.setRequiredIndicatorVisible(true);
		role.setPlaceholder("User Role");
		role.setValue(UserType.CONSUMER);
		role.setItems(UserType.values());
		role.setItemLabelGenerator(UserType::name);
		binder.forField(role)
				.asRequired("User is required to have a role")
				.bind(User::getRole, User::setRole);

		final @NotNull Button reset = new Button("Reset", event -> binder.refreshFields());
		final @NotNull Button save = new Button("Save", event -> {
			final @NotNull String userTitle = user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")";

			try {
				@NotNull String successMessage = "Successfully Updated the " + userTitle + " User";
				if(editor.getHeaderTitle().equals("Add User")) {
					// TODO: Ensure that newly created Users get an Email to set their Password
					successMessage = "Successfully Saved the New User, " + userTitle;
					userService.create(user);
				} else {
					userService.upsert(user);
				}

				AuditLog.info(String.format("%s (%s)", successMessage, user.getId()));
				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			} catch (EntityExistsException e) {
				@NotNull Notification notification = Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
		final @NotNull Button resetPassword = new Button("Reset Password", event -> {
			try {
				userService.upsert(user);
				OTPValidationView.sendOTPCode(user, "Reset Password", authService, userService, emailService);
				AuditLog.info(String.format("Successfully Sent Reset Password Email to User (%s)", user.getId()));
				this.getUI().map(ui -> ui.navigate(OTPValidationView.class));
			} catch (EntityExistsException e) {
				@NotNull Notification notification = Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});

		return new VerticalLayout(firstName, lastName, email, phone, country, role, new HorizontalLayout(reset, save, resetPassword));
	}

	@Override
	protected void generateGrid() {
		grid.setItems(userService.getAll());
		grid.addColumn(User::getId);
		grid.addColumn(User::getFirstName).setFrozen(true).setHeader("First Name");
		grid.addColumn(User::getLastName).setFrozen(true).setHeader("Last Name");
		grid.addColumn(User::getEmail).setFrozen(true).setHeader("Email");
		grid.addColumn(User::getPhone).setHeader("Phone #");
		grid.addColumn(buildCountryRenderer()).setHeader("Country");
		grid.addColumn(buildActionsRenderer()).setFrozen(true).setHeader("Actions");
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull User> buildCountryRenderer() {
		return new ComponentRenderer<>(Div::new, (div, user) -> {
			final @NotNull TextField country = new TextField();
			country.setReadOnly(true);
			country.setValue(user.getCountry());
			country.getStyle().remove("--vaadin-input-field-border-width");

			div.add(country);
		});
	}

	private @NotNull ComponentRenderer<@NotNull Div, @NotNull User> buildActionsRenderer() {
		return new ComponentRenderer<>(Div::new, (div, user) -> {
			final @NotNull String userTitle = user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")";
			final @NotNull Button edit = new Button(new Icon(VaadinIcon.EDIT), event -> {
				if(editor.isOpened()) {
					editor.close();
				}

				openEditor(user, "Edit " + userTitle);
			});
			edit.addThemeVariants(ButtonVariant.LUMO_ICON);
			edit.setTooltipText("Edit the User");

			final @NotNull Button softDelete = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE_O), event -> {
				final @NotNull ConfirmDialog confirm = new ConfirmDialog();
				confirm.setHeader("\"Soft\" Delete Confirmation");
				confirm.setText("Are you sure you wish to \"soft\" delete this User?\n" +
						"(The User will still exist within the database but will not be searchable by non-admin users "+
						"making this a reversible action)");
				confirm.setCancelable(true);
				confirm.addCancelListener(cancelEvent -> confirm.close());

				confirm.setConfirmText("Delete");
				confirm.addConfirmListener(confirmEvent -> {
					user.setDeleted(true);
					userService.upsert(user);
					confirm.close();

					@NotNull String successMessage = "Successfully (Soft) Deleted the " + userTitle + " User";

					AuditLog.info(String.format("%s (%s)", successMessage, user.getId()));
					@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				});
			});
			softDelete.addThemeVariants(ButtonVariant.LUMO_ICON);
			softDelete.setTooltipText("Delete this User (Non-Permanent)");

			final @NotNull Button physicalDelete = createPhysicalDeleteButton(user, userTitle);

			div.add(edit, softDelete, physicalDelete);
			if(user.getBusiness() != null) {
				div.addClickListener(event -> this.getUI()
                        .flatMap(ui -> ui.navigate(ManageBusinessesView.class))
                        .ifPresent(manageBusinessesView -> manageBusinessesView.manage(user.getBusiness())));
			}
		});
	}

	private @NotNull Button createPhysicalDeleteButton(@NotNull User user, @NotNull String userTitle) {
		final @NotNull Icon physicalDeleteIcon = new Icon(VaadinIcon.CLOSE_CIRCLE);
		physicalDeleteIcon.setColor("red");
		final @NotNull Button physicalDelete =  new Button(physicalDeleteIcon, event -> {
			final @NotNull ConfirmDialog confirm = new ConfirmDialog();
			confirm.setHeader("\"Physical\" Delete Confirmation");
			confirm.setText("Are you sure you wish to \"physically\" delete this User?\n" +
					"(The User will no longer exist within the database. This action is NOT reversible)");
			confirm.setCancelable(true);
			confirm.addCancelListener(cancelEvent -> confirm.close());

			confirm.setConfirmText("Delete");
			confirm.addConfirmListener(confirmEvent -> {
				userService.delete(user);
				confirm.close();

				@NotNull String successMessage = "Successfully (Hard) Deleted the " + userTitle + " User";

				AuditLog.info(String.format("%s (%s)", successMessage, user.getId()));
				@NotNull Notification notification = Notification.show(successMessage, 3000, Notification.Position.BOTTOM_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			});
		});
		physicalDelete.addThemeVariants(ButtonVariant.LUMO_ICON);
		physicalDelete.setTooltipText("Delete this User (Permanently)");

		return physicalDelete;
	}
}