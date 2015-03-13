
package com.t4j.wtk.components.tables.containers.plain;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import com.t4j.wtk.util.T4JCollectionUtils;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JQueryBuilderSQL extends T4JQueryBuilderJPA {

	private static final long serialVersionUID = 1L;

	public T4JQueryBuilderSQL(String fromClause, String countSelectClause, String pageItemsSelectClause) {

		super(fromClause, countSelectClause, pageItemsSelectClause);
	}

	@Override
	public void setQueryParameters(Query query) {

		int position = 1;

		List<T4JQueryBuilderFilterByItem> coreFilterItems = getCoreFilterByItems();

		if (false == T4JCollectionUtils.isNullOrEmpty(coreFilterItems)) {

			for (Iterator<T4JQueryBuilderFilterByItem> i1 = coreFilterItems.iterator(); i1.hasNext();) {

				T4JQueryBuilderFilterByItem item = i1.next();

				if (false == item.getFilterParameters().isEmpty()) {

					Set<String> keySet = item.getFilterParameters().keySet();

					for (Iterator<String> i2 = keySet.iterator(); i2.hasNext();) {
						String key = i2.next();
						query.setParameter(position, item.getFilterParameters().get(key));
						position++;
					}
				}
			}
		}

		List<T4JQueryBuilderFilterByItem> filterItems = getFilterByItems();

		if (false == T4JCollectionUtils.isNullOrEmpty(filterItems)) {

			for (Iterator<T4JQueryBuilderFilterByItem> i1 = filterItems.iterator(); i1.hasNext();) {

				T4JQueryBuilderFilterByItem item = i1.next();

				if (false == item.getFilterParameters().isEmpty()) {

					Set<String> keySet = item.getFilterParameters().keySet();

					for (Iterator<String> i2 = keySet.iterator(); i2.hasNext();) {
						String key = i2.next();
						query.setParameter(position, item.getFilterParameters().get(key));
						position++;
					}
				}
			}
		}

		List<T4JQueryBuilderHavingItem> havingItems = getHavingItems();

		if (false == T4JCollectionUtils.isNullOrEmpty(havingItems)) {

			for (Iterator<T4JQueryBuilderHavingItem> i1 = havingItems.iterator(); i1.hasNext();) {

				T4JQueryBuilderHavingItem item = i1.next();

				if (false == item.getFilterParameters().isEmpty()) {

					Set<String> keySet = item.getFilterParameters().keySet();

					for (Iterator<String> i2 = keySet.iterator(); i2.hasNext();) {
						String key = i2.next();
						query.setParameter(position, item.getFilterParameters().get(key));
						position++;
					}
				}
			}
		}
	}
}
