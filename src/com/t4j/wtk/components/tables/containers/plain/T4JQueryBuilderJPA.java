
package com.t4j.wtk.components.tables.containers.plain;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.logging.Log;

import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JQueryBuilderJPA implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JQueryBuilderJPA.class);

	private String countSelectClause;

	private String pageItemsSelectClause;

	private String fromClause;

	private LinkedList<T4JQueryBuilderFilterByItem> coreFilterByItems;

	private LinkedList<T4JQueryBuilderFilterByItem> filterByItems;

	private LinkedList<T4JQueryBuilderGroupByItem> groupByItems;

	private LinkedList<T4JQueryBuilderHavingItem> havingItems;

	private LinkedList<T4JQueryBuilderOrderByItem> orderByItems;

	public T4JQueryBuilderJPA(String fromClause, String countSelectClause, String pageItemsSelectClause) {

		super();

		if (T4JStringUtils.isNullOrEmpty(fromClause) || T4JStringUtils.isNullOrEmpty(countSelectClause) || T4JStringUtils.isNullOrEmpty(pageItemsSelectClause)) {

			throw new IllegalArgumentException();
		}

		this.fromClause = fromClause;
		this.countSelectClause = countSelectClause;
		this.pageItemsSelectClause = pageItemsSelectClause;

		coreFilterByItems = new LinkedList<T4JQueryBuilderFilterByItem>();
		filterByItems = new LinkedList<T4JQueryBuilderFilterByItem>();
		groupByItems = new LinkedList<T4JQueryBuilderGroupByItem>();
		havingItems = new LinkedList<T4JQueryBuilderHavingItem>();
		orderByItems = new LinkedList<T4JQueryBuilderOrderByItem>();
	}

	public void addCoreFilterByItem(T4JQueryBuilderFilterByItem item) {

		if (null == item) {

			return;
		} else {

			coreFilterByItems.add(item);
		}
	}

	public void addFilterByItem(T4JQueryBuilderFilterByItem item) {

		if (null == item) {

			return;
		} else {

			filterByItems.add(item);
		}
	}

	public void addGroupByItem(T4JQueryBuilderGroupByItem item) {

		if (null == item) {

			return;
		} else {

			groupByItems.add(item);
		}
	}

	public void addHavingItem(T4JQueryBuilderHavingItem item) {

		if (null == item) {

			return;
		} else {

			havingItems.add(item);
		}
	}

	public void addOrderByItem(T4JQueryBuilderOrderByItem item) {

		if (null == item) {

			return;
		} else {

			orderByItems.add(item);
		}
	}

	public String generateCountQuery() {

		return "select count(" + countSelectClause + ") " + generateFromQuery(false);
	}

	public String generateFromQuery(boolean includeSortClause) {

		return generateFromQuery(true, includeSortClause);
	}

	public String generateFromQuery(boolean includeGroupClause, boolean includeSortClause) {

		StringBuffer fromClauseBuffer = new StringBuffer(1024);
		StringBuffer whereClauseBuffer = new StringBuffer(1024);

		fromClauseBuffer.append(fromClause);

		boolean hasWhereClause = false;

		if (false == T4JCollectionUtils.isNullOrEmpty(coreFilterByItems)) {

			hasWhereClause = true;

			whereClauseBuffer = new StringBuffer(1024);

			boolean isFirstIteration = true;

			for (Iterator<T4JQueryBuilderFilterByItem> iterator = coreFilterByItems.iterator(); iterator.hasNext();) {

				T4JQueryBuilderFilterByItem item = iterator.next();

				if (false == T4JStringUtils.isNullOrEmpty(item.getFromClauseExtension())) {

					fromClauseBuffer.append(", ");
					fromClauseBuffer.append(item.getFromClauseExtension());
				}

				if (isFirstIteration) {
					isFirstIteration = false;
					whereClauseBuffer.append(item.getFilterClause());
				} else {
					whereClauseBuffer.append(" and ");
					whereClauseBuffer.append(item.getFilterClause());
				}
			}
		}

		if (false == T4JCollectionUtils.isNullOrEmpty(filterByItems)) {

			boolean isFirstIteration = true;

			if (hasWhereClause) {

				isFirstIteration = false;
			} else {

				hasWhereClause = true;
				whereClauseBuffer = new StringBuffer(1024);
			}

			for (Iterator<T4JQueryBuilderFilterByItem> iterator = filterByItems.iterator(); iterator.hasNext();) {

				T4JQueryBuilderFilterByItem item = iterator.next();

				if (false == T4JStringUtils.isNullOrEmpty(item.getFromClauseExtension())) {

					fromClauseBuffer.append(", ");
					fromClauseBuffer.append(item.getFromClauseExtension());
				}

				if (isFirstIteration) {
					isFirstIteration = false;
					whereClauseBuffer.append(item.getFilterClause());
				} else {
					whereClauseBuffer.append(" and ");
					whereClauseBuffer.append(item.getFilterClause());
				}
			}
		}

		boolean hasGroupClause = false;
		boolean hasHavingClause = false;

		StringBuffer groupClauseBuffer = null;
		StringBuffer havingClauseBuffer = null;

		if (includeGroupClause && false == T4JCollectionUtils.isNullOrEmpty(groupByItems)) {

			hasGroupClause = true;

			groupClauseBuffer = new StringBuffer(1024);

			boolean isFirstGroupByIteration = true;

			for (Iterator<T4JQueryBuilderGroupByItem> iterator = groupByItems.iterator(); iterator.hasNext();) {

				T4JQueryBuilderGroupByItem item = iterator.next();

				if (isFirstGroupByIteration) {
					isFirstGroupByIteration = false;
				} else {
					groupClauseBuffer.append(", ");
				}

				groupClauseBuffer.append(item.getFieldClause());
			}

			if (false == T4JCollectionUtils.isNullOrEmpty(havingItems)) {

				hasHavingClause = true;

				boolean isFirstHavingIteration = true;

				havingClauseBuffer = new StringBuffer(1024);

				for (Iterator<T4JQueryBuilderHavingItem> iterator = havingItems.iterator(); iterator.hasNext();) {

					T4JQueryBuilderHavingItem item = iterator.next();

					if (isFirstHavingIteration) {
						isFirstHavingIteration = false;
						havingClauseBuffer.append(item.getFilterClause());
					} else {
						havingClauseBuffer.append(" and ");
						havingClauseBuffer.append(item.getFilterClause());
					}
				}
			}
		}

		boolean hasSortClause = false;

		StringBuffer sortClauseBuffer = null;

		if (includeSortClause && false == T4JCollectionUtils.isNullOrEmpty(orderByItems)) {

			hasSortClause = true;

			sortClauseBuffer = new StringBuffer(1024);

			boolean isFirstIteration = true;

			for (Iterator<T4JQueryBuilderOrderByItem> iterator = orderByItems.iterator(); iterator.hasNext();) {

				T4JQueryBuilderOrderByItem item = iterator.next();

				if (isFirstIteration) {
					isFirstIteration = false;
				} else {
					sortClauseBuffer.append(", ");
				}

				sortClauseBuffer.append(item.getFieldClause());

				if (Boolean.FALSE.equals(item.getIsAscending())) {
					sortClauseBuffer.append(" desc");
				}
			}
		}

		StringBuffer stringBuffer = new StringBuffer(4096);

		stringBuffer.append(" from ");
		stringBuffer.append(fromClauseBuffer);

		if (hasWhereClause) {
			stringBuffer.append(" where ");
			stringBuffer.append(whereClauseBuffer);
		}

		if (includeGroupClause && hasGroupClause) {

			stringBuffer.append(" group by ");
			stringBuffer.append(groupClauseBuffer);

			if (hasHavingClause) {

				stringBuffer.append(" having ");
				stringBuffer.append(havingClauseBuffer);
			}
		}

		if (includeSortClause && hasSortClause) {
			stringBuffer.append(" order by ");
			stringBuffer.append(sortClauseBuffer);
		}

		String tmpString = stringBuffer.toString();

		if (logger.isTraceEnabled()) {

			logger.trace(tmpString);
		}

		return tmpString;
	}

	public String generatePageItemsQuery() {

		return generatePageItemsQuery(true);
	}

	public String generatePageItemsQuery(boolean includeSortClause) {

		return "select " + pageItemsSelectClause + generateFromQuery(includeSortClause);
	}

	public List<T4JQueryBuilderFilterByItem> getCoreFilterByItems() {

		return coreFilterByItems;
	}

	public String getCountSelectClause() {

		return countSelectClause;
	}

	public List<T4JQueryBuilderFilterByItem> getFilterByItems() {

		return filterByItems;
	}

	public String getFromClause() {

		return fromClause;
	}

	public List<T4JQueryBuilderGroupByItem> getGroupByItems() {

		return groupByItems;
	}

	public List<T4JQueryBuilderHavingItem> getHavingItems() {

		return havingItems;
	}

	public List<T4JQueryBuilderOrderByItem> getOrderByItems() {

		return orderByItems;
	}

	public String getPageItemsSelectClause() {

		return pageItemsSelectClause;
	}

	public void setQueryParameters(Query query) {

		if (false == T4JCollectionUtils.isNullOrEmpty(coreFilterByItems)) {

			for (Iterator<T4JQueryBuilderFilterByItem> i1 = coreFilterByItems.iterator(); i1.hasNext();) {

				T4JQueryBuilderFilterByItem item = i1.next();

				if (false == item.getFilterParameters().isEmpty()) {

					Set<String> keySet = item.getFilterParameters().keySet();

					for (Iterator<String> i2 = keySet.iterator(); i2.hasNext();) {
						String key = i2.next();
						query.setParameter(key, item.getFilterParameters().get(key));
					}
				}
			}
		}

		if (false == T4JCollectionUtils.isNullOrEmpty(filterByItems)) {

			for (Iterator<T4JQueryBuilderFilterByItem> i1 = filterByItems.iterator(); i1.hasNext();) {

				T4JQueryBuilderFilterByItem item = i1.next();

				if (false == item.getFilterParameters().isEmpty()) {

					Set<String> keySet = item.getFilterParameters().keySet();

					for (Iterator<String> i2 = keySet.iterator(); i2.hasNext();) {
						String key = i2.next();
						query.setParameter(key, item.getFilterParameters().get(key));
					}
				}
			}
		}

		if (false == T4JCollectionUtils.isNullOrEmpty(havingItems)) {

			for (Iterator<T4JQueryBuilderHavingItem> i1 = havingItems.iterator(); i1.hasNext();) {

				T4JQueryBuilderHavingItem item = i1.next();

				if (false == item.getFilterParameters().isEmpty()) {

					Set<String> keySet = item.getFilterParameters().keySet();

					for (Iterator<String> i2 = keySet.iterator(); i2.hasNext();) {
						String key = i2.next();
						query.setParameter(key, item.getFilterParameters().get(key));
					}
				}
			}
		}
	}
}
