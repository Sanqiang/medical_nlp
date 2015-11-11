package edu.pitt.medical_nlp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import edu.pitt.medical_nlp.graph.Graph;
import edu.pitt.medical_nlp.utility.DependencyType;
import edu.pitt.medical_nlp.utility.Helper;
import edu.pitt.medical_nlp.utility.Module;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class PostProcess extends Process {
	public PostProcess() {
		super();
	}

	public void postProcessDocs(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
			for (String doc : super.processDocs()) {
				writer.write(postProcessSingleDocs(doc));
				writer.write("\n");
				writer.flush();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			System.out.println("Done!");
		}
	}

	public String postProcessSingleDocs(String doc) {
		boolean add_feature = true;
		Graph graph = new Graph();
		StringBuilder ndoc = new StringBuilder();
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(doc));
		MaxentTagger tagger = Module.getInst().getTagger();
		DependencyParser parser = Module.getInst().getDependencyParser();
		int accumulate_idx = 0;
		for (List<HasWord> token_part : tokenizer) {
			List<TaggedWord> tagged = tagger.tagSentence(token_part);
			GrammaticalStructure gs = parser.predict(tagged);
			for (TypedDependency typed_dependence : gs.allTypedDependencies()) {
				int idx_gov = accumulate_idx + typed_dependence.gov().index() - 1;
				int idx_dep = accumulate_idx + typed_dependence.dep().index() - 1;
				String lemma_gov = typed_dependence.gov().word(), lemma_dep = typed_dependence.dep().word();
				// PartOfSpeech pos_gov = PartOfSpeech.OTHER;
				// if (idx_gov >= 0) {
				// pos_gov = Helper.mapPartOfSpeech(tagged.get(idx_gov).tag());
				// }
				// PartOfSpeech pos_dep = PartOfSpeech.OTHER;
				// if (idx_dep >= 0) {
				// pos_dep = Helper.mapPartOfSpeech(tagged.get(idx_dep).tag());
				// }
				// gov->dep
				DependencyType dependency_type = Helper.mapRelationTypes(typed_dependence.reln().getShortName());
				switch (dependency_type) {
				case AdjectiveModifer:
					graph.createEdge(lemma_dep, lemma_gov, idx_dep, idx_gov, dependency_type);
					break;
				case Negative:
					graph.createEdge(lemma_dep, lemma_gov, idx_dep, idx_gov, dependency_type);
					break;
				case NominalSubject:
					// graph.createEdge(lemma_dep, lemma_gov, idx_dep, idx_gov,
					// dependency_type);
					graph.createEdge(lemma_gov, lemma_dep, idx_gov, idx_dep, dependency_type);
					break;
				case Compound:
					graph.createEdge(lemma_dep, lemma_gov, idx_dep, idx_gov, dependency_type);
					break;
				default:
					break;

				}
			}
			accumulate_idx += token_part.size();
		}
		add_features.addAll( graph.generateFeatures());
		ndoc.append(doc).append(" ");
		if (add_feature) {
			for (String feature : add_features) {
				ndoc.append(feature).append(" ");
			}
		}

		return ndoc.toString();
	}
}
