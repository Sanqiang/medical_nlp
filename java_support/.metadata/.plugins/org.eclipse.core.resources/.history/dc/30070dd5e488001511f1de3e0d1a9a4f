package edu.pitt.medical_nlp;

import org.junit.Test;

import edu.pitt.medical_nlp.utility.Module;
import edu.pitt.medical_nlp.utility.WordNetUtility;

public class testProcess {

	// @Test
	public void testProcessDocs() {
		Module.getInst();
		Process process = new Process();
		System.out.println("start processing!");
		String ndoc = process.processDocs("Cough, rule out pneumonia. No acute pulmonary disease.");
		System.out.println(ndoc);
	}

	// @Test
	public void testWordNetGetStem() {
		Module.getInst();
		System.out.println(WordNetUtility.getStem("chest x-ray"));
	}

	@Test
	public void testPostProcessDocs() {
		Module.getInst();
		PostProcess process = new PostProcess();
		System.out.println("start processing!");
		String ndoc = process.postProcessSingleDocs(process.processDocs(
				"13-1/2-year - old with recent urinary tract and traction and fever. Horseshoe kidney. No complications from pyelonephritis are seen."));
		System.out.println(ndoc);
	}

}
