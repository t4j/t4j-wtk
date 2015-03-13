
package com.t4j.wtk.components.tables.containers.plain;

import java.io.Serializable;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JQueryBuilderGroupByItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fieldClause;

	public T4JQueryBuilderGroupByItem(String fieldClause) {

		super();

		this.fieldClause = fieldClause;
	}

	public String getFieldClause() {

		return fieldClause;
	}

	public void setFieldClause(String fieldClause) {

		this.fieldClause = fieldClause;
	}
}
