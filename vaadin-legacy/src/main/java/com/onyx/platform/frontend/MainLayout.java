package com.onyx.platform.frontend;

import com.onyx.platform.backend.services.AuthService;
import com.onyx.platform.backend.services.ProductService;
import com.onyx.platform.backend.services.UserService;
import com.onyx.platform.frontend.views.CompanyView;
import com.onyx.platform.frontend.views.HomeView;
import com.onyx.platform.frontend.views.admin.business.ManageBusinessesView;
import com.onyx.platform.frontend.views.admin.product.ManageProductsView;
import com.onyx.platform.frontend.views.product.ProductsView;
import com.onyx.platform.frontend.views.user.auth.LoginView;
import com.onyx.platform.frontend.views.admin.user.ManageUsersView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;


/**
 * MainLayout of the Onyx Website.
 * Houses the MenuBar which has the
 * Home, Products, and Company Pages
 */
public class MainLayout extends AppLayout {
	private final @NotNull MenuBar menuBar;
	private final @NotNull UserService userService;
	private final @NotNull AuthService authService;
	private final @NotNull ProductService productService;

	@Inject
	public MainLayout(@NotNull UserService userService,
					  @NotNull AuthService authService,
                      @NotNull ProductService productService) {
        this.userService = userService;
        this.authService = authService;
        this.productService = productService;

		menuBar = new MenuBar();

		addHeaderContent();
		addFooterContent();
	}

	private void addHeaderContent() {
		final @NotNull H1 logo = new H1();
		logo.addClassNames(
			LumoUtility.FontSize.LARGE,
			LumoUtility.Margin.MEDIUM);

		final @NotNull HorizontalLayout header = new HorizontalLayout(logo);
		header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		header.setWidthFull();
		header.addClassNames(
			LumoUtility.Padding.Vertical.NONE,
			LumoUtility.Padding.Horizontal.MEDIUM);

		this.addToNavbar(true, header);
		addMenuContent();
	}

	private void addFooterContent() {

	}

	private void addMenuContent() {
		menuBar.addItem("Home", menuBarClickListener());

		final @NotNull MenuItem products = menuBar.addItem("Products", menuBarClickListener());
		final @NotNull SubMenu productsSubMenu = products.getSubMenu();
		productService.getAvailableProducts()
			.forEach(product -> productsSubMenu.addItem(product.getName(), productClickListener(product.getId())));

		menuBar.addItem("Company", menuBarClickListener());

		if(AuthService.isAdmin()) {
			final @NotNull MenuItem admin = menuBar.addItem("Admin");
			final @NotNull SubMenu adminSubMenu = admin.getSubMenu();

			adminSubMenu.addItem("Manage Users", event -> this.getUI().map(ui -> ui.navigate(ManageUsersView.class)));
			adminSubMenu.addItem("Manage Businesses", event -> this.getUI().map(ui -> ui.navigate(ManageBusinessesView.class)));
			adminSubMenu.addItem(new Hr());
			adminSubMenu.addItem("Manage Products", event -> this.getUI().map(ui -> ui.navigate(ManageProductsView.class)));
		}

		if(authService.isAuthenticated()) {
			menuBar.addItem("Logout", menuBarClickListener());
		} else {
			menuBar.addItem("Login/Signup", menuBarClickListener());
		}

		menuBar.setOpenOnHover(true);
		menuBar.getStyle().set("margin-left", "auto");
		menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);

		this.addToNavbar(menuBar);
	}

	private @NotNull ComponentEventListener<@NotNull ClickEvent<@NotNull MenuItem>> productClickListener(@NotNull Long productId) {
		return listener -> this.getUI().map(ui -> ui.navigate(ProductsView.class, productId));
	}

	private @NotNull ComponentEventListener<@NotNull ClickEvent<@NotNull MenuItem>> menuBarClickListener() {
		return listener -> {
			switch (listener.getSource().getText()) {
				case "Home":
					this.getUI().map(ui -> ui.navigate(HomeView.class));
					break;

				case "Products":
					this.getUI().map(ui -> ui.navigate(ProductsView.class));
					break;

				case "Company":
					this.getUI().map(ui -> ui.navigate(CompanyView.class));
					break;

				case "Login/Signup":
					this.getUI()
							.flatMap(ui -> ui.navigate(LoginView.class))
							.ifPresent(LoginView::toggleOverlay);
					break;

				case "Logout":
					authService.logout();
					break;
			}
		};
	}
}
