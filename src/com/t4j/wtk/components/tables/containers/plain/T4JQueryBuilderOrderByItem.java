
package com.t4j.wtk.components.tables.containers.plain;

import java.io.Serializable;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JQueryBuilderOrderByItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private Boolean isAscending;

	private String fieldClause;

	public T4JQueryBuilderOrderByItem(String fieldClause, Boolean isAscending) {

		super();
		this.fieldClause = fieldClause;
		this.isAscending = isAscending;
	}

	public String getFieldClause() {

		return fieldClause;
	}

	public Boolean getIsAscending() {

		return isAscending;
	}

	public void setFieldClause(String fieldClause) {

		this.fieldClause = fieldClause;
	}

	public void setIsAscending(Boolean isAscending) {

		this.isAscending = isAscending;
	}
}
