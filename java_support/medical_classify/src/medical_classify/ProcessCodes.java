package medical_classify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProcessCodes {

	//public HashMap<String, List<String>> code_map = new HashMap<>();
	public HashMap<Integer, HashMap<String, List<String>>> code_map = new HashMap<>();
	
	
	public ProcessCodes() {
		try {
			for (int i = 1; i < 18; i++) {
				code_map.put(i, new HashMap<>());
			}
			
			BufferedReader reader = new BufferedReader(new FileReader(new File("data/code.txt")));
			String line = null;
			while (null != (line = reader.readLine())) {
				if ("==END==".equals(line)) {
					break;
				}
				String code = line.split(":")[0].trim();
				String[] descs = line.split(":")[1].split("\\t");

				
				for (String desc : descs) {
					desc = desc.toLowerCase().trim();
					desc = Stemmer.getStem(desc);
					int len = desc.split(" ").length;
					
					if (!code_map.get(len).containsKey(code)) {
						code_map.get(len).put(code, new ArrayList<>());
					}
					
					code_map.get(len).get(code).add(desc);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new ProcessCodes();
	}
}