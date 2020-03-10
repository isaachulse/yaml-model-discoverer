package com.isaachulse.yamlmodeldiscoverer;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.yaml.snakeyaml.Yaml;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * This class is used to discover/refine metamodels (as {@link EPackage}
 * elements) from JSON documents (i.e., json file).
 * <p>
 * Once created, a metamodel out of a {@link DocumentSource} element can be
 * discovered by calling the method
 * {@link JsonSimpleDiscoverer#discover(DocumentSource)}.
 * <p>
 * To refine a metamodel, you have to call to
 * {@link JsonSimpleDiscoverer#refine(EPackage, DocumentSource)} giving the
 * metamodel to refine and a new {@link DocumentSource}.
 * <p>
 */
class JsonSimpleDiscoverer {
	/**
	 * Default prefix for the discovered metamodel
	 */
	private static final String DEFAULT_NS_PREFIX = "disco";
	/**
	 * Default NS URI for the discovered metamodel
	 */
	private static final String DEFAULT_NS_URI = "http://jsonDiscoverer/discovered/";
	/**
	 * Used to log all the activity
	 */
	private final static Logger LOGGER = Logger.getLogger(JsonSimpleDiscoverer.class.getName());
	/**
	 * The set of metamodel classes discovered
	 */
	private HashMap<String, EClass> eClasses = new HashMap<String, EClass>();

	/**
	 * Constructs a new {@link JsonSimpleDiscoverer} element
	 */
	public JsonSimpleDiscoverer() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

		LOGGER.setLevel(Level.OFF);
	}

	/**
	 * Launches the metamodel discoverer from a JSON document.
	 * <p>
	 * The method receives a {@link DocumentSource} element, which includes the set of
	 * JSON documents to be considered.
	 * <p>
	 * The discovered metamodel is returned and also stored in the
	 * {@link DocumentSource} received as param (calling
	 * {@link DocumentSource#setMetamodel(EPackage)}).
	 *
	 * @param source The {@link DocumentSource} including the JSON documents
	 * @return The metamodel (as {@link EPackage})
	 */
	EPackage discover(DocumentSource source) {
		if (source == null)
			throw new IllegalArgumentException("Source cannot be null");
		else if (source.getJsonData().size() == 0)
			throw new IllegalArgumentException(
					"The source [" + source.getName() + "] must include, at least, one JSON document.");

		List<JsonObject> elements = source.getSourceDigested();

		LOGGER.fine("[discoverMetamodel] Received " + elements.size() + " json objects to discover");

		String sourceName = source.getName();
		sourceName = digestId(sourceName);

		for (JsonObject jsonObject : elements) {
			discoverMetaclass(sourceName, jsonObject);
		}

		// Default package
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName(source.getName());
		ePackage.setNsURI(DEFAULT_NS_URI + source.getName());
		ePackage.setNsPrefix(DEFAULT_NS_PREFIX + source.getName().charAt(0));
		ePackage.getEClassifiers().addAll(geteClasses().values());
		source.setMetamodel(ePackage);

		// Calculating coverage
		AnnotationHelper.INSTANCE.calculateCoverage(ePackage);

		return ePackage;
	}

	/**
	 * Discover a metaclass for a {@link DocumentSource}
	 *
	 * @param id         Unique identifier for the {@link JsonObject}
	 * @param jsonObject the {@link JsonObject}
	 * @return Discovered {@link EClass}
	 */
	private EClass discoverMetaclass(String id, JsonObject jsonObject) {
		if (id == null)
			throw new IllegalArgumentException("id cannot be null");
		if (jsonObject == null)
			throw new IllegalArgumentException("jsonObject cannot be null");

		EClass eClass = eClasses.get(id);
		if (eClass != null) {
			LOGGER.finer("[discoverMetaclass] Refining " + id);
			eClass = refineMetaclass(eClass, jsonObject);
		} else {
			LOGGER.finer("[discoverMetaclass] Creating " + id);
			eClass = createMetaclass(id, jsonObject);
		}
		return eClass;
	}

	/**
	 * Creates a new metaclass form scratch. Takes a {@link JsonObject} as input and
	 * an identifier.
	 *
	 * @param id         Unique identifier of the {@link JsonObject}
	 * @param jsonObject The new {@link JsonObject}
	 * @return The create metaclass (as {@link EClass})
	 */
	private EClass createMetaclass(String id, JsonObject jsonObject) {
		if (id == null)
			throw new IllegalArgumentException("id cannot be null");
		if (jsonObject == null)
			throw new IllegalArgumentException("jsonObject cannot be null");

		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(id);
		AnnotationHelper.INSTANCE.increaseTotalFound(eClass);
		eClasses.put(id, eClass);
		LOGGER.fine("[createMetaclass] Metaclass created with name " + id);

		Iterator<Map.Entry<String, JsonElement>> pairs = jsonObject.entrySet().iterator();
		LOGGER.finer("[createMetaclass] Iterating over " + jsonObject.entrySet().size() + " pairs");
		while (pairs.hasNext()) {
			Map.Entry<String, JsonElement> pair = pairs.next();

			String pairId = pair.getKey();
			JsonElement value = pair.getValue();

			createStructuralFeature(pairId, value, 1, eClass);
		}
		return eClass;
	}

	/**
	 * Refines the attributes and references of an existing {@link EClass} from a
	 * new {@link JsonObject} definition.
	 *
	 * @param eClass     The existing {@link EClass}
	 * @param jsonObject The {@link JsonObject} to use as input to refine
	 * @return The refined class (as {@link EClass})
	 */
	private EClass refineMetaclass(EClass eClass, JsonObject jsonObject) {
		if (eClass == null)
			throw new IllegalArgumentException("eClass cannot be null");
		if (jsonObject == null)
			throw new IllegalArgumentException("jsonObject cannot be null");

		LOGGER.fine("[refineMetaclass] Refining metaclass " + eClass.getName());
		AnnotationHelper.INSTANCE.increaseTotalFound(eClass);

		Iterator<Map.Entry<String, JsonElement>> pairs = jsonObject.entrySet().iterator();
		while (pairs.hasNext()) {
			Map.Entry<String, JsonElement> pair = pairs.next();

			String pairId = pair.getKey();
			JsonElement value = pair.getValue();

			EStructuralFeature eStructuralFeature = null;
			if ((eStructuralFeature = eClass.getEStructuralFeature(pairId)) != null) {
				AnnotationHelper.INSTANCE.increaseTotalFound(eStructuralFeature);
				// Dealing with attributes (references are considered non-conflicting)
				if (eStructuralFeature instanceof EAttribute) {
					EAttribute eAttribute = (EAttribute) eStructuralFeature;
					if (eAttribute.getEType() != mapType(pairId, value)) {
						LOGGER.fine("[refineMetaclass] Attribute " + eAttribute.getName()
								+ " typed to String due to conflicts");
						eAttribute.setEType(EcorePackage.Literals.ESTRING);
					} else {
						LOGGER.fine("[refineMetaclass] No conflicts with attribute " + eAttribute.getName());
					}
				}
			} else {
				createStructuralFeature(pairId, value, 0, eClass);
			}
		}

		return eClass;
	}

	/**
	 * Creates a new {@link EStructuralFeature} out from a pairId/Value
	 *
	 * @param pairId     Identifier of the feature
	 * @param value      {@link JsonElement} including the value
	 * @param lowerBound The lover bound for the structural feature
	 * @param eClass     {@link EClass} containing the feature
	 */
	private void createStructuralFeature(String pairId, JsonElement value, int lowerBound, EClass eClass) {
		if (pairId == null)
			throw new IllegalArgumentException("pairId cannot be null");
		if (value == null)
			throw new IllegalArgumentException("value cannot be null");
		if (eClass == null)
			throw new IllegalArgumentException("eClass cannot be null");

		EStructuralFeature eStructuralFeature = null;

		// Selecting attribute vs. reference according to feature's type
		EClassifier type = mapType(pairId, value);
		if (type instanceof EDataType) {
			eStructuralFeature = EcoreFactory.eINSTANCE.createEAttribute();
		} else {
			eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
			((EReference) eStructuralFeature).setContainment(true);
		}

		if (value.isJsonArray()) {
			eStructuralFeature.setUpperBound(-1);
		}

		if (eStructuralFeature != null) {
			eStructuralFeature.setName(pairId);
			eStructuralFeature.setLowerBound(lowerBound);
			eStructuralFeature.setEType(mapType(pairId, value));
			AnnotationHelper.INSTANCE.increaseTotalFound(eStructuralFeature);
			eClass.getEStructuralFeatures().add(eStructuralFeature);
			LOGGER.fine("[createStructuralFeature] " + eStructuralFeature.getClass().getSimpleName()
					+ " created with name " + pairId + " type " + eStructuralFeature.getEType().getName()
					+ " and lower bound " + lowerBound);
		}
	}

	/**
	 * Maps JSON types into ECORE types
	 *
	 * @param id    Identifier of the feature (to infer the name of the type if
	 *              non-primitive)
	 * @param value {@link JsonElement} including the value
	 * @return The mapped type (as {@link EClassifier}
	 */
	private EClassifier mapType(String id, JsonElement value) {
		if (id == null)
			throw new IllegalArgumentException("id cannot be null");
		if (value == null)
			throw new IllegalArgumentException("jsonObject cannot be null");

		if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
			return EcorePackage.Literals.ESTRING;
		} else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
			return EcorePackage.Literals.EINT;
		} else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
			return EcorePackage.Literals.EBOOLEAN;
		} else if (value.isJsonArray()) {
			JsonArray arrayValue = value.getAsJsonArray();
			if (arrayValue.size() > 0) {
				EClassifier generalArrayType = mapType(digestId(id), arrayValue.get(0));
				for (int i = 1; i < arrayValue.size(); i++) {
					JsonElement arrayElement = arrayValue.get(i);
					EClassifier arrayType = mapType(digestId(id), arrayElement);
					if (generalArrayType != arrayType) {
						LOGGER.finer("[mapType] Detected array multi-typed, using fallback type (String) for " + id);
						return EcorePackage.Literals.ESTRING;
					}
				}
				return generalArrayType;
			}
		} else if (value.isJsonObject()) {
			return discoverMetaclass(digestId(id), value.getAsJsonObject());
		}
		LOGGER.finer("[mapType] Type not discovererd for " + id);
		return EcorePackage.Literals.ESTRING;
	}

	/**
	 * Generates a type identifier from a String (normally coming for the key value
	 * of JSON objects)
	 *
	 * @param id String to digest
	 * @return The digested identifier
	 */
	private String digestId(String id) {
		if (id == null)
			throw new IllegalArgumentException("id cannot be null");

		String result = id;
		if (result.length() > 1 && result.endsWith("s"))
			result = result.substring(0, result.length() - 1);
		result = result.substring(0, 1).toUpperCase() + result.substring(1, result.length());
		return result;
	}

	/**
	 * Returns the set of metamodel classes discovered
	 *
	 * @return List of {@link EClass} elements
	 */
	private HashMap<String, EClass> geteClasses() {
		return eClasses;
	}

	private static JsonElement wrapYamlObject(Object o) {

		// NULL => JsonNull
		if (o == null)
			return JsonNull.INSTANCE;

		// Collection => JsonArray
		if (o instanceof Collection) {
			JsonArray array = new JsonArray();
			for (Object childObj : (Collection<?>) o)
				array.add(wrapYamlObject(childObj));
			return array;
		}

		// Array => JsonArray
		if (o.getClass().isArray()) {
			JsonArray array = new JsonArray();

			int length = Array.getLength(array);
			for (int i = 0; i < length; i++)
				array.add(wrapYamlObject(Array.get(array, i)));

			return array;
		}

		// Map => JsonObject
		if (o instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) o;

			JsonObject jsonObject = new JsonObject();
			for (final Map.Entry<?, ?> entry : map.entrySet()) {
				final String name = String.valueOf(entry.getKey());
				final Object value = entry.getValue();
				jsonObject.add(name, wrapYamlObject(value));
			}

			return jsonObject;
		}

		// everything else => JsonPrimitive
		if (o instanceof String)
			return new JsonPrimitive((String) o);
		if (o instanceof Number)
			return new JsonPrimitive((Number) o);
		if (o instanceof Character)
			return new JsonPrimitive((Character) o);
		if (o instanceof Boolean)
			return new JsonPrimitive((Boolean) o);

		// otherwise.. string is a good guess
		return new JsonPrimitive(String.valueOf(o));
	}

	public static void main(String[] args) {

//		String yamlCode = "---\n" + "employee:\n" + "  name: sonoo\n" + "  salary: 56000\n" + "  married: true\n" + "\n"+ "";
		
		String yamlCode = "\n" + 
				"invoice: 34843\n" + 
				"date   : 2001-01-23\n" + 
				"bill-to: &id001\n" + 
				"    given  : Chris\n" + 
				"    family : Dumars\n" + 
				"    address:\n" + 
				"        lines: |\n" + 
				"            458 Walkman Dr.\n" + 
				"            Suite #292\n" + 
				"        city    : Royal Oak\n" + 
				"        state   : MI\n" + 
				"        postal  : 48046\n" + 
				"ship-to: *id001\n" + 
				"product:\n" + 
				"    - sku         : BL394D\n" + 
				"      quantity    : 4\n" + 
				"      description : Basketball\n" + 
				"      price       : 450.00\n" + 
				"    - sku         : BL4438H\n" + 
				"      quantity    : 1\n" + 
				"      description : Super Hoop\n" + 
				"      price       : 2392.00\n" + 
				"tax  : 251.42\n" + 
				"total: 4443.52\n" + 
				"comments: >\n" + 
				"    Late afternoon is best.\n" + 
				"    Backup contact is Nancy\n" + 
				"    Billsmer @ 338-4338.";

//		String jsonCode = "{  \n" +
//				"    \"employee\": {  \n" +
//				"        \"name\":       \"sonoo\",   \n" +
//				"        \"salary\":      56000,   \n" +
//				"        \"married\":    true  \n" +
//				"    }  \n" +
//				"}";
//
		Yaml yaml = new Yaml();
		Map<String, Object> yamlMap = yaml.load(yamlCode);
		JsonElement jsonElem = wrapYamlObject(yamlMap);
		
		JsonSimpleDiscoverer discoverer = new JsonSimpleDiscoverer();
		
		DocumentSource source = new DocumentSource("Discovered");
//		source.addJsonData(null, new StringReader(jsonCode));

		source.addJsonDataFromElem(jsonElem);
		EPackage discoveredModel = discoverer.discover(source);

		EList<EClassifier> model = discoveredModel.getEClassifiers();

		EClass first = (EClass) model.get(0);
		System.out.println(first.getEStructuralFeatures());

	}
}
