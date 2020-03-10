package com.isaachulse.yamlmodeldiscoverer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class YamlResource extends ResourceImpl {

	public YamlResource(URI uri) {
		super(uri);
	}

	public static void main(String[] args) {

		// Load metamodel EPackage
		ResourceSet metamodelResourceSet = new ResourceSetImpl();

		metamodelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		Resource metamodelResource = metamodelResourceSet
				.getResource(URI.createURI(new File("universitydsl.ecore").toURI().toString()), true);
		try {
			metamodelResource.load(null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		EPackage metamodelEPackage = (EPackage) metamodelResource.getContents().get(0);

		// Generate EPackage from YAML
		ResourceSet modelResourceSet = new ResourceSetImpl();
		modelResourceSet.getPackageRegistry().put(metamodelEPackage.getNsURI(), metamodelEPackage);

		modelResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new YamlResourceFactory());
		Resource modelResource = modelResourceSet
				.getResource(URI.createURI(new File("university.yaml").toURI().toString()), true);

		try {
			modelResource.load(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		try {
			doLoadImpl(inputStream, options);
		} catch (IOException ioException) {
			ioException.printStackTrace();
			throw ioException;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	public void doLoadImpl(InputStream inputStream, Map<?, ?> options) throws Exception {
		YamlProcessor yamlProcessor = new YamlProcessor();

		EPackage thing = yamlProcessor.processYaml(inputStream);

		System.out.println(thing.getEClassifiers());

	}

}