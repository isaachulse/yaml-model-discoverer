package com.isaachulse.yamlmodeldiscoverer.model;

import java.math.BigInteger;

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

	private static boolean isIntegral(YamlPrimitive primitive) {
		if (primitive.value instanceof Number) {
			Number number = (Number) primitive.value;
			return number instanceof BigInteger || number instanceof Long || number instanceof Integer
					|| number instanceof Short || number instanceof Byte;
		}
		return false;
	}
}