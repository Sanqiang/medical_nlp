package medical_classify;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;

public class Module {
	// singular set up
	private static Module _inst = null;
	private static Object _lock = new Object();

	public static Module getInst() {
		synchronized (_lock) {
			if (_inst == null) {
				_inst = new Module();
			}
		}
		return _inst;
	}

	// nested module
	private MaxentTagger _tagger = null;
	private DependencyParser _dep_parser = null;
	private LexicalizedParser _lex_parser = null;

	private Module() {
		_tagger = new MaxentTagger(Config.PATH_TAGGER);
		_dep_parser = DependencyParser.loadFromModelFile(Config.CONFIG_DEPENDENCY_PARSER);
		String[] options = { "-maxLength", "80", "-retainTmpSubcategories" };
		_lex_parser = LexicalizedParser.loadModel(Config.CONFIG_PCFG_PARSER, options);
		
		try {
			JWNL.initialize(new FileInputStream(Config.PATH_JWNL_CONFIG));
		} catch (FileNotFoundException | JWNLException e) {
			e.printStackTrace();
		}
	}

	public MaxentTagger getTagger() {
		return getInst()._tagger;
	}

	public DependencyParser getDependencyParser() {
		return getInst()._dep_parser;
	}
	
	public LexicalizedParser getLexicalizedParser(){
		return getInst()._lex_parser;
	}
}
