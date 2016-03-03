package medical_classify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ClassifyByDisEx {
	ProcessCodes processCodes = new ProcessCodes();

	void classifyTask(double threshold) {
		double grid1 = 0d, grid2 = 0d, grid3 = 0d;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("data/data_neg2.txt")));
			String line = null;
			while (null != (line = reader.readLine())) {
				String[] pair = line.split("\\t");
				String doc = pair[1];
				// String gt_codes = pair[0];
				ArrayList<String> gt_codes = new ArrayList<>();
				for (String code : pair[0].split(",")) {
					gt_codes.add(code.replace("'", "").replace("[", "").replace("]", "").trim());
				}
				List<String> assign_codes = classifyDoc(doc, threshold);

				for (String assign_code : assign_codes) {
					if (!gt_codes.contains(assign_code)) {
						System.out.println(doc);
						grid2++;
					}else{
						grid1++;
					}
				}
				
				for (String gt_code : gt_codes) {
					if (!assign_codes.contains(gt_code)) {
						System.out.println(doc);
						grid3++;
					}
				}

			}
			reader.close();

			System.out.println(grid1);
			System.out.println(grid2);
			System.out.println(grid3);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> classifyDoc(String doc, double threshold) {
		TreeMap<String, Double> output = new TreeMap<>();
		TreeMap<String, Double> scores = new TreeMap<>();

		HashMap<Integer, HashMap<String, List<String>>> code_map = processCodes.code_map;

		String[] tokens = doc.split(" ");
		for (int idx = 0; idx < tokens.length; idx++) {

			for (int len = 1; len <= 18; len++) {
				if (idx + len <= tokens.length) {
					String phrase = "";
					for (int loop = idx; loop < idx + len; loop++) {
						phrase += " " + tokens[loop];
					}
					phrase = Stemmer.getStem(phrase.trim());

					for (int len_loop_map : code_map.keySet()) {
						for (String code : code_map.get(len_loop_map).keySet()) {
							List<String> descs = code_map.get(len_loop_map).get(code);
							for (String desc : descs) {
								double score = minDistance(phrase, desc);
								if (score > 0) {
									if (score == 1 && code.equals("518.0")) {
										// System.out.println("1");
									}
									output.put(code + " -> " + desc + " -> " + phrase, score);
									if (!scores.containsKey(code)) {
										scores.put(code, score);
									} else if (scores.get(code) < score) {
										scores.put(code, score);
									}
								}
							}
						}
					}
				}

			}

		}

		ArrayList<Map.Entry<String, Double>> list = new ArrayList<>();
		list.addAll(output.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return (int) (1000000000 * (o2.getValue() - o1.getValue()));
			}
		});
		// for (int i = 0; i <= 30; i++) {
		// Map.Entry<String, Double> item = list.get(i);
		// System.out.println(item.getKey() + ":" + item.getValue());
		// }

		ArrayList<Map.Entry<String, Double>> list2 = new ArrayList<>();
		list2.addAll(scores.entrySet());
		Collections.sort(list2, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return (int) (1000000000 * (o2.getValue() - o1.getValue()));
			}
		});

		ArrayList<String> assign_codes = new ArrayList<>();
		for (Entry<String, Double> entry : list2) {
			if (entry.getValue() >= threshold) {
				assign_codes.add(entry.getKey());
			}
		}
		return assign_codes;
	}

	public double minDistance(String word1, String word2) {

		String[] word1set = word1.split(" "), word2set = word2.split(" ");
		HashSet<String> unionset = new HashSet<>();
		double intersection_count = 0;
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
		ClassifyByDisEx classifyByDis = new ClassifyByDisEx();
		classifyByDis.classifyTask(1);

		List<String> codes = classifyByDis.classifyDoc(
				"cough .   focal   . findings consistent with  .", 1);
		for (String code : codes) {
			System.out.println(code);
		}
	}
}
