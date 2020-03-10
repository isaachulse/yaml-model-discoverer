# YAML Model Discoverer

_Lightweight utility to generate an ECore metamodel from a YAML file._

---
Usage as follows:
```java
YamlModelDiscoverer discoverer = new YamlModelDiscoverer();

DocumentSource source = new DocumentSource("RootElement");
source.setYamlData("YAML String Here");

EPackage discoveredModel = discoverer.discover(source);


// Find EClassifiers
EList<EClassifier> eClassifiers = discoveredModel.getEClassifiers();

// Finding some EStructuralFeatures
EList<EStructuralFeature> eStructuralFeatures = ((EClass) eClassifiers.get(0)).getEStructuralFeatures()

```

Currently Supported Types:
* `null`
* `java.util.Collection`
* `java.util.Map`
* `java.lang.Number`
* `java.lang.String`
* `java.lang.Boolean`
* All other values default to `java.lang.String` using `.toString()`

