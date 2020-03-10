package com.isaachulse.yamlmodeldiscoverer;

import com.google.gson.JsonElement;

class JsonData {

	private JsonElement data;

	public JsonData(JsonElement data) {
		if (data == null)
			throw new IllegalArgumentException("Data can't be null");

		this.data = data;
	}

	public JsonElement getData() {
		return data;
	}
}
