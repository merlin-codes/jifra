import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.lang.Exception;

import java.util.Scanner;


public class Toml {
	public static void println(Map<String, Map<String, String>> map) {
		for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
			System.out.println(entry.getKey());
			for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) 
				System.out.println("    " + entry2.getKey() + " = " + entry2.getValue());
		}
	}
	public static Map<String, Map<String, String>> parse(String url) {
		File file = new File(url);
		Scanner scanner = null;
		try { scanner = new Scanner(file); } 
		catch (Exception e) { System.out.println(e); return null; }

		String key = "root";
		Map<String, String> map = new HashMap<String, String>();
		Map<String, Map<String, String>> origin = new HashMap<String, Map<String, String>>();
		origin.put(key, map);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (line.contains("#")) { 
				line = line.substring(0, line.indexOf("#")); 
			}
			if (line.startsWith("[") && line.endsWith("]")) {
				key = line.substring(1, line.length() - 1);
				map = new HashMap<String, String>();
				origin.put(key, map);
			} else if (line.contains("=")) {
				String[] arr = line.split("=");
				map.put(arr[0].trim(), arr[1].trim().replace("\"", ""));
			} else {
				origin.get(key).put(line.trim(), "");
			}
		}
		scanner.close();
		return origin;
	}
}


