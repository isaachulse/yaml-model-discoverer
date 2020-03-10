package com.isaachulse.yamlmodeldiscoverer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class DocumentSource {

	private String name;

	private EPackage metamodel;

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

	
	private List<JsonData> jsonData;

	/**
	 * Builds a new JsonSource with a name. Once created, the JsonSource has to be
	 * populated by calling {@link DocumentSource#addJsonData(Reader, Reader)}.
	 *
	 * @param name The name of the JsonSource
	 */
	DocumentSource(String name) {
		this.name = name;
		this.jsonData = new ArrayList<JsonData>();
	}

	/**
	 * Gets the set of {@link JsonData} elements linked to this source. Warning: the
	 * returned list is mutable
	 *
	 * @return The set of JSON documents (as {@link JsonData}
	 */
	protected List<JsonData> getJsonData() {
		return jsonData;
	}

	private JsonData buildJsonDataFromElem(JsonElement jsonElement) {
		JsonObject inputJsonObject = null;
		JsonData data = new JsonData(inputJsonObject, jsonElement);

		return data;
	}

	void addJsonDataFromElem(JsonElement jsonElement) {
		JsonData data = buildJsonDataFromElem(jsonElement);
		getJsonData().add(data);

	}

	/**
	 * Generates a list of JSON objects according to the {@link JsonData} of this
	 * source.
	 * <ul>
	 * <li>If the source DOES include inputs, the list will include the set of input
	 * elements as roots for the {@link JsonData}. For instance:</li>
	 * </ul>
	 *
	 * <pre>
	 * - [input JSON element 1]
	 *   +-- Output
	 *       +-- [output JSON element 1]
	 * - [input JSON element 2]
	 *   +-- Output
	 *       +-- [output JSON element 2]
	 * -...
	 * </pre>
	 * <ul>
	 * <li>If the source DOES NOT include inputs, the list will include all the
	 * objects from {@link JsonData}. For instance</li>
	 * </ul>
	 * 
	 * <pre>
	 * - [output JSON element 1]
	 * - [output JSON element 2]
	 * - [output JSON element 3]
	 * -...
	 * </pre>
	 *
	 * @return The list of {@link JsonObject}s
	 */
	public List<JsonObject> getSourceDigested() {
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