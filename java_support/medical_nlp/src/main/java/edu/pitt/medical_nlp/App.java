package edu.pitt.medical_nlp;

import edu.pitt.medical_nlp.utility.Module;

public class App {
	public static void main(String[] args) {
		// Process p = new Process();
		// p.extractAspect();
		// p.printAspects(0);
		Module.getInst();
		PostProcess postProcess = new PostProcess();
		postProcess.postProcessDocs();
	}
}