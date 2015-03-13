
package com.t4j.wtk.components.tables.tree;

import com.vaadin.ui.Component.Event;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JTreeTablePageChangeEvent extends Event {

	private static final long serialVersionUID = 1L;

	private T4JTreeTable table;

	public T4JTreeTablePageChangeEvent(T4JTreeTable table) {

		super(table);

		this.table = table;
	}

	public T4JTreeTable getTable() {

		return table;
	}
}
