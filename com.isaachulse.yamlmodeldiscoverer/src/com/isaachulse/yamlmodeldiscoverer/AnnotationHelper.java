package com.isaachulse.yamlmodeldiscoverer;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;

/**
 * This class provides some helper methods to fill in the metadata in the
 * discovered models.
 */
class AnnotationHelper {
	/**
	 * {@link AnnotationHelper} is used as singleton, this is the instance
	 */
	static AnnotationHelper INSTANCE = new AnnotationHelper();

	// Tags used in the coverage model
	/**
	 * Tag used for the total number of found elements
	 * <p>
	 * Added to each attribute/reference to count the times it has been found
	 */
	private static final String TOTAL_FOUND_TAG = "totalFound";
	/**
	 * Tag used for referring to the coverage
	 * <p>
	 * The value indicates the number of clases including such attribute/reference
	 */
	private static final String COVERAGE_TAG = "coverage";
	/**
	 * Tag used for the annotation related to the ratio of total found
	 * <p>
	 * This value is the ratio version of the
	 * {@link AnnotationHelper#TOTAL_FOUND_TAG}
	 */
	private static final String RATIO_TOTAL_FOUND_TAG = "ratioTotalFound";

	/**
	 * Constructor. As this class is singleton, no need to be public.
	 */
	private AnnotationHelper() {
	}

	/**
	 * Access to the annotation for {@link AnnotationHelper#COVERAGE_TAG}. To be
	 * edited afterwards.
	 *
	 * @param modelElement The model element from which the annotation has to be
	 *                     retrieved
	 * @return The {@link EAnnotation} element
	 */
	private EAnnotation getCoverageAnnotation(EModelElement modelElement) {
		if (modelElement == null)
			throw new IllegalArgumentException("The modelElement cannot be null");

		EAnnotation annotation = modelElement.getEAnnotation(COVERAGE_TAG);
		if (annotation == null) {
			annotation = EcoreFactory.eINSTANCE.createEAnnotation();
			annotation.setSource(COVERAGE_TAG);
			annotation.getDetails().put(TOTAL_FOUND_TAG, "0");
			modelElement.getEAnnotations().add(annotation);
		}
		return annotation;
	}

	/**
	 * Accesses to the annotation for {@link AnnotationHelper#TOTAL_FOUND_TAG} and
	 * increases it.
	 *
	 * @param modelElement The model element from which the annotation has to be
	 *                     retrieved
	 */
	void increaseTotalFound(EModelElement modelElement) {
		if (modelElement == null)
			throw new IllegalArgumentException("The modelElement cannot be null");

		EAnnotation annotation = getCoverageAnnotation(modelElement);
		if (annotation != null) {
			String currentCounter = annotation.getDetails().get(TOTAL_FOUND_TAG);
			if (currentCounter != null) {
				annotation.getDetails().put(TOTAL_FOUND_TAG,
						String.valueOf(Integer.valueOf(currentCounter).intValue() + 1));
			}
		}
	}

	/**
	 * Calculate the value for the annotation
	 * {@link AnnotationHelper#RATIO_TOTAL_FOUND_TAG}.
	 *
	 * @param ePackage The model element from which the annotation has to be
	 *                 retrieved
	 */
	void calculateCoverage(EPackage ePackage) {
		if (ePackage == null)
			throw new IllegalArgumentException("The ePackage cannot be null");

		for (EClassifier eClassifier : ePackage.getEClassifiers()) {
			if (eClassifier instanceof EClass) {
				EClass eClass = (EClass) eClassifier;
				EAnnotation eClassAnnotation = getCoverageAnnotation(eClass);

				int eClassCounter = Integer.valueOf(eClassAnnotation.getDetails().get(TOTAL_FOUND_TAG)).intValue();
				for (EStructuralFeature eStructuralFeature : eClass.getEStructuralFeatures()) {
					EAnnotation eStructuralFeatureAnnotation = getCoverageAnnotation(eStructuralFeature);
					int eStructuralFeatureCounter = Integer
							.valueOf(eStructuralFeatureAnnotation.getDetails().get(TOTAL_FOUND_TAG)).intValue();
					double ratio = ((double) eStructuralFeatureCounter) / ((double) eClassCounter);
					eStructuralFeatureAnnotation.getDetails().put(RATIO_TOTAL_FOUND_TAG, String.valueOf(ratio));
				}
			}
		}
	}

}
