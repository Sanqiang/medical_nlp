package edu.pitt.medical_nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.pitt.medical_nlp.utility.WordNetUtility;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class Process {
	protected ArrayList<String> _docs = null;
	protected HashMap<String, Integer> _aspects = null;
	protected HashMap<String, String> _mrconso, _mrsty = null;
	protected HashMap<String, String> _types = null;
	protected HashSet<String> add_features = new HashSet<>();
	protected HashMap<String, String> _exchange_cases = null;
	protected static String[] exclude_words = { "is", "are", "or", ".", ",", "-", "_", "was", "were" };
	public static List<String> exclude_words_list = Arrays.asList(exclude_words);

	public Process() {
		this._docs = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(Config.PATH_DATA)));
			String line = null;
			while (null != (line = reader.readLine())) {
				String[] items = line.split("\t");
				this._docs.add(items[2]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("processMrconso");
		this.processMrconso();
		System.out.println("processMrsty");
		this.processMrsty();
		System.out.println("processTypes");
		this.processTypes();
		System.out.println("extractAspect");
		this.extractAspect();
		this.processExchangeCases();
	}

	void processExchangeCases() {
		_exchange_cases = new HashMap<>();
		_exchange_cases.put("rule out", "no");
	}

	void processMrsty() {
		try {
			this._mrsty = new HashMap<>();
			BufferedReader reader = new BufferedReader(new FileReader(new File(Config.MRSTY)));
			String line = null;
			while (null != (line = reader.readLine())) {
				String[] items = line.split("\\|");
				String type = items[3].replace(" ", "").replace(",", "");
				String id = items[0];
				_mrsty.put(id, type);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void processMrconso() {
		try {
			this._mrconso = new HashMap<>();
			BufferedReader reader = new BufferedReader(new FileReader(new File(Config.MRCONSO)));
			String line = null;
			while (null != (line = reader.readLine())) {
				String[] items = line.split("\\|");
				String keyword = items[14].toLowerCase();
				String id = items[0];
				_mrconso.put(keyword, id);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void processTypes() {
		try {
			this._types = new HashMap<>();
			BufferedReader reader = new BufferedReader(new FileReader(new File(Config.PATH_TYPE_LABELED)));
			String line = null;
			while (null != (line = reader.readLine())) {
				String[] pair = line.split("\t");
				if (pair[1].equals("1")) {
					_types.put(pair[0].replace(" ", "").replace(",", ""), "adj");
				} else {
					_types.put(pair[0].replace(" ", "").replace(",", ""), "n");
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void extractAspect() {
		this._aspects = new HashMap<>();
		for (String doc : this._docs) {
			extractAspect(doc);
		}
	}

	void extractAspect(String doc) {
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(doc));
		for (List<HasWord> token_part : tokenizer) {
			for (int i = 0; i < token_part.size(); i++) {
				String cur_word = WordNetUtility.getStem(token_part.get(i).word());
				String pattern = cur_word;
				if (_mrconso.containsKey(pattern)) {
					if (!_aspects.containsKey(pattern)) {
						_aspects.put(pattern, 0);
					}
					_aspects.put(pattern, 1 + _aspects.get(pattern));
				}

				if (i + 1 < token_part.size()) {
					pattern += " " + WordNetUtility.getStem(token_part.get(1 + i).word());
					if (_mrconso.containsKey(pattern)) {
						if (!_aspects.containsKey(pattern)) {
							_aspects.put(pattern, 0);
						}
						_aspects.put(pattern, 1 + _aspects.get(pattern));
					}
				}

				if (i + 2 < token_part.size()) {
					pattern += " " + WordNetUtility.getStem(token_part.get(2 + i).word());
					if (_mrconso.containsKey(pattern)) {
						if (!_aspects.containsKey(pattern)) {
							_aspects.put(pattern, 0);
						}
						_aspects.put(pattern, 1 + _aspects.get(pattern));
					}
				}

				if (i + 3 < token_part.size()) {
					pattern += " " + WordNetUtility.getStem(token_part.get(3 + i).word());
					if (_mrconso.containsKey(pattern)) {
						if (!_aspects.containsKey(pattern)) {
							_aspects.put(pattern, 0);
						}
						_aspects.put(pattern, 1 + _aspects.get(pattern));
					}
				}

				if (i + 4 < token_part.size()) {
					pattern += " " + WordNetUtility.getStem(token_part.get(4 + i).word());
					if (_mrconso.containsKey(pattern)) {
						if (!_aspects.containsKey(pattern)) {
							_aspects.put(pattern, 0);
						}
						_aspects.put(pattern, 1 + _aspects.get(pattern));
					}
				}
			}
		}
	}

	public ArrayList<String> processDocs() {
		ArrayList<String> ndocs = new ArrayList<>();
		for (String doc : this._docs) {
			ndocs.add(processDocsReversePhrase(doc));
		}
		return ndocs;
	}

	public String processDocs(String doc) {
		for (String base_case : _exchange_cases.keySet()) {
			String update_case = _exchange_cases.get(base_case);
			doc = doc.replace(base_case, update_case);
		}
		boolean addphrase = true, addtype = true, add_raw_text = false;
		HashSet<String> types = new HashSet<>();

		doc = doc.toLowerCase();
		StringBuilder ndoc = new StringBuilder();
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(doc));
		for (List<HasWord> token_part : tokenizer) {
			for (int i = 0; i < token_part.size(); i++) {

				if (exclude_words_list.contains(token_part.get(i).word())) {
					ndoc.append(token_part.get(i).word()).append(" ");
					continue;
				}

				boolean is_continue = true;
				if (i + 4 < token_part.size()
						&& _aspects.containsKey(WordNetUtility.getStem(WordNetUtility.getStem(token_part.get(i).word()))
								+ " " + WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 3).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 4).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 3).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 4).word())));
					if (addphrase && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 3).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 4).word()))
								/* .append("_").append(type) */.append(" ");
						is_continue = false;
						i += 4;
						types.add(type);
					}
					if (add_raw_text) {

						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 3).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 4).word())).append(" ");
						is_continue = false;
						i += 4;
					}

				} else if (i + 3 < token_part.size()
						&& _aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 3).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 3).word())));
					if (addphrase && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 3).word()))
								/* .append("_").append(type) */.append(" ");
						is_continue = false;
						i += 3;
						types.add(type);
					}
					if (add_raw_text) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 3).word())).append(" ");
						is_continue = false;
						i += 3;
					}
				} else if (i + 2 < token_part.size()
						&& _aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 2).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 2).word())));
					if (addphrase && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word()))
								/* .append("_").append(type) */.append(" ");
						is_continue = false;
						i += 2;
						types.add(type);
					}
					if (add_raw_text) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append(" ");
						is_continue = false;
						i += 2;
					}
				} else if (i + 1 < token_part.size()
						&& _aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 1).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word())));
					if (addphrase && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 1)
										.word()))/* .append("_").append(type) */
								.append(" ");
						is_continue = false;
						i += 1;
						types.add(type);
					}
					if (add_raw_text) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append(" ");
						is_continue = false;
						i += 1;
					}
				} else if (_aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word())));
					if (_types.get(type) != null && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word()))
								/* .append("_").append(type) */.append(" ");
						is_continue = false;
						types.add(type);
					}
				}
				if (is_continue) {
					ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ");
				}

			}
		}
		if (addtype) {
			add_features.addAll(types);
		}
		return ndoc.toString();
	}

	public String processDocsReversePhrase(String doc) {
		for (String base_case : _exchange_cases.keySet()) {
			String update_case = _exchange_cases.get(base_case);
			doc = doc.replace(base_case, update_case);
		}
		boolean addphrase = true, addtype = true, add_raw_text = false;
		HashSet<String> types = new HashSet<>();

		doc = doc.toLowerCase();
		StringBuilder ndoc = new StringBuilder();
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(doc));
		for (List<HasWord> token_part : tokenizer) {
			for (int i = 0; i < token_part.size(); i++) {

				if (exclude_words_list.contains(token_part.get(i).word())) {
					ndoc.append(token_part.get(i).word()).append(" ");
					continue;
				}

				boolean is_continue = true;
				if (_aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word())));
					if (_types.get(type) != null && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word()))
								/* .append("_").append(type) */.append(" ");
						is_continue = false;
						types.add(type);
					}
				} else if (i + 1 < token_part.size()
						&& _aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 1).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word())));
					if (addphrase && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 1)
										.word()))/* .append("_").append(type) */
								.append(" ");
						is_continue = false;
						i += 1;
						types.add(type);
					}
					if (add_raw_text) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append(" ");
						is_continue = false;
						i += 1;
					}
				} else if (i + 2 < token_part.size()
						&& _aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 2).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 2).word())));
					if (addphrase && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word()))
								/* .append("_").append(type) */.append(" ");
						is_continue = false;
						i += 2;
						types.add(type);
					}
					if (add_raw_text) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append(" ");
						is_continue = false;
						i += 2;
					}
				} else if (i + 3 < token_part.size()
						&& _aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 3).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 3).word())));
					if (addphrase && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 3).word()))
								/* .append("_").append(type) */.append(" ");
						is_continue = false;
						i += 3;
						types.add(type);
					}
					if (add_raw_text) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 3).word())).append(" ");
						is_continue = false;
						i += 3;
					}
				} else if (i + 4 < token_part.size()
						&& _aspects.containsKey(WordNetUtility.getStem(WordNetUtility.getStem(token_part.get(i).word()))
								+ " " + WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 3).word()) + " "
								+ WordNetUtility.getStem(token_part.get(i + 4).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 3).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 4).word())));
					if (addphrase && _types.get(type).equals("n")) {
						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 3).word())).append("_")
								.append(WordNetUtility.getStem(token_part.get(i + 4).word()))
								/* .append("_").append(type) */.append(" ");
						is_continue = false;
						i += 4;
						types.add(type);
					}
					if (add_raw_text) {

						ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 3).word())).append(" ")
								.append(WordNetUtility.getStem(token_part.get(i + 4).word())).append(" ");
						is_continue = false;
						i += 4;
					}

				}
				if (is_continue) {
					ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ");
				}

			}
		}
		if (addtype) {
			add_features.addAll(types);
		}
		return ndoc.toString();
	}

	public void printAspects(int threshold) {
		try {
			HashSet<String> types = new HashSet<>();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("aspects.txt")));
			for (String aspect : _aspects.keySet()) {
				if (_aspects.get(aspect) > threshold) {
					String type = _mrsty.get(_mrconso.get(aspect));
					String output = aspect + ":" + _aspects.get(aspect) + ":" + type;
					types.add(type);
					writer.write(output + "\n");
					writer.flush();
					System.out.println(output);
				}
			}
			writer = new BufferedWriter(new FileWriter(new File("type.txt")));
			for (String type : types) {
				writer.write(type);
				writer.write("\t");
				writer.write("0");
				writer.write("\n");
			}
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
