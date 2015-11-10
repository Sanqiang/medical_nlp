package edu.pitt.medical_nlp;

import edu.stanford.nlp.parser.nndep.DependencyParser;

public class Config {
	public final static String PATH_DATA = "C:\\Users\\phd2\\PycharmProjects\\medical_nn\\data\\data.txt";
	public final static String MRCONSO = "C:\\Users\\phd2\\Desktop\\mmsys_direct\\2015AA\\META\\MRCONSO.RRF";
	public final static String MRSTY = "C:\\Users\\phd2\\Desktop\\mmsys_direct\\2015AA\\META\\MRSTY.RRF";
	public final static String PATH_TYPE_LABELED =  "type_labeled.txt";
	public final static String PATH_JWNL_CONFIG =  "jwnl_properties.xml";
	
	// tagger
	public final static String PATH_TAGGER = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
	public final static String CONFIG_DEPENDENCY_PARSER = DependencyParser.DEFAULT_MODEL;
	public final static String CONFIG_PCFG_PARSER = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
}
