package com.onyx.platform.frontend.views.user.auth;

import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.Business;
import com.onyx.platform.backend.models.BusinessType;
import com.onyx.platform.backend.models.User;
import com.onyx.platform.backend.services.BusinessService;
import com.onyx.platform.backend.services.UserService;
import com.onyx.platform.backend.utils.Utils;
import com.onyx.platform.frontend.components.CreateNewPasswordField;
import com.onyx.platform.frontend.components.PhoneField;
import com.onyx.platform.frontend.views.HomeView;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * Form to be filled and Create a User to log into the Platform
 * <ul>
 *     <li>Requests some of the User's Personal Identifiable Information (PII); FirstName, LastName, Email, Phone, and Country </li>
 *     <li>Requests if the User owns a Business and the Business-Related Information</li>
 *     <ul><li>
 *         If the User wants to Create a Business, they will be Taken to the BusinessCreationView
 *     </li></ul>
 * </ul>
 */
@Slf4j
@PermitAll
@Route(value = "sign-up")
@PageTitle("Sign Up | Onyx Softworks")
public class SignUpView extends VerticalLayout {
    private final @NotNull User user;
    private final @NotNull UserService userService;

    private final @NotNull Business business;
    private final @NotNull BusinessService businessService;

    private @NotNull AccordionPanel userInformationPanel = new AccordionPanel();
    private @NotNull AccordionPanel cardInformationPanel = new AccordionPanel();
    private @NotNull AccordionPanel businessInformationPanel = new AccordionPanel();

    private final @NotNull TextField firstName = new TextField("First Name");
    private final @NotNull TextField lastName = new TextField("Last Name");
    private final @NotNull EmailField userEmail = new EmailField("Email");
    private final @NotNull ComboBox<@NotNull String> userCountry = new ComboBox<>("Country");
    private final @NotNull PhoneField userPhone = new PhoneField();
    private final @NotNull CreateNewPasswordField password = new CreateNewPasswordField();
    private final @NotNull Binder<@NotNull User> userBinder = new Binder<>(User.class);

    private final @NotNull TextField businessName = new TextField("Name");
    private final @NotNull PhoneField businessPhone = new PhoneField();
    private final @NotNull TextField businessVATNumber = new TextField("VAT Number");
    private final @NotNull ComboBox<@NotNull String> businessCountry = new ComboBox<>("Country");
    private final @NotNull ComboBox<@NotNull BusinessType> businessType = new ComboBox<>("Business Type");
    private final @NotNull Binder<@NotNull Business> businessBinder = new Binder<>(Business.class);

    @Inject
    public SignUpView(@NotNull UserService userService,
                      @NotNull BusinessService businessService) {
        AuditLog.info("Moved to sign-up");
        this.user = new User();
        this.userService = userService;

        this.business = new Business();
        this.businessService = businessService;

        userInformationPanel.addThemeVariants(DetailsVariant.REVERSE);
        cardInformationPanel.addThemeVariants(DetailsVariant.REVERSE);
        businessInformationPanel.addThemeVariants(DetailsVariant.REVERSE);

        addHeaderContent();
        addBodyContent();
        addFooterContent();
    }

    private void addHeaderContent() {
        final @NotNull HorizontalLayout titleSection = new HorizontalLayout(new H2("Creating New User"));
        this.add(titleSection);
    }

    private void addBodyContent() {
        final @NotNull Accordion signUpAccordion = new Accordion();

        userInformationPanel = signUpAccordion.add("User Information", generatePIIForm());
        cardInformationPanel = signUpAccordion.add("Card Information", generateCardForm());
        businessInformationPanel = signUpAccordion.add("Business Information", generateBusinessForm());

        @NotNull Button signUpBtn = new Button("Sign Up", new Icon(VaadinIcon.USER_CARD), (event -> {
            @NotNull String errorMessage = "";

            if(!user.equals(new User())) {
                try {
                    userService.create(user);
                } catch (EntityExistsException e) {
                    errorMessage = e.getMessage();
                }
            } else {
                errorMessage = "No user information provided";
            }

            // TODO: Add Validation Checks for Adding a New Card to Strapi once Integrated

            if(!business.equals(new Business())) {
                try {
                    businessService.create(business);
                } catch (EntityExistsException e) {
                    errorMessage = e.getMessage();
                }
            }

            if(!errorMessage.isEmpty()) {
                @NotNull Notification notification = Notification.show(errorMessage, 3000, Notification.Position.BOTTOM_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                @NotNull Notification notification = Notification.show("Successfully Created User", 3000, Notification.Position.BOTTOM_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                this.getUI().ifPresent(ui -> ui.navigate(HomeView.class));
            }
        }));

        this.add(signUpAccordion, signUpBtn);
    }

    private void addFooterContent() {}

    private @NotNull VerticalLayout generatePIIForm() {
        firstName.setAllowedCharPattern(Utils.NAME_REGEX);
        firstName.setHelperText("Your First Name must be between 3 and 24 characters long and cannot consist "
                + "of #$%^*.");
        firstName.setRequiredIndicatorVisible(true);
        firstName.setRequired(true);

        lastName.setAllowedCharPattern(Utils.NAME_REGEX);
        lastName.setHelperText("Your Last Name must be between 3 and 24 characters long and cannot consist "
                + "of #$%^*.");
        lastName.setRequiredIndicatorVisible(true);
        lastName.setRequired(true);

        userEmail.setErrorMessage("Please enter a valid email address");
        userEmail.setAllowedCharPattern(Utils.EMAIL_REGEX);
        userEmail.setRequiredIndicatorVisible(true);
        userEmail.setRequired(true);

        password.setRequiredIndicatorVisible(true);

        @NotNull List<@NotNull String> countries = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream()
                .map(ctry -> new Locale("ENGLISH", ctry).getDisplayCountry(Locale.ROOT))
                .toList();
        userCountry.setItems(countries);
        userCountry.setRequiredIndicatorVisible(true);
        userCountry.setRequired(true);

        userPhone.setRequiredIndicatorVisible(true);

        final @NotNull FormLayout formLayout = new FormLayout(firstName, lastName, userEmail, password, userCountry, userPhone);
        formLayout.setColspan(userEmail, 2);
        formLayout.setColspan(password,2);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        userBinder.forField(firstName).bind(User::getFirstName, User::setFirstName);
        userBinder.forField(lastName).bind(User::getLastName, User::setLastName);
        userBinder.forField(userEmail).bind(User::getEmail, User::setEmail);
        userBinder.forField(userCountry).bind(User::getCountry, User::setCountry);
        userBinder.forField(userPhone).bind(User::getPhone, User::setPhone);
        userBinder.setBean(user);

        final @NotNull Button next = new Button("Next", event -> {
            if (password.validatePassword().isPresent()) {
                user.setPassword(password.validatePassword().get());
                cardInformationPanel.setOpened(true);
            }
        });

        return new VerticalLayout(formLayout, next);
    }

    private @NotNull VerticalLayout generateCardForm() {
        // TODO: Build FormLayout based on What is Required to add a Card to Stripe

        return new VerticalLayout(
                new Button("Next", event -> businessInformationPanel.setOpened(true)));
    }

    private @NotNull VerticalLayout generateBusinessForm() {
        businessName.setHelperText("Your Business' name is how customers will identify your business");

        businessType.setItems(businessService.getAllBusinessTypes());

        @NotNull List<@NotNull String> countries = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream()
                .map(ctry -> new Locale("ENGLISH", ctry).getDisplayCountry(Locale.ROOT))
                .toList();
        businessCountry.setItems(countries);

        businessPhone.setRequiredIndicatorVisible(true);

        businessVATNumber.setHelperText("Your Business' VAT Number should be matched to your country's information");

        final @NotNull FormLayout formLayout = new FormLayout(businessName, businessType, businessCountry, businessPhone, businessVATNumber);
        formLayout.setColspan(businessName, 2);
        formLayout.setColspan(businessType,2);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        businessBinder.forField(businessName).bind(Business::getName, Business::setName);
        businessBinder.forField(businessType).bind(Business::getType, Business::setType);
        businessBinder.forField(businessCountry).bind(Business::getCountry, Business::setCountry);
        businessBinder.forField(businessPhone).bind(Business::getPhone, Business::setPhone);
        businessBinder.forField(businessVATNumber).bind(Business::getVatNumber, Business::setVatNumber);
        businessBinder.setBean(business);

        return new VerticalLayout(formLayout);
    }
}
