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
	protected String[] exclude_words = { "is", "are", "or", ".", ",", "-", "_" };

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
				if (pair[1] == "1") {
					_types.put(pair[0], "adj");
				} else {
					_types.put(pair[0], "n");
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
			ndocs.add(processDocs(doc));
		}
		return ndocs;
	}

	public String processDocs(String doc) {
		List<String> exclude_words_list = Arrays.asList(exclude_words);
		doc = doc.toLowerCase();
		StringBuilder ndoc = new StringBuilder();
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(doc));
		for (List<HasWord> token_part : tokenizer) {
			for (int i = 0; i < token_part.size(); i++) {
				if (exclude_words_list.contains(token_part.get(i).word())) {
					ndoc.append(token_part.get(i).word()).append(" ");
					continue;
				}

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
					ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 3).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 4).word()))
							/* .append("_").append(type) */.append(" ");
					i += 4;
					continue;
				}
				if (i + 3 < token_part.size() && _aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word())
						+ " " + WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
						+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
						+ WordNetUtility.getStem(token_part.get(i + 3).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 2).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 3).word())));
					ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 2).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 3).word()))
							/* .append("_").append(type) */.append(" ");
					i += 3;
					continue;
				}
				if (i + 2 < token_part.size() && _aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word())
						+ " " + WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
						+ WordNetUtility.getStem(token_part.get(i + 2).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word())) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 2).word()));
					ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 1).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 2).word()))
							/* .append("_").append(type) */.append(" ");
					i += 2;
					continue;
				}
				if (i + 1 < token_part.size() && _aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word())
						+ " " + WordNetUtility.getStem(token_part.get(i + 1).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word()) + " "
							+ WordNetUtility.getStem(token_part.get(i + 1).word())));
					ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append("_")
							.append(WordNetUtility.getStem(token_part.get(i + 1).word()))/*.append("_").append(type)*/
							.append(" ");
					i += 1;
					continue;
				}
				if (_aspects.containsKey(WordNetUtility.getStem(token_part.get(i).word()))) {
					String type = _mrsty.get(_mrconso.get(WordNetUtility.getStem(token_part.get(i).word())));
					ndoc.append(WordNetUtility.getStem(token_part.get(i).word()))
							/* .append("_").append(type) */.append(" ");
					continue;
				}

				ndoc.append(WordNetUtility.getStem(token_part.get(i).word())).append(" ");
			}
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