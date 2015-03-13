
package com.t4j.wtk.components.tables.containers.plain;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JStringUtils;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JQueryBuilderFilterByItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String filterClause;

	private String fromClauseExtension;

	private LinkedHashMap<String, Object> filterParameters;

	public T4JQueryBuilderFilterByItem(String filterClause) {

		this(filterClause, null);
	}

	public T4JQueryBuilderFilterByItem(String filterClause, Map<String, Object> filterParameters) {

		super();

		if (T4JStringUtils.isNullOrEmpty(filterClause)) {

			throw new IllegalArgumentException();
		}

		this.filterClause = filterClause;

		if (T4JCollectionUtils.isNullOrEmpty(filterParameters)) {
			this.filterParameters = new LinkedHashMap<String, Object>();
		} else {
			this.filterParameters = new LinkedHashMap<String, Object>(filterParameters);
		}
	}

	public void addParameter(String key, Object value) {

		if (null == filterParameters) {

			filterParameters = new LinkedHashMap<String, Object>();
		}

		if (T4JStringUtils.isNullOrEmpty(key)) {

			throw new IllegalArgumentException();
		}

		if (null == value) {

			throw new IllegalArgumentException();
		}

		filterParameters.put(key, value);
	}

	public String getFilterClause() {

		return filterClause;
	}

	public Map<String, Object> getFilterParameters() {

		if (null == filterParameters) {

			filterParameters = new LinkedHashMap<String, Object>();
		}

		return filterParameters;
	}

	public String getFromClauseExtension() {

		return fromClauseExtension;
	}

	public void setFromClauseExtension(String fromClauseExtension) {

		this.fromClauseExtension = fromClauseExtension;
	}
}
