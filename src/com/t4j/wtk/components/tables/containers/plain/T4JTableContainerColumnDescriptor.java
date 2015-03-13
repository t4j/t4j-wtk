
package com.t4j.wtk.components.tables.containers.plain;

import java.io.Serializable;

import com.vaadin.ui.Table.ColumnGenerator;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JTableContainerColumnDescriptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private ColumnGenerator columnGenerator;

	private String excelHeaderText;

	private T4JTableContainerFilter filter;

	private String headerText;

	private Boolean isSortable;

	private Serializable propertyId;

	private Class<?> type;

	public T4JTableContainerColumnDescriptor(Object propertyId, Class<?> type, String headerText, String excelHeaderText, ColumnGenerator columnGenerator) {

		this(propertyId, type, headerText, excelHeaderText, columnGenerator, null, Boolean.FALSE);
	}

	public T4JTableContainerColumnDescriptor(Object propertyId, Class<?> type, String headerText, String excelHeaderText, ColumnGenerator columnGenerator, Boolean isSortable) {

		this(propertyId, type, headerText, excelHeaderText, columnGenerator, null, isSortable);
	}

	public T4JTableContainerColumnDescriptor(Object propertyId, Class<?> type, String headerText, String excelHeaderText, ColumnGenerator columnGenerator, T4JTableContainerFilter filter) {

		this(propertyId, type, headerText, excelHeaderText, columnGenerator, filter, null);
	}

	public T4JTableContainerColumnDescriptor(Object propertyId, Class<?> type, String headerText, String excelHeaderText, ColumnGenerator columnGenerator, T4JTableContainerFilter filter, Boolean isSortable) {

		super();

		if (null == propertyId) {

			throw new IllegalArgumentException();
		}

		if (null == type) {

			throw new IllegalArgumentException();
		}

		if (null == headerText) {

			headerText = propertyId.toString();
		}

		if (null == excelHeaderText) {

			excelHeaderText = headerText.toString();
		}

		this.propertyId = (Serializable) propertyId;
		this.type = type;
		this.headerText = headerText;
		this.excelHeaderText = excelHeaderText;
		this.columnGenerator = columnGenerator;
		this.filter = filter;
		this.isSortable = isSortable;
	}

	public ColumnGenerator getColumnGenerator() {

		return columnGenerator;
	}

	public String getExcelHeaderText() {

		return excelHeaderText;
	}

	public T4JTableContainerFilter getFilter() {

		return filter;
	}

	public String getHeaderText() {

		return headerText;
	}

	public Boolean getIsSortable() {

		return isSortable;
	}

	public Serializable getPropertyId() {

		return propertyId;
	}

	public Class<?> getType() {

		return type;
	}

}
