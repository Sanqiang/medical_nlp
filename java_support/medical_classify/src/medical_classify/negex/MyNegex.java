package medical_classify.negex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class MyNegex {
	public static void processNegex() throws Exception {
		HashSet<String> rules = new HashSet<>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File("data/test_trigger.txt")));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data/data_neg.txt")));
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			rules.add(line.split("\\t")[0]);
		}
		
		reader = new BufferedReader(new FileReader(new File("data/data.txt")));
		
		while ((line = reader.readLine()) != null) {
			line = line.toLowerCase();
			for (String rule : rules) {
				if(line.contains(rule)){
					line = line.replaceAll(rule, "no");
				}
			}
			writer.write(line);
			writer.write("\n");
		}
		
		
		reader.close();
		writer.close();
	}
	
	public static void main(String[] args) throws Exception {
		processNegex();
	}
}