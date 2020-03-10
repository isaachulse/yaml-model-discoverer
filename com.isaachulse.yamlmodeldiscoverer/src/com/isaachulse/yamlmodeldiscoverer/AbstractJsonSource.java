package com.isaachulse.yamlmodeldiscoverer;

import org.eclipse.emf.ecore.EPackage;

abstract class AbstractJsonSource {

	/**
	 * A representative name for this source
	 */
	private String name;

	/**
	 * If required, this class can also store the metamodel of the set of JSON
	 * definitions
	 */
	private EPackage metamodel;

	/**
	 * Constructs a new {@link AbstractJsonSource} element with a name
	 *
	 * @param name The name for the JSON source
	 */
	AbstractJsonSource(String name) {
		if (name == null || name.equals(""))
			throw new IllegalArgumentException("Name cannot be null or empty");
		this.name = name;
	}

	/**
	 * Returns the name of the JSON source
	 *
	 * @return The name of the JSON source
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the JSON source
	 *
	 * @param name The name of the JSON source
	 */
	public void setName(String name) {
		if (name == null || name.equals(""))
			throw new IllegalArgumentException("Name cannot be null or empty");
		this.name = name;
	}

	/**
	 * Gets the metamodel linked to the JSON source
	 *
	 * @return The metamodel (as {@link EPackage})
	 */
	public EPackage getMetamodel() {
		return metamodel;
	}

	/**
	 * Sets the metamodel linked to the JSON source
	 *
	 * @param metamodel The metamodel (as {@link EPackage})
	 */
	public void setMetamodel(EPackage metamodel) {
		if (metamodel == null)
			throw new IllegalArgumentException("Metamodel cannot be null");
		this.metamodel = metamodel;
	}

}
