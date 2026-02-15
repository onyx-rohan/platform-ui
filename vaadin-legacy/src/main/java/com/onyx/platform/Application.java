package com.onyx.platform;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@Theme("theme")
@PWA(name = "Onyx", shortName = "Onyx")
public class Application implements AppShellConfigurator {
}