package com.isaachulse.yamlmodeldiscoverer.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class YamlObject extends YamlElement {
	private final Map<String, YamlElement> members = new HashMap<String, YamlElement>();

	public void add(String property, YamlElement value) {
		members.put(property, value == null ? YamlNull.INSTANCE : value);
	}

	public Set<Map.Entry<String, YamlElement>> entrySet() {
		return members.entrySet();
	}

}