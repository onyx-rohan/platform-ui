package com.onyx.platform.frontend.views.user.auth;

import com.onyx.platform.backend.models.AuditLog;
import com.onyx.platform.backend.models.User;
import com.onyx.platform.backend.services.AuthService;
import com.onyx.platform.backend.services.EmailService;
import com.onyx.platform.backend.services.UserService;
import com.onyx.platform.backend.utils.Utils;
import com.onyx.platform.frontend.views.business.BusinessView;
import com.onyx.platform.frontend.views.user.ProfileView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@PermitAll
@Route(value = "otp-validation")
@PageTitle("Reset Password, Code Validation | Onyx Softworks")
public class OTPValidationView extends VerticalLayout {
    private final @NotNull User user = new User();
    private final @NotNull AuthService authService;
    private final @NotNull UserService userService;
    private final @NotNull EmailService emailService;

    private final @NotNull Timer timer = new Timer("OTP Timer");

    private final @NotNull Text timerText = new Text("");
    private final @NotNull Button sendOtpBtn = new Button("Send", new Icon(VaadinIcon.CHEVRON_CIRCLE_RIGHT_O));
    private final @NotNull TextField otpTextField = new TextField("OTP Code");

    @Inject
    public OTPValidationView(@NotNull AuthService authService,
                             @NotNull UserService userService,
                             @NotNull EmailService emailService) {
        this.authService = authService;
        this.userService = userService;
        this.emailService = emailService;

        initialize();
        scheduleResendTimeOut();
    }

    private void initialize() {
        otpTextField.setAllowedCharPattern("[0-9]");
        otpTextField.setPlaceholder("Enter One-Time Password Code");
        otpTextField.setHelperText("A One-Time Password Code has been sent to your email at" + Utils.BUILD_MASK(user.getEmail()));
        otpTextField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        submitOTPConfiguration();
        sendOtpBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        sendOtpBtn.addClickListener(buttonClickEvent -> {
            if("Resend".equals(sendOtpBtn.getText()) && user.getOtpCode() != null) {
                final @NotNull String reason = user.getOtpCode().substring(user.getOtpCode().indexOf(": ") + 1);
                sendOTPCode(user, reason, authService, userService, emailService);
                scheduleResendTimeOut();
            } else if("Submit".equals(sendOtpBtn.getText())) {
                try {
                    final @Nullable String otpReason = authService.validateOTP(user.getId(), otpTextField.getValue());
                    if(otpReason != null) {
                        switch (otpReason) {
                            case "Reset Password":
                                this.getUI().flatMap(ui -> ui.navigate(ChangePasswordView.class));
                            case "Edit Profile":
                                this.getUI()
                                        .flatMap(ui -> ui.navigate(ProfileView.class))
                                        .ifPresent(ProfileView::editModeOn);
                            case "Edit Business":
                                this.getUI()
                                        .flatMap(ui -> ui.navigate(BusinessView.class))
                                        .ifPresent(BusinessView::editModeOn);
                        }
                        userService.wipeOTPCode(user);
                        AuditLog.info("Valid OTP Code Entered");
                        @NotNull Notification notification = Notification.show("Valid OTP Code Entered", 3000, Notification.Position.BOTTOM_CENTER);
                        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    } else {
                        AuditLog.info("Invalid OTP Code Entered");
                        @NotNull Notification notification = Notification.show("Invalid OTP Code Entered", 3000, Notification.Position.BOTTOM_CENTER);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                } catch (EntityNotFoundException e) {
                    @NotNull Notification notification = Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        });

        final @NotNull HorizontalLayout innerLayout = new HorizontalLayout(sendOtpBtn, otpTextField);
        innerLayout.setSizeFull();
        innerLayout.getThemeList().add("spacing-s");
        innerLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        final @NotNull VerticalLayout outerLayout = new VerticalLayout(innerLayout);
        outerLayout.setPadding(true);
        outerLayout.setHeight("200px");
        outerLayout.setWidth("500px");
        outerLayout.add(timerText);
        outerLayout.setAlignItems(Alignment.CENTER);
        outerLayout.setAlignSelf(Alignment.END, timerText);

        this.add(outerLayout);
    }

    private void scheduleResendTimeOut() {
        final @NotNull Instant start = Instant.now();

        timer.schedule(new TimerTask() {
           public void run() {
               final @NotNull Instant end = start.plus(AuthService.OTP_DURATION);
               if(Instant.now().isAfter(end)) {     // Timer Has Expired; OTP is No Longer Valid
                   resendOTPConfiguration();
               } else {                             // Timer Still Active; OTP is Still Valid
                   submitOTPConfiguration();
               }
           }
        }, 0);
    }

    private void submitOTPConfiguration() {
        sendOtpBtn.setText("Submit");
        sendOtpBtn.setIcon(new Icon(VaadinIcon.CHEVRON_CIRCLE_RIGHT_O));
        timerText.setVisible(true);
        timerText.setText("Can Resend in " + DurationFormatUtils.formatDuration(AuthService.OTP_DURATION.toMillis(), "mm:ss", true));
    }

    private void resendOTPConfiguration() {
        sendOtpBtn.setText("Resend");
        sendOtpBtn.setIcon(new Icon(VaadinIcon.ROTATE_RIGHT));
        userService.wipeOTPCode(user);
        timerText.setVisible(false);
    }

    public static void sendOTPCode(@NotNull User user,
                                   @NotNull String reason,
                                   @NotNull AuthService authService,
                                   @NotNull UserService userService,
                                   @NotNull EmailService emailService) {
        final @NotNull String otp = authService.generateOTP();
        userService.setOTPCode(user, otp, reason);
        emailService.sendOTPCodeEmail(user, otp);

        @NotNull Notification notification = Notification.show("Sent OTP Code", 3000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
