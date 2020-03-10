package com.isaachulse.yamlmodeldiscoverer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This class represents a JSON source. A JSON source is represented by a set of
 * JSON documents with the same meaning (i.e., set of JSON documents returned by
 * the same JSON-based Web API).
 * <p>
 * In the context of JSON Discoverer, it is used to model a single JSON-based
 * Web service.
 * <p>
 * JSON documents are represented as {@link JsonData} elements.
 * <p>
 * The set of JSON documents can be retrieved by calling the method
 * {@link JsonSource#getJsonData()}.
 * <p>
 * As {@link JsonData} elements can include input elements (to represent which
 * JSON data has triggered such JSON document), a {@link JsonSource} can include
 * {@link JsonData} elements with or without input. To know whether the
 * {@link JsonData} elements included in the {@link JsonSource} have input
 * elements, you have to call the method {@link JsonSource#withInput}.
 */
class JsonSource extends AbstractJsonSource {
	/**
	 * List of JSON documents
	 */
	private List<JsonData> jsonData;

	/**
	 * Sets if the set of JSON documents are the result of computing an input JSON
	 * document (to support JSON-based Web APIs)
	 */
	private boolean withInput;

	/**
	 * Builds a new JsonSource with a name. Once created, the JsonSource has to be
	 * populated by calling {@link JsonSource#addJsonData(Reader, Reader)}.
	 *
	 * @param name The name of the JsonSource
	 */
	JsonSource(String name) {
		super(name);
		this.jsonData = new ArrayList<JsonData>();
		this.withInput = false;
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

	/**
	 * Indicates if the JsonSource includes input elements.
	 * <p>
	 * Note that all of them have to include (or not include) input elements
	 *
	 * @return Boolean indicating the status
	 */
	boolean includesInput() {
		return this.withInput;
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
		if (this.withInput == true) {
			for (JsonData data : this.getJsonData()) {
				JsonObject inputElement = data.getInput();
				JsonElement outputElement = data.getData();
				inputElement.getAsJsonObject().add(getName() + "Output", outputElement);
				result.add(inputElement);
			}
		} else {
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
		}
		return result;
	}
}
