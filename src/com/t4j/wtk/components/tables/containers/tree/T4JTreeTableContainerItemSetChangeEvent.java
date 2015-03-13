
package com.t4j.wtk.components.tables.containers.tree;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JTreeTableContainerItemSetChangeEvent implements ItemSetChangeEvent {

	private static final long serialVersionUID = 1L;

	private T4JTreeTableContainer container;

	public T4JTreeTableContainerItemSetChangeEvent(T4JTreeTableContainer container) {

		super();

		this.container = container;
	}

	public Container getContainer() {

		return container;
	}

}
