package com.isaachulse.yamlmodeldiscoverer;

import com.google.gson.JsonElement;

class YamlData {

	private JsonElement data;

	public YamlData(JsonElement data) {
		if (data == null)
			throw new IllegalArgumentException("Data can't be null");

		this.data = data;
	}

	public JsonElement getData() {
		return data;
	}
	
}
