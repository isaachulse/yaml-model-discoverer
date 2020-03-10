package com.isaachulse.yamlmodeldiscoverer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.emf.ecore.EPackage;
import org.yaml.snakeyaml.Yaml;

import com.isaachulse.yamlmodeldiscoverer.model.YamlElement;

public class YamlProcessor {

	private String ROOT_ELEMENT_NAME = "RootElement";
	
	protected EPackage processYaml(InputStream inputStream) {
		
		Yaml yaml = new Yaml();
		
		Object seralized = null;
		try {
			seralized = yaml.load(readFromInputStream(inputStream));
		} catch (IOException e) {
			System.out.println("Could not serialize YAML");
			e.printStackTrace();
		}
		
		YamlElement yamlElement = YamlModelDiscoverer.wrapYamlObject(seralized);
		YamlModelDiscoverer discoverer = new YamlModelDiscoverer();
	
		DocumentSource source = new DocumentSource(ROOT_ELEMENT_NAME);
		source.setYamlData(yamlElement);
	
		EPackage discoveredModel = discoverer.discover(source);
		return discoveredModel;
	}
	
	private String readFromInputStream(InputStream inputStream) throws IOException {
		StringBuilder contents = new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = bufferedReader.readLine();

		// Load lines into StringBuilder
		while (line != null) {
			contents.append(line + "\n");
			line = bufferedReader.readLine();
		}
		
		return contents.toString();
	}
}