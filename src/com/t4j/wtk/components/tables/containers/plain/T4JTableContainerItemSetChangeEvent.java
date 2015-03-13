
package com.t4j.wtk.components.tables.containers.plain;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JTableContainerItemSetChangeEvent implements ItemSetChangeEvent {

	private static final long serialVersionUID = 1L;

	private T4JTableContainer container;

	public T4JTableContainerItemSetChangeEvent(T4JTableContainer container) {

		super();

		this.container = container;
	}

	public Container getContainer() {

		return container;
	}

}
