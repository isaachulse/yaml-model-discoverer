package com.isaachulse.yamlmodeldiscoverer;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.yaml.snakeyaml.Yaml;

import com.isaachulse.yamlmodeldiscoverer.model.YamlArray;
import com.isaachulse.yamlmodeldiscoverer.model.YamlElement;
import com.isaachulse.yamlmodeldiscoverer.model.YamlNull;
import com.isaachulse.yamlmodeldiscoverer.model.YamlObject;
import com.isaachulse.yamlmodeldiscoverer.model.YamlPrimitive;

class YamlModelDiscoverer {

	private static final String DEFAULT_NS_PREFIX = "PREFIX";
	private static final String DEFAULT_NS_URI = "http://DEFAULT_NS_URI/";
	private HashMap<String, EClass> eClasses = new HashMap<String, EClass>();

	public YamlModelDiscoverer() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
	}

	private EPackage discover(DocumentSource source) {
		if (source == null)
			throw new IllegalArgumentException("Source can't be null");

		discoverMetaclass(digestId(source.getName()), source.getSourceDigest());

		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName(source.getName());
		ePackage.setNsURI(DEFAULT_NS_URI + source.getName());
		ePackage.setNsPrefix(DEFAULT_NS_PREFIX + source.getName().charAt(0));
		ePackage.getEClassifiers().addAll(getEClasses().values());
		source.setMetamodel(ePackage);

		return ePackage;
	}

	private EClass discoverMetaclass(String id, YamlObject yamlObject) {
		if (id == null || yamlObject == null)
			throw new IllegalArgumentException("ID or Data can't be null");

		EClass eClass = eClasses.get(id);
		if (eClass != null) {
			eClass = refineMetaclass(eClass, yamlObject);
		} else {
			eClass = createMetaclass(id, yamlObject);
		}
		return eClass;
	}

	private EClass createMetaclass(String id, YamlObject yamlObject) {
		if (id == null)
			throw new IllegalArgumentException("ID can't be null");
		if (yamlObject == null)
			throw new IllegalArgumentException("Data object can't be null");

		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(id);
		eClasses.put(id, eClass);

		Iterator<Map.Entry<String, YamlElement>> pairs = yamlObject.entrySet().iterator();
		while (pairs.hasNext()) {
			Map.Entry<String, YamlElement> pair = pairs.next();

			String pairId = pair.getKey();
			YamlElement value = pair.getValue();

			createStructuralFeature(pairId, value, 1, eClass);
		}
		return eClass;
	}

	// Refines EAttributes and EReferences of existing eclass
	private EClass refineMetaclass(EClass eClass, YamlObject yamlObject) {
		if (eClass == null || yamlObject == null)
			throw new IllegalArgumentException("Argument(s) can't be null");

		Iterator<Map.Entry<String, YamlElement>> pairs = yamlObject.entrySet().iterator();
		while (pairs.hasNext()) {
			Map.Entry<String, YamlElement> pair = pairs.next();

			String pairId = pair.getKey();
			YamlElement value = pair.getValue();

			EStructuralFeature eStructuralFeature = null;
			if ((eStructuralFeature = eClass.getEStructuralFeature(pairId)) != null) {
				if (eStructuralFeature instanceof EAttribute) {
					EAttribute eAttribute = (EAttribute) eStructuralFeature;
					if (eAttribute.getEType() != mapType(pairId, value)) {
						eAttribute.setEType(EcorePackage.Literals.ESTRING);
					}
				}
			} else {
				createStructuralFeature(pairId, value, 0, eClass);
			}
		}

		return eClass;
	}

	// Creates a new EStructuralFeature
	private void createStructuralFeature(String pairId, YamlElement value, int lowerBound, EClass eClass) {
		if (pairId == null || value == null || eClass == null)
			throw new IllegalArgumentException("Argument(s) can't be null");

		EStructuralFeature eStructuralFeature = null;

		// Select either attribute or reference, according to feature type
		EClassifier type = mapType(pairId, value);
		if (type instanceof EDataType) {
			eStructuralFeature = EcoreFactory.eINSTANCE.createEAttribute();
		} else {
			eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
			((EReference) eStructuralFeature).setContainment(true);
		}

		if (value.isYamlArray()) {
			eStructuralFeature.setUpperBound(-1);
		}

		if (eStructuralFeature != null) {
			eStructuralFeature.setName(pairId);
			eStructuralFeature.setLowerBound(lowerBound);
			eStructuralFeature.setEType(mapType(pairId, value));
			eClass.getEStructuralFeatures().add(eStructuralFeature);
		}
	}

	// Mapping into ECore types
	private EClassifier mapType(String id, YamlElement value) {
		if (id == null || value == null)
			throw new IllegalArgumentException("Argument(s) can't be null");

		if (value.isYamlPrimitive() && value.getAsYamlPrimitive().isString()) {
			return EcorePackage.Literals.ESTRING;
		} else if (value.isYamlPrimitive() && value.getAsYamlPrimitive().isNumber()) {
			return EcorePackage.Literals.EINT;
		} else if (value.isYamlPrimitive() && value.getAsYamlPrimitive().isBoolean()) {
			return EcorePackage.Literals.EBOOLEAN;
		} else if (value.isYamlArray()) {
			YamlArray arrayValue = value.getAsYamlArray();
			if (arrayValue.size() > 0) {
				EClassifier generalArrayType = mapType(digestId(id), arrayValue.get(0));
				for (int i = 1; i < arrayValue.size(); i++) {
					YamlElement arrayElement = arrayValue.get(i);
					EClassifier arrayType = mapType(digestId(id), arrayElement);
					if (generalArrayType != arrayType) {
						return EcorePackage.Literals.ESTRING;
					}
				}
				return generalArrayType;
			}
		} else if (value.isYamlObject()) {
			return discoverMetaclass(digestId(id), value.getAsYamlObject());
		}
		return EcorePackage.Literals.ESTRING;
	}

	// Generates String type identifier
	private String digestId(String id) {
		if (id == null)
			throw new IllegalArgumentException("Argument(s) can't be null");

		// Look out for this, might want to change later
		String result = id;
		if (result.length() > 1 && result.endsWith("s"))
			result = result.substring(0, result.length() - 1);
		result = result.substring(0, 1).toUpperCase() + result.substring(1, result.length());
		return result;
	}

	private HashMap<String, EClass> getEClasses() {
		return eClasses;
	}

	// Maps YAML elements to values
	private static YamlElement wrapYamlObject(Object o) {

		// NULL transformed to YamlNull
		if (o == null)
			return YamlNull.INSTANCE;

		// Collection transformed to YamlArray
		if (o instanceof Collection) {
			YamlArray array = new YamlArray();
			for (Object child : (Collection<?>) o)
				array.add(wrapYamlObject(child));
			return array;
		}

		// Array transformed to YamlArray
		if (o.getClass().isArray()) {
			YamlArray array = new YamlArray();

			int length = Array.getLength(array);
			for (int i = 0; i < length; i++)
				array.add(wrapYamlObject(Array.get(array, i)));

			return array;
		}

		// Map transformed to YamlObject
		if (o instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) o;

			YamlObject yamlObject = new YamlObject();
			for (final Map.Entry<?, ?> entry : map.entrySet()) {
				final String name = String.valueOf(entry.getKey());
				final Object value = entry.getValue();
				yamlObject.add(name, wrapYamlObject(value));
			}

			return yamlObject;
		}

		// Everything else transformed to YamlPrimitive
		if (o instanceof String)
			return new YamlPrimitive((String) o);
		if (o instanceof Number)
			return new YamlPrimitive((Number) o);
		if (o instanceof Character)
			return new YamlPrimitive((Character) o);
		if (o instanceof Boolean)
			return new YamlPrimitive((Boolean) o);

		// Default to String if we can't find anything else
		return new YamlPrimitive(String.valueOf(o));
	}

	// Main method to test
	public static void main(String[] args) {

		String yamlCode = "\n" + "invoice: 34843\n" + "date   : 2001-01-23\n" + "bill-to: &id001\n"
				+ "    given  : Chris\n" + "    family : Dumars\n" + "    addresss : \n" + "        lines: |\n"
				+ "            458 Walkman Dr.\n" + "            Suite #292\n" + "        city    : Royal Oak\n"
				+ "        state   : MI\n" + "        postal  : 48046\n" + "ship-to: *id001\n" + "product:\n"
				+ "    - sku         : BL394D\n" + "      quantity    : 4\n" + "      description : Basketball\n"
				+ "      price       : 450.00\n" + "    - sku         : BL4438H\n" + "      quantity    : 1\n"
				+ "      description : Super Hoop\n" + "      price       : 2392.00\n" + "tax  : 251.42\n"
				+ "total: 4443.52\n" + "comments: >\n" + "    Late afternoon is best.\n"
				+ "    Backup contact is Nancy\n" + "    Billsmer @ 338-4338.";

		Yaml yaml = new Yaml();
		Map<String, Object> yamlMap = yaml.load(yamlCode);
		YamlElement yamlElem = wrapYamlObject(yamlMap);

		YamlModelDiscoverer discoverer = new YamlModelDiscoverer();

		DocumentSource source = new DocumentSource("Discovered");

		source.setYamlData(yamlElem);
		EPackage discoveredModel = discoverer.discover(source);

		EList<EClassifier> model = discoveredModel.getEClassifiers();

		EClass first = (EClass) model.get(0);

		System.out.println("EPackage is: " + discoveredModel);
		System.out.println();

		System.out.println("2nd Eclass is: " + model.get(1));
		System.out.println();

		System.out.println("Some structural features are: " + first.getEStructuralFeatures());
	}
}
