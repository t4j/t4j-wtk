
package com.t4j.wtk.beans;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Contenido serializado de una petición HTTP (Cabeceras, Parametros, etc...)
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JHttpRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private byte[] requestBytes;

	private int contentLength;

	private String characterEncoding;

	private String contentType;

	private String httpMethod;

	private String requestURI;

	private LinkedList<String> handlerClassNames;

	private LinkedList<T4JKeyValuePair> requestHeaders;

	private LinkedList<T4JKeyValuePair> requestParameters;

	public T4JHttpRequest() {

		super();

		handlerClassNames = new LinkedList<String>();
		requestHeaders = new LinkedList<T4JKeyValuePair>();
		requestParameters = new LinkedList<T4JKeyValuePair>();
	}

	public String getCharacterEncoding() {

		return characterEncoding;
	}

	public int getContentLength() {

		return contentLength;
	}

	public String getContentType() {

		return contentType;
	}

	public List<String> getHandlerClassNames() {

		return handlerClassNames;
	}

	public String getHttpMethod() {

		return httpMethod;
	}

	public byte[] getRequestBytes() {

		return requestBytes;
	}

	public List<T4JKeyValuePair> getRequestHeaders() {

		return requestHeaders;
	}

	public List<T4JKeyValuePair> getRequestParameters() {

		return requestParameters;
	}

	public String getRequestURI() {

		return requestURI;
	}

	public void setCharacterEncoding(String characterEncoding) {

		this.characterEncoding = characterEncoding;
	}

	public void setContentLength(int contentLength) {

		this.contentLength = contentLength;
	}

	public void setContentType(String contentType) {

		this.contentType = contentType;
	}

	public void setHandlerClassNames(List<String> handlerClassNames) {

		this.handlerClassNames = new LinkedList<String>(handlerClassNames);
	}

	public void setHttpMethod(String httpMethod) {

		this.httpMethod = httpMethod;
	}

	public void setRequestBytes(byte[] requestBytes) {

		this.requestBytes = requestBytes;
	}

	public void setRequestHeaders(List<T4JKeyValuePair> requestHeaders) {

		this.requestHeaders = new LinkedList<T4JKeyValuePair>(requestHeaders);
	}

	public void setRequestParameters(List<T4JKeyValuePair> requestParameters) {

		this.requestParameters = new LinkedList<T4JKeyValuePair>(requestParameters);
	}

	public void setRequestURI(String requestURI) {

		this.requestURI = requestURI;
	}

}
