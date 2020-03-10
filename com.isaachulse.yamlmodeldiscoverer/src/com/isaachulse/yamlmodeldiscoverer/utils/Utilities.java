package com.isaachulse.yamlmodeldiscoverer.utils;

public class Utilities {

	public static <T> T checkNotNull(T obj) {
		if (obj == null) {
			throw new NullPointerException();
		}
		return obj;
	}
}
