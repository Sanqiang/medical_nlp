package edu.pitt.review_mining.process.text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;

import edu.pitt.review_mining.utility.Config;
import edu.pitt.review_mining.utility.Stemmer;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNetUtility {

	public WordNetUtility() {
		try {
			JWNL.initialize(new FileInputStream(Config.PATH_JWNL_CONFIG));
		} catch (FileNotFoundException | JWNLException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public boolean canBeNoun(String word) {
		try {
			IndexWordSet idxWord = Dictionary.getInstance().lookupAllIndexWords(word);
			IndexWord[] words = idxWord.getIndexWordArray();
			for (int i = 0; i < words.length; i++) {
				if (words[i].getPOS() == POS.NOUN) {
					return true;
				}
			}
		} catch (JWNLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public double isFirstNoun(String word1, String word2) {
		try {

			IndexWord[] idxWords1 = Dictionary.getInstance().lookupAllIndexWords(word1).getIndexWordArray(),
					idxWords1ex = Dictionary.getInstance().lookupAllIndexWords(Stemmer.getStem(word1))
							.getIndexWordArray();
			IndexWord[] idxWords2 = Dictionary.getInstance().lookupAllIndexWords(word2).getIndexWordArray(),
					idxWords2ex = Dictionary.getInstance().lookupAllIndexWords(Stemmer.getStem(word2))
							.getIndexWordArray();
			idxWords1 = ArrayUtils.addAll(idxWords1, idxWords1ex);
			idxWords2 = ArrayUtils.addAll(idxWords2, idxWords2ex);
			double score1 = 0d, score2 = 0d, len1 = 0d, len2 = 0d;
			for (int i = 0; i < idxWords1.length; i++) {
				if (idxWords1[i].getPOS() == POS.NOUN) {
					score1 += idxWords1[i].getSenseCount();
				}
				len1 += idxWords1[i].getSenseCount();
			}
			score1 /= len1;
			for (int i = 0; i < idxWords2.length; i++) {
				if (idxWords2[i].getPOS() == POS.NOUN) {
					score2 += idxWords2[i].getSenseCount();
				}
				len2 +=  idxWords2[i].getSenseCount();
			}
			score2 /= len2;
			return score1 - score2;
		} catch (JWNLException e) {
			e.printStackTrace();
			return 0;
		}

	}

	public int measureNounSimilarity(String word1, String word2) {
		try {
			IndexWord iword1 = Dictionary.getInstance().lookupIndexWord(POS.NOUN, word1);
			IndexWord iword2 = Dictionary.getInstance().lookupIndexWord(POS.NOUN, word2);

			int min_depth = Integer.MAX_VALUE;
			for (int i = 1; i <= iword1.getSenseCount(); i++) {
				for (int j = 1; j <= iword2.getSenseCount(); j++) {
					RelationshipList list = RelationshipFinder.getInstance().findRelationships(iword1.getSense(i),
							iword2.getSense(j), PointerType.HYPERNYM);
					int depth = ((Relationship) list.get(0)).getDepth();
					if (min_depth > depth) {
						min_depth = depth;
					}
				}
			}
			return min_depth;
		} catch (JWNLException e) {
			e.printStackTrace();
			return -1;
		}

	}
}
