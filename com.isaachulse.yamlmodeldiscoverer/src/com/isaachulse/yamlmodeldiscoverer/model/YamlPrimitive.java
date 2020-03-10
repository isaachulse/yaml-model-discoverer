package com.isaachulse.yamlmodeldiscoverer.model;

import com.isaachulse.yamlmodeldiscoverer.utils.*;

public final class YamlPrimitive extends YamlElement {

	private final Object value;

	public YamlPrimitive(Boolean bool) {
		value = Utilities.checkNotNull(bool);
	}

	public YamlPrimitive(Number number) {
		value = Utilities.checkNotNull(number);
	}

	public YamlPrimitive(String string) {
		value = Utilities.checkNotNull(string);
	}

	public YamlPrimitive(Character c) {
		value = Utilities.checkNotNull(c).toString();
	}

	public boolean isBoolean() {
		return value instanceof Boolean;
	}

	public boolean isNumber() {
		return value instanceof Number;
	}

	public boolean isString() {
		return value instanceof String;
	}
}