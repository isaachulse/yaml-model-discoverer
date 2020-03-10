# YAML Model Discoverer

_Lightweight utility to generate an ECore metamodel from a YAML file._

---

Find `main` in `YamlResource.java`, instantiating a metamodel ResourceSet and corresponding EPackage, as well as generated EPackage from YAML file. 

Usage as follows:
```java
// Bam! EPackage with everything in...
EPackage discoveredModel = discoverer.discover(source);

// Find EClassifiers
EList<EClassifier> eClassifiers = discoveredModel.getEClassifiers();

// Finding some EStructuralFeatures
EList<EStructuralFeature> eStructuralFeatures = ((EClass) eClassifiers.get(0)).getEStructuralFeatures();
```

Currently Supported Types:
* `null`
* `java.util.Collection`
* `java.util.Map`
* `java.lang.Number`
* `java.lang.String`
* `java.lang.Boolean`
* All other values default to `java.lang.String` using `.toString()`

