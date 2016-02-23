package edu.pitt.medical_nlp;

import org.junit.Test;

import edu.pitt.medical_nlp.text.FeatureSelect;
import edu.pitt.medical_nlp.text.PostProcess;
import edu.pitt.medical_nlp.text.Process;
import edu.pitt.medical_nlp.utility.MetaType;
import edu.pitt.medical_nlp.utility.Module;
import edu.pitt.medical_nlp.utility.WordNetUtility;

public class testProcess {

	// @Test
	public void testProcessDocs() {
		Module.getInst();
		Process process = new Process();
		System.out.println("start processing!");
		process.processDocs("Cough, rule out pneumonia. No acute pulmonary disease.");
		System.out.println(process);
	}

	// @Test
	public void testWordNetGetStem() {
		Module.getInst();
		System.out.println(WordNetUtility.getStem("was"));
	}

	// @Test
	public void testPostProcessDocs() {
		Module.getInst();
		PostProcess process = new PostProcess();
		System.out.println("start processing!");
		process.processDocs("the heart and lungs appear normal.");
		process.postProcessSingleDocs();
		System.out.println(process);
	}

	 @Test
	public void testInitMetaType() {
		String entry = MetaType.requestWeb("enuresis");
		System.out.println(entry);
	}
	
	//@Test
	public void testFeatureSelect(){
		FeatureSelect fs = new FeatureSelect();
		fs.select(10);
	}

}
