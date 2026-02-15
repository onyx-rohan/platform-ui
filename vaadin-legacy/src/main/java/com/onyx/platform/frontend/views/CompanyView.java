package com.onyx.platform.frontend.views;

import com.onyx.platform.frontend.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PermitAll
@AnonymousAllowed
@Route(value = "company", layout = MainLayout.class)
@PageTitle("Company | Onyx Softworks")
public class CompanyView extends VerticalLayout {
}
