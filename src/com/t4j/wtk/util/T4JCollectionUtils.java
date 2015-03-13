
package com.t4j.wtk.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.w3c.dom.NodeList;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JCollectionUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	private T4JCollectionUtils() {

		super();
	}

	public static boolean isNullOrEmpty(boolean[] a) {

		if (null == a) {
			return true;
		} else {
			if (0 == a.length) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(byte[] a) {

		if (null == a) {
			return true;
		} else {
			if (0 == a.length) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(char[] a) {

		if (null == a) {
			return true;
		} else {
			if (0 == a.length) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(Collection<?> c) {

		if (null == c) {
			return true;
		} else {
			if (c.isEmpty() || 0 == c.size()) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(double[] a) {

		if (null == a) {
			return true;
		} else {
			if (0 == a.length) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(float[] a) {

		if (null == a) {
			return true;
		} else {
			if (0 == a.length) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(int[] a) {

		if (null == a) {
			return true;
		} else {
			if (0 == a.length) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(long[] a) {

		if (null == a) {
			return true;
		} else {
			if (0 == a.length) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(Map<?, ?> m) {

		if (null == m) {
			return true;
		} else {
			if (m.isEmpty() || 0 == m.size()) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(NodeList n) {

		if (null == n) {
			return true;
		} else {
			if (0 == n.getLength()) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(Object[] a) {

		if (null == a) {
			return true;
		} else {
			if (0 == a.length) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isNullOrEmpty(short[] a) {

		if (null == a) {
			return true;
		} else {
			if (0 == a.length) {
				return true;
			} else {
				return false;
			}
		}
	}

}
