
package com.t4j.wtk.components.tables.containers.plain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
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
public abstract class T4JTableContainerSQL extends T4JTableContainer {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JTableContainerSQL.class);

	private T4JQueryBuilderSQL nativeQueryBuilder;

	public T4JTableContainerSQL(T4JQueryBuilderSQL nativeQueryBuilder) {

		this(nativeQueryBuilder, Integer.valueOf(10));
	}

	public T4JTableContainerSQL(T4JQueryBuilderSQL nativeQueryBuilder, Integer itemsPerPage) {

		super(itemsPerPage);

		if (null == nativeQueryBuilder) {

			throw new IllegalArgumentException();
		}

		this.nativeQueryBuilder = nativeQueryBuilder;
	}

	private EntityManager getInternalEntityManager() {

		EntityManager methodResult = null;

		if (null == excelExportEntityManager || false == excelExportEntityManager.isOpen()) {

			methodResult = getEntityManager();
		} else {

			methodResult = excelExportEntityManager;
		}

		if (null == methodResult || false == methodResult.isOpen()) {

			methodResult = getEntityManager();
		}

		return methodResult;
	}

	protected abstract T4JTableContainerItem<?> generateItem(Object resultSetItem);

	protected abstract String generateOrderByFieldClause(Object propertyId, Boolean isAscending);

	protected abstract List<T4JQueryBuilderOrderByItem> getDefaultOrderByItems();

	protected abstract EntityManager getEntityManager();

	protected void setupDefaultOrderByItems() {

		nativeQueryBuilder.getOrderByItems().clear();

		List<T4JQueryBuilderOrderByItem> defaultOrderByItems = getDefaultOrderByItems();

		if (false == T4JCollectionUtils.isNullOrEmpty(defaultOrderByItems)) {

			for (Iterator<T4JQueryBuilderOrderByItem> iterator = defaultOrderByItems.iterator(); iterator.hasNext();) {

				T4JQueryBuilderOrderByItem orderByItem = iterator.next();

				if (null == orderByItem) {

					continue;
				}

				nativeQueryBuilder.getOrderByItems().add(orderByItem);
			}
		}
	}

	@Override
	public Integer getCount(Object filterParameters) {

		try {

			if (null == filterParameters || false == filterParameters instanceof List<?>) {

				nativeQueryBuilder.getFilterByItems().clear();
			} else {

				nativeQueryBuilder.getFilterByItems().clear();

				List<?> filterParametersList = (List<?>) filterParameters;

				if (false == filterParametersList.isEmpty()) {

					for (Iterator<?> iterator = filterParametersList.iterator(); iterator.hasNext();) {

						T4JQueryBuilderFilterByItem filterByItem = (T4JQueryBuilderFilterByItem) iterator.next();

						if (null == filterByItem) {

							continue;
						}

						nativeQueryBuilder.addFilterByItem(filterByItem);
					}
				}
			}

			Query jpaQuery = getInternalEntityManager().createNativeQuery(nativeQueryBuilder.generateCountQuery());
			nativeQueryBuilder.setQueryParameters(jpaQuery);
			Number tmpNumber = (Number) jpaQuery.getSingleResult();
			return Integer.valueOf(tmpNumber.intValue());
		} catch (Exception e) {
			logger.error(e, e);
			return Integer.valueOf(0);
		}
	}

	@Override
	public List<T4JTableContainerItem<?>> getPageItems(Object filterParameters, Object sortParameters, Integer firstIndex, Integer pageSize) {

		try {

			if (null == sortParameters || false == sortParameters instanceof List<?>) {

				setupDefaultOrderByItems();
			} else {

				List<?> sortParametersList = (List<?>) sortParameters;

				if (sortParametersList.isEmpty()) {

					setupDefaultOrderByItems();
				} else {

					nativeQueryBuilder.getOrderByItems().clear();

					for (Iterator<?> iterator = sortParametersList.iterator(); iterator.hasNext();) {

						T4JQueryBuilderOrderByItem orderByItem = (T4JQueryBuilderOrderByItem) iterator.next();

						if (null == orderByItem) {

							continue;
						}

						nativeQueryBuilder.addOrderByItem(orderByItem);
					}
				}
			}

			Query jpaQuery = getInternalEntityManager().createNativeQuery(nativeQueryBuilder.generatePageItemsQuery());

			jpaQuery.setFirstResult(firstIndex.intValue());
			jpaQuery.setMaxResults(pageSize.intValue());

			nativeQueryBuilder.setQueryParameters(jpaQuery);

			List<?> tmpList = jpaQuery.getResultList();

			List<T4JTableContainerItem<?>> pageItems = new ArrayList<T4JTableContainerItem<?>>();

			for (Iterator<?> iterator = tmpList.iterator(); iterator.hasNext();) {

				pageItems.add(generateItem(iterator.next()));
			}

			return pageItems;
		} catch (Exception e) {
			logger.error(e, e);
			return new ArrayList<T4JTableContainerItem<?>>();
		}
	}

	public T4JQueryBuilderJPA getQueryBuilder() {

		return nativeQueryBuilder;
	}

	public void resetExcelExportEntityManager() {

		excelExportEntityManager = null;
	}

	public void setupExcelExportEntityManager() {

		excelExportEntityManager = getEntityManager();
	}

	@Override
	public void sort(Object[] propertyIds, boolean[] ascending) {

		ArrayList<T4JQueryBuilderOrderByItem> orderByItems = new ArrayList<T4JQueryBuilderOrderByItem>();

		try {

			for (int i = 0; i < propertyIds.length; i++) {

				Object propertyId = propertyIds[i];
				Boolean isAscending = Boolean.valueOf(ascending[i]);

				String fieldClause = generateOrderByFieldClause(propertyId, isAscending);

				if (false == T4JStringUtils.isNullOrEmpty(fieldClause)) {

					T4JQueryBuilderOrderByItem orderByItem = new T4JQueryBuilderOrderByItem(fieldClause, isAscending);
					orderByItems.add(orderByItem);
				}
			}
		} catch (Exception e) {
			orderByItems.clear();
		}

		setSortParameters(orderByItems);
		setFirstItemIndex(null);

		tableComponent.setCurrentPageIndex(null);
	}
}
