package com.isaachulse.yamlmodeldiscoverer;

import org.eclipse.emf.ecore.EPackage;

import com.isaachulse.yamlmodeldiscoverer.model.YamlElement;
import com.isaachulse.yamlmodeldiscoverer.model.YamlObject;

class DocumentSource {

	private String name;

	private EPackage metamodel;

	private YamlElement yamlData;

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

	private YamlElement getYamlData() {
		return yamlData;
	}

	DocumentSource(String name) {
		this.name = name;
	}

	void setYamlData(YamlElement yamlElement) {
		this.yamlData = yamlElement;
	}

	public YamlObject getSourceDigest() {
		YamlElement outputElement = this.getYamlData();

		if (outputElement.isYamlArray()) {
			for (int i = 0; i < outputElement.getAsYamlArray().size(); i++)
				if (outputElement.getAsYamlArray().get(i).isYamlObject())
					return outputElement.getAsYamlArray().get(i).getAsYamlObject();
		}
		return outputElement.getAsYamlObject();
	}
}