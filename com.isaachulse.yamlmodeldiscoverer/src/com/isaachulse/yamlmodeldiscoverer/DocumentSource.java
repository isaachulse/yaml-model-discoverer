package com.isaachulse.yamlmodeldiscoverer;

import org.eclipse.emf.ecore.EPackage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class DocumentSource {

	private String name;

	private EPackage metamodel;

	private JsonElement yamlData;

	public String getName() {
		return name;
	}

	public EPackage getMetamodel() {
		return metamodel;
	}

	public void setMetamodel(EPackage metamodel) {
		if (metamodel == null)
			throw new IllegalArgumentException("Metamodel can't be null");
		this.metamodel = metamodel;
	}

	protected JsonElement getJsonData() {
		return yamlData;
	}

	DocumentSource(String name) {
		this.name = name;
	}

	void setJsonDataFromElement(JsonElement jsonElement) {
		this.yamlData = jsonElement;
	}

	void setYamlData(JsonElement jsonElement) {
		this.yamlData = jsonElement;
	}

	public JsonObject getSourceDigest() {
		JsonElement outputElement = this.getJsonData();

		if (outputElement.isJsonArray()) {
			for (int i = 0; i < outputElement.getAsJsonArray().size(); i++)
				if (outputElement.getAsJsonArray().get(i).isJsonObject())
					return (JsonObject) outputElement.getAsJsonArray().get(i).getAsJsonObject();
		}
		return (JsonObject) outputElement.getAsJsonObject();
	}
}