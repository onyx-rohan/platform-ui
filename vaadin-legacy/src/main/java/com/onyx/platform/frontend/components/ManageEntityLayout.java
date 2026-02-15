package com.onyx.platform.frontend.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import org.jetbrains.annotations.NotNull;

public abstract class ManageEntityLayout<T> extends VerticalLayout {
	protected final @NotNull Dialog editor;
	protected final @NotNull Binder<@NotNull T> binder;
	protected final @NotNull Grid<@NotNull T> grid;

	public ManageEntityLayout() {
		this.binder = new Binder<>();
		this.editor = new Dialog();
		this.grid = new Grid<>();

		this.editor.setModal(true);
		this.editor.setCloseOnEsc(true);

		generateGrid();
	}

	protected void openEditor(@NotNull T obj, @NotNull String title) {
		binder.setBean(obj);

		final @NotNull Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), (e) -> editor.close());
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
		closeButton.setTooltipText("Close this Editor");

		editor.getHeader().add(closeButton);
		editor.add(generateEditor(obj));
		editor.setHeaderTitle(title);

		editor.open();
	}
	protected abstract @NotNull VerticalLayout generateEditor(@NotNull T obj);

	protected abstract void generateGrid();
}
