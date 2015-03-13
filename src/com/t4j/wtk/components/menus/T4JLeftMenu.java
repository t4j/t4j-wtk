
package com.t4j.wtk.components.menus;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.t4j.wtk.util.T4JCollectionUtils;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JLeftMenu extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private LinkedList<T4JLeftMenuGroup> leftMenuGroups;

	public T4JLeftMenu() {

		this(null);
	}

	public T4JLeftMenu(List<T4JLeftMenuGroup> leftMenuGroups) {

		super();

		setupComponent();

		setLeftMenuGroups(leftMenuGroups);
	}

	protected void setupComponent() {

		addStyleName("t4j-left-menu");
		setSpacing(true);
	}

	public void addLeftMenuGroup(T4JLeftMenuGroup group) {

		if (null == group) {

			return;
		} else {

			getLeftMenuGroups().add(group);
		}
	}

	@Override
	public void attach() {

		removeAllComponents();

		if (false == T4JCollectionUtils.isNullOrEmpty(leftMenuGroups)) {

			for (Iterator<T4JLeftMenuGroup> iterator = leftMenuGroups.iterator(); iterator.hasNext();) {
				addComponent(iterator.next());
			}
		}

		super.attach();
	}

	public List<T4JLeftMenuGroup> getLeftMenuGroups() {

		if (null == leftMenuGroups) {

			leftMenuGroups = new LinkedList<T4JLeftMenuGroup>();
		}

		return leftMenuGroups;
	}

	public void setLeftMenuGroups(List<T4JLeftMenuGroup> leftMenuGroups) {

		if (T4JCollectionUtils.isNullOrEmpty(leftMenuGroups)) {
			this.leftMenuGroups = new LinkedList<T4JLeftMenuGroup>();
		} else {
			this.leftMenuGroups = new LinkedList<T4JLeftMenuGroup>(leftMenuGroups);
		}
	}

}
