package com.isaachulse.yamlmodeldiscoverer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class DocumentSource {

	private String name;

	private EPackage metamodel;
	
	private List<JsonData> jsonData;

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
	
	protected List<JsonData> getJsonData() {
		return jsonData;
	}

	DocumentSource(String name) {
		this.name = name;
		this.jsonData = new ArrayList<JsonData>();
	}

	void addJsonDataFromElem(JsonElement jsonElement) {
		getJsonData().add(new JsonData(jsonElement));
	}

	public List<JsonObject> getSourceDigest() {
		List<JsonObject> result = new ArrayList<JsonObject>();

		for (JsonData data : this.getJsonData()) {
			JsonElement outputElement = data.getData();
			if (outputElement.isJsonArray()) {
				for (int i = 0; i < outputElement.getAsJsonArray().size(); i++)
					if (outputElement.getAsJsonArray().get(i).isJsonObject())
						result.add(outputElement.getAsJsonArray().get(i).getAsJsonObject());
			} else if (outputElement.isJsonObject()) {
				result.add(outputElement.getAsJsonObject());
			}
		}

		return result;
	}
}