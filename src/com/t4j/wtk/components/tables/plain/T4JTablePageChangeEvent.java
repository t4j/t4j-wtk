
package com.t4j.wtk.components.tables.plain;

import com.vaadin.ui.Component.Event;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JTablePageChangeEvent extends Event {

	private static final long serialVersionUID = 1L;

	private T4JTable table;

	public T4JTablePageChangeEvent(T4JTable table) {

		super(table);

		this.table = table;
	}

	public T4JTable getTable() {

		return table;
	}

}
