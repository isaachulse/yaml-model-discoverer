package com.isaachulse.yamlmodeldiscoverer.model;

public abstract class YamlElement {

	public boolean isYamlArray() {
		return this instanceof YamlArray;
	}

	public boolean isYamlObject() {
		return this instanceof YamlObject;
	}

	public boolean isYamlPrimitive() {
		return this instanceof YamlPrimitive;
	}

	public boolean isYamlNull() {
		return this instanceof YamlNull;
	}

	public YamlObject getAsYamlObject() {
		if (isYamlObject()) {
			return (YamlObject) this;
		}
		throw new IllegalStateException("Not a YAML Object: " + this);
	}

	public YamlArray getAsYamlArray() {
		if (isYamlArray()) {
			return (YamlArray) this;
		}
		throw new IllegalStateException("Not a YAML Array: " + this);
	}

	public YamlPrimitive getAsYamlPrimitive() {
		if (isYamlPrimitive()) {
			return (YamlPrimitive) this;
		}
		throw new IllegalStateException("Not a YAML Primitive: " + this);
	}

	public YamlNull getAsYamlNull() {
		if (isYamlNull()) {
			return (YamlNull) this;
		}
		throw new IllegalStateException("Not a YAML Null: " + this);
	}

	Boolean getAsBooleanWrapper() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public boolean getAsBoolean() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

}