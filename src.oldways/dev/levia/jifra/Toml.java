package dev.levia.jifra;

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
		catch (Exception e) { System.out.println(e); scanner.close(); return Map.of(); }

		String key = "root";
		Map<String, String> map = new HashMap<String, String>();
		Map<String, Map<String, String>> origin = 
				new HashMap<String, Map<String, String>>();
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
		// this values are correct
		Toml.Info.init(origin.get("root").get("name"), 
				origin.get("root").get("version"), 
				origin.get("root").get("group"));

		Toml.Info.support(origin.get("support").get("maven"), 
				origin.get("support").get("local"), 
				origin.get("support").get("search"));

		Toml.Info.libs(origin.get("libs"), origin.get("local"), origin.get("test-libs"));
		System.out.println("(Project::Parse) Toml info group is: `"+Toml.Info.group+"`");
		return origin;
	}
	public class Info {
		public static String name = "";
		public static String version = "";
		public static String group = "";
		public static String maven = "";
		public static String local = "";
		public static String search = "";

		public static Map<String, String> libs = Map.of();
		public static Map<String, String> localLibs = Map.of();
		public static Map<String, String> testLibs = Map.of();

		public static void init(String name, String version, String group) {
			Info.name = name; Info.version = version; Info.group = group;
		}
		public static void support(String maven, String local, String search) {
			Info.maven = maven; Info.local = local; Info.search = search;
		}
		public static void libs(Map<String, String> libs, 
				Map<String, String> local, Map<String, String> test) {
			Info.libs = libs == null ? Map.of() : libs; 
			Info.localLibs = local == null ? Map.of() : local; 
			Info.testLibs = test == null ? Map.of() : test;
		}
		public static String dirGroup() { return Info.group.replace(".", "/"); }

	}
}


