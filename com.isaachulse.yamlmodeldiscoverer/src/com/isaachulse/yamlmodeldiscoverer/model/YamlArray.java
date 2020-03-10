package com.isaachulse.yamlmodeldiscoverer.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class YamlArray extends YamlElement implements Iterable<YamlElement> {
	private final List<YamlElement> elements;

	public YamlArray() {
		elements = new ArrayList<YamlElement>();
	}

	public YamlArray(int capacity) {
		elements = new ArrayList<YamlElement>(capacity);
	}

	public void add(Boolean bool) {
		elements.add(bool == null ? YamlNull.INSTANCE : new YamlPrimitive(bool));
	}

	public void add(Character character) {
		elements.add(character == null ? YamlNull.INSTANCE : new YamlPrimitive(character));
	}

	public void add(Number number) {
		elements.add(number == null ? YamlNull.INSTANCE : new YamlPrimitive(number));
	}

	public void add(String string) {
		elements.add(string == null ? YamlNull.INSTANCE : new YamlPrimitive(string));
	}

	public void add(YamlElement element) {
		if (element == null) {
			element = YamlNull.INSTANCE;
		}
		elements.add(element);
	}

	public void addAll(YamlArray array) {
		elements.addAll(array.elements);
	}

	public YamlElement set(int index, YamlElement element) {
		return elements.set(index, element);
	}

	public boolean remove(YamlElement element) {
		return elements.remove(element);
	}

	public YamlElement remove(int index) {
		return elements.remove(index);
	}

	public boolean contains(YamlElement element) {
		return elements.contains(element);
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