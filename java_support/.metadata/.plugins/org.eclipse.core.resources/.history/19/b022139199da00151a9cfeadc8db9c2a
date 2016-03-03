package medical_classify;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;

public class GoogleCount {
	private static long getResultsCount(final String query) throws IOException {
		final URL url;
		url = new URL("https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8"));
		final URLConnection connection = url.openConnection();

		connection.setConnectTimeout(60000);
		connection.setReadTimeout(60000);
		connection.addRequestProperty("User-Agent", "Google Chrome/36");

		final Scanner reader = new Scanner(connection.getInputStream(), "UTF-8");

		while (reader.hasNextLine()) { // for each line in buffer
			final String line = reader.nextLine();

			if (!line.contains("\"resultStats\">"))
				continue;

			try {
				return Long.parseLong(line.split("\"resultStats\">")[1].split("<")[0].replaceAll("[^\\d]", ""));
			} finally {
				reader.close();
			}
		}
		reader.close();
		return 0;
	}

	public static void main(String[] args) throws Exception {
		String word1 = "\"" + "flank pain" + "\"";
		String word2 = "\"" + "flank pain" + "\"";

		long count = GoogleCount.getResultsCount(word1 + " " + word2), count1 = GoogleCount.getResultsCount(word1),
				count2 = GoogleCount.getResultsCount(word2);
		System.out.println(count / (count1 * count2));
	}
}
