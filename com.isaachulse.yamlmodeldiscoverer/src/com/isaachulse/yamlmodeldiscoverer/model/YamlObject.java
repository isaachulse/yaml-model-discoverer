package com.isaachulse.yamlmodeldiscoverer.model;

import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class YamlObject extends YamlElement {
	private final Map<String, YamlElement> members = new HashMap<String, YamlElement>();

	public void add(String property, YamlElement value) {
		members.put(property, value == null ? YamlNull.INSTANCE : value);
	}

	public YamlElement remove(String property) {
		return members.remove(property);
	}

	public void addProperty(String property, String value) {
		add(property, value == null ? YamlNull.INSTANCE : new YamlPrimitive(value));
	}

	public void addProperty(String property, Number value) {
		add(property, value == null ? YamlNull.INSTANCE : new YamlPrimitive(value));
	}

	public void addProperty(String property, Boolean value) {
		add(property, value == null ? YamlNull.INSTANCE : new YamlPrimitive(value));
	}

	public void addProperty(String property, Character value) {
		add(property, value == null ? YamlNull.INSTANCE : new YamlPrimitive(value));
	}

	public Set<Map.Entry<String, YamlElement>> entrySet() {
		return members.entrySet();
	}

	public Set<String> keySet() {
		return members.keySet();
	}

	public int size() {
		return members.size();
	}

	public boolean has(String memberName) {
		return members.containsKey(memberName);
	}

	public YamlElement get(String memberName) {
		return members.get(memberName);
	}

	public YamlPrimitive getAsYamlPrimitive(String memberName) {
		return (YamlPrimitive) members.get(memberName);
	}

	public YamlArray getAsYamlArray(String memberName) {
		return (YamlArray) members.get(memberName);
	}

	public YamlObject getAsYamlObject(String memberName) {
		return (YamlObject) members.get(memberName);
	}
}