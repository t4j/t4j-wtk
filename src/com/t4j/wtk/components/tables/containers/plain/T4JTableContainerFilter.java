
package com.t4j.wtk.components.tables.containers.plain;

import java.io.Serializable;

import com.vaadin.ui.Field;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public interface T4JTableContainerFilter extends Serializable {

	public void clearField(Field field);

	public Field generateField();

	public Object generateWhereClause(Field field);

	public Object getFormPropertyId();
}
