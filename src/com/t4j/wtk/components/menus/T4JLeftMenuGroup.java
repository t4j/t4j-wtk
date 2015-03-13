
package com.t4j.wtk.components.menus;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.t4j.wtk.components.buttons.T4JLeftMenuButton;
import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JStringUtils;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JLeftMenuGroup extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private String headerHtml;

	private LinkedList<T4JLeftMenuButton> buttons;

	public T4JLeftMenuGroup() {

		this(null, null);
	}

	public T4JLeftMenuGroup(String headerHtml) {

		this(headerHtml, null);
	}

	public T4JLeftMenuGroup(String headerHtml, List<T4JLeftMenuButton> buttons) {

		super();

		this.headerHtml = headerHtml;

		setButtons(buttons);
	}

	protected void setupComponent() {

		addStyleName("t4j-left-menu-group");

		setMargin(false, false, true, false); // Margen inferior
	}

	public void addLeftMenuButton(T4JLeftMenuButton button) {

		if (null == button) {

			return;
		} else {

			getButtons().add(button);
		}
	}

	@Override
	public void attach() {

		removeAllComponents();

		if (false == T4JStringUtils.isNullOrEmpty(headerHtml)) {

			Label headerLabel = new Label(headerHtml, Label.CONTENT_RAW);
			headerLabel.addStyleName("t4j-left-menu-group-header-label");
			addComponent(headerLabel);
		}

		if (false == T4JCollectionUtils.isNullOrEmpty(buttons)) {

			VerticalLayout itemsWrapper = new VerticalLayout();
			itemsWrapper.addStyleName("t4j-left-menu-group-items-wrapper");

			for (Iterator<T4JLeftMenuButton> iterator = buttons.iterator(); iterator.hasNext();) {

				itemsWrapper.addComponent(iterator.next());
			}

			addComponent(itemsWrapper);
		}

		super.attach();
	}

	public List<T4JLeftMenuButton> getButtons() {

		if (null == buttons) {

			buttons = new LinkedList<T4JLeftMenuButton>();
		}

		return buttons;
	}

	public String getHeaderHtml() {

		return headerHtml;
	}

	public void setButtons(List<T4JLeftMenuButton> buttons) {

		if (T4JCollectionUtils.isNullOrEmpty(buttons)) {
			this.buttons = new LinkedList<T4JLeftMenuButton>();
		} else {
			this.buttons = new LinkedList<T4JLeftMenuButton>(buttons);
		}
	}

	public void setHeaderHtml(String headerHtml) {

		this.headerHtml = headerHtml;
	}

}
