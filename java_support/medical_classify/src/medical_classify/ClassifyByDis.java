package medical_classify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ClassifyByDis {
	ProcessCodes processCodes = new ProcessCodes();

	public void classifyDoc(String doc) {
		TreeMap<String, Double> scores = new TreeMap<>();

		HashMap<Integer, HashMap<String, List<String>>> code_map = processCodes.code_map;

		String[] tokens = doc.split(" ");
		for (int idx = 0; idx < tokens.length; idx++) {
			for (int len : code_map.keySet()) {
				if (idx + len <= tokens.length) {
					String phrase = "";
					for (int loop = idx; loop < idx + len; loop++) {
						phrase += " " + tokens[loop];
					}
					phrase = Stemmer.getStem(phrase.trim());
					
					for (String code : code_map.get(len).keySet()) {
						if (code.equals("786.2")) {
							//System.out.println("2");
						}
						List<String> descs = code_map.get(len).get(code);
						for (String desc : descs) {
							if (desc.contains("cough")) {
								//System.out.println("1");
							}
							double score = minDistance(phrase, desc);
							if (score > 0) {
								scores.put(code + " -> " + desc + " -> " + phrase, score);
							}

						}
					}
				}
			}

		}

		ArrayList<Map.Entry<String, Double>> list = new ArrayList<>();
		list.addAll(scores.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return (int) (1000000000 * (o2.getValue() - o1.getValue()));
			}
		});
		for (int i = 0; i <= 3; i++) {
			Map.Entry<String, Double> item = list.get(i);
			System.out.println(item.getKey() + ":" + item.getValue());
		}
	}

	public double minDistance(String word1, String word2) {
		
		String[] word1set = word1.split(" "), word2set = word2.split(" ");
		HashSet<String> unionset = new HashSet<>();
		int intersection_count = 0;
		for (String token1 : word1set) {
			unionset.add(token1);
			for (String token2 : word2set) {
				if (token1.equals(token2)) {
					++intersection_count;
				}
				unionset.add(token2);
			}
		}
		
		if (unionset.isEmpty()) {
			return 0d;
		}
		
		return intersection_count / unionset.size();
	}

	public static void main(String[] args) {
		ClassifyByDis classifyByDis = new ClassifyByDis();
		classifyByDis.classifyDoc("nearly 6-month - old with recent uti . normal renal ultrasound including the bladder . ");
	}
}
