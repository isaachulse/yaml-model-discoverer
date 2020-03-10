package com.isaachulse.yamlmodeldiscoverer.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class YamlArray extends YamlElement implements Iterable<YamlElement> {
	private final List<YamlElement> elements;

	public YamlArray() {
		elements = new ArrayList<YamlElement>();
	}

	public void add(YamlElement element) {
		if (element == null) {
			element = YamlNull.INSTANCE;
		}
		elements.add(element);
	}

	public int size() {
		return elements.size();
	}

	@Override
	public Iterator<YamlElement> iterator() {
		return elements.iterator();
	}

	public YamlElement get(int i) {
		return elements.get(i);
	}
}