package com.onyx.platform;

import com.onyx.platform.frontend.views.user.auth.LoginView;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.NavigationAccessControl;

public class ApplicationServiceInitListener implements VaadinServiceInitListener {

    private final NavigationAccessControl accessControl;


    public ApplicationServiceInitListener() {
        accessControl = new NavigationAccessControl();
        accessControl.setLoginView(LoginView.class);
    }

    @Override
    public void serviceInit(ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.getSource().addUIInitListener(uiInitEvent -> uiInitEvent.getUI().addBeforeEnterListener(accessControl));
    }
}
