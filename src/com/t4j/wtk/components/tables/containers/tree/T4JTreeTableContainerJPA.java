
package com.t4j.wtk.components.tables.containers.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;

import com.t4j.wtk.components.tables.containers.plain.T4JTableContainerItem;
import com.t4j.wtk.components.tables.containers.plain.T4JQueryBuilderJPA;
import com.t4j.wtk.components.tables.containers.plain.T4JQueryBuilderFilterByItem;
import com.t4j.wtk.components.tables.containers.plain.T4JQueryBuilderOrderByItem;
import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public abstract class T4JTreeTableContainerJPA extends T4JTreeTableContainer {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JTreeTableContainerJPA.class);

	private T4JQueryBuilderJPA queryBuilder;

	public T4JTreeTableContainerJPA(T4JQueryBuilderJPA queryBuilder) {

		this(queryBuilder, Integer.valueOf(10));
	}

	public T4JTreeTableContainerJPA(T4JQueryBuilderJPA queryBuilder, Integer itemsPerPage) {

		super(itemsPerPage);

		if (null == queryBuilder) {

			throw new IllegalArgumentException();
		}

		this.queryBuilder = queryBuilder;
	}

	protected abstract T4JTableContainerItem<?> generateItem(Object resultSetItem);

	protected abstract Object generateItemId(Object resultSetItem);

	protected abstract String generateOrderByFieldClause(Object propertyId, Boolean isAscending);

	protected abstract List<T4JQueryBuilderOrderByItem> getDefaultOrderByItems();

	protected abstract EntityManager getEntityManager();

	@Override
	public Integer getCount(Object filterParameters) {

		try {

			if (null == filterParameters) {

				queryBuilder.getFilterByItems().clear();
			} else {

				if (filterParameters instanceof List<?>) {

					try {

						List<?> tmpList = (List<?>) filterParameters;

						queryBuilder.getFilterByItems().clear();

						if (false == tmpList.isEmpty()) {

							for (Iterator<?> iterator = tmpList.iterator(); iterator.hasNext();) {

								T4JQueryBuilderFilterByItem filterByItem = (T4JQueryBuilderFilterByItem) iterator.next();
								queryBuilder.addFilterByItem(filterByItem);
							}
						}
					} catch (Exception e) {

						queryBuilder.getFilterByItems().clear();
					}
				} else {

					queryBuilder.getFilterByItems().clear();
				}
			}

			Query jpaQuery = getEntityManager().createQuery(queryBuilder.generateCountQuery());
			queryBuilder.setQueryParameters(jpaQuery);
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

			if (null == filterParameters) {

				queryBuilder.getFilterByItems().clear();
			} else {

				if (filterParameters instanceof List<?>) {

					try {

						List<?> tmpList = (List<?>) filterParameters;

						queryBuilder.getFilterByItems().clear();

						if (false == tmpList.isEmpty()) {

							for (Iterator<?> iterator = tmpList.iterator(); iterator.hasNext();) {

								T4JQueryBuilderFilterByItem filterByItem = (T4JQueryBuilderFilterByItem) iterator.next();
								queryBuilder.addFilterByItem(filterByItem);
							}
						}
					} catch (Exception e) {

						queryBuilder.getFilterByItems().clear();
					}
				} else {

					queryBuilder.getFilterByItems().clear();
				}
			}

			if (null == sortParameters) {

				List<T4JQueryBuilderOrderByItem> defaultOrderByItems = getDefaultOrderByItems();

				queryBuilder.getOrderByItems().clear();

				if (false == T4JCollectionUtils.isNullOrEmpty(defaultOrderByItems)) {

					queryBuilder.getOrderByItems().clear();

					for (Iterator<T4JQueryBuilderOrderByItem> iterator = defaultOrderByItems.iterator(); iterator.hasNext();) {

						Object tmpItem = iterator.next();

						if (null == tmpItem) {

							continue;
						} else if (tmpItem instanceof T4JQueryBuilderOrderByItem) {

							T4JQueryBuilderOrderByItem orderByItem = (T4JQueryBuilderOrderByItem) tmpItem;
							queryBuilder.getOrderByItems().add(orderByItem);
						}
					}
				}
			} else {

				if (sortParameters instanceof List<?>) {

					try {

						List<?> tmpList = (List<?>) sortParameters;

						queryBuilder.getOrderByItems().clear();

						if (false == T4JCollectionUtils.isNullOrEmpty(tmpList)) {

							for (Iterator<?> iterator = tmpList.iterator(); iterator.hasNext();) {

								Object tmpItem = iterator.next();

								if (null == tmpItem) {

									continue;
								} else if (tmpItem instanceof T4JQueryBuilderOrderByItem) {

									T4JQueryBuilderOrderByItem orderByItem = (T4JQueryBuilderOrderByItem) tmpItem;
									queryBuilder.addOrderByItem(orderByItem);
								}
							}
						}
					} catch (Exception e) {

						queryBuilder.getOrderByItems().clear();
					}
				} else {

					queryBuilder.getOrderByItems().clear();
				}
			}

			Query jpaQuery = getEntityManager().createQuery(queryBuilder.generatePageItemsQuery());

			jpaQuery.setFirstResult(firstIndex.intValue());
			jpaQuery.setMaxResults(pageSize.intValue());

			queryBuilder.setQueryParameters(jpaQuery);

			List<?> tmpList = jpaQuery.getResultList();

			List<T4JTableContainerItem<?>> pageItems = new ArrayList<T4JTableContainerItem<?>>();

			for (Iterator<?> iterator = tmpList.iterator(); iterator.hasNext();) {

				Object tmpObject = iterator.next();
				T4JTableContainerItem<?> tmpItem = generateItem(tmpObject);
				pageItems.add(tmpItem);
			}

			return pageItems;
		} catch (Exception e) {
			logger.error(e, e);
			return new ArrayList<T4JTableContainerItem<?>>();
		}
	}

	public T4JQueryBuilderJPA getQueryBuilder() {

		return queryBuilder;
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

		treeTableComponent.setCurrentPageIndex(null);
	}
}
