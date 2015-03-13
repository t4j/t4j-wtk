
package com.t4j.wtk.beans;

import java.io.Serializable;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JKeyValuePair implements Serializable {

	private static final long serialVersionUID = 1L; // 123

	private Serializable key;

	private Serializable value;

	public T4JKeyValuePair() {

		super();
	}

	public T4JKeyValuePair(Serializable key, Serializable value) {

		super();

		this.key = key;
		this.value = value;
	}

	public static T4JKeyValuePair newInstance(Serializable key, Serializable value) {

		return new T4JKeyValuePair(key, value);
	}

	public Serializable getKey() {

		return key;
	}

	public Serializable getValue() {

		return value;
	}

	public void setKey(Serializable key) {

		this.key = key;
	}

	public void setValue(Serializable value) {

		this.value = value;
	}

}
