
package com.t4j.wtk.application;

import java.util.Map;

import javax.persistence.EntityManager;

import com.t4j.wtk.beans.T4JKeyValuePair;

public abstract class T4JWebAppJPA extends T4JWebApp {

	private static final long serialVersionUID = 1L;

	public abstract Object getDetachedPersistenceEntity(Object entity) throws Exception;

	public abstract EntityManager getEntityManager(String persistenceUnitName) throws Exception;

	public abstract Object getManagedPersistenceEntity(Object entity) throws Exception;

	public abstract Object getNamedQueryResults(String queryName, Map<String, Object> queryParameters) throws Exception;

	public abstract Object getNamedQueryResults(String queryName, T4JKeyValuePair... queryParameters) throws Exception;

	public abstract Object getNamedQueryResultsWithIndexedParameters(String queryName, Object... queryParameters) throws Exception;

	public abstract Object getNamedQuerySingleResult(String queryName, Map<String, Object> queryParameters) throws Exception;

	public abstract Object getNamedQuerySingleResult(String queryName, T4JKeyValuePair... queryParameters) throws Exception;

	public abstract Object getNamedQuerySingleResultWithIndexedParameters(String queryName, Object... queryParameters) throws Exception;

	public abstract EntityManager getNewEntityManager(String persistenceUnitName) throws Exception;

}
