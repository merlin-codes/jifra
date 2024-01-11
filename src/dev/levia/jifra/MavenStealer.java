package dev.levia.jifra;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import java.net.URI;

public class MavenStealer {
	public String origin;
	public String root;
	public static void deleteRecursive(String index, String[] list) {
		for(String s: list) {
			File currentFile = new File(index+"/"+s);
			if (currentFile.isDirectory()) deleteRecursive(index+"/"+s, currentFile.list());
			currentFile.delete();
		}
	}
	public static void deleteDir(String dir) {
		File index = new File(dir);
		String[] entries = index.list();
		if (entries == null) return;
		MavenStealer.deleteRecursive(dir, entries);

		if (!index.delete()) System.out.println("failed to delete "+dir);
	}
	public static void stealLibs(String key, Map<String, Map<String, String>> map) {
		System.out.println("--------------------------------------------------");
		if (new File(key).mkdirs()) System.out.println("dependencies cloning for "+key);
		if (map.get("libs") == null) System.out.println(key+" is not defined in app.toml");
		for (Map.Entry<String, String> entry : map.get(key).entrySet()) {
			if (entry.getValue() == "" || (map.get("support") == null && map.get("support").get("maven") == null) ) continue;
			System.out.println("Support: "+Arrays.toString(map.get("support").entrySet().toArray()));
			MavenStealer steal = new MavenStealer(map.get("support").get("maven"), key+"/");
			if (entry.getValue().contains(":")) {
				String[] parts = entry.getValue().split(":");
				steal.wget(entry.getKey(), parts[0], parts[1]);
			} else steal.wget(entry.getKey(), entry.getValue(), null);
		}
	}
	public static void localLibs(String key, Map<String, String> map) {
		System.out.println("------------------------------------------");
		System.out.println("Copying Local Dependencies");
		if (new File(key).mkdirs()) System.out.println("dependencies cloning for "+key);
		try {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				try {
					if (entry.getValue() == "" || entry.getKey() == "") continue;
					Files.copy(Paths.get(entry.getValue().replace("\"", "")), Paths.get(key+"/"+entry.getKey()+".jar"));
				} catch (IOException e) {
					System.out.println("Problem with copying "+entry.getValue()+" to "+key+"/"+entry.getKey());
					e.printStackTrace();
				}
			}
		} catch (NullPointerException e) {
			System.out.println("WARNING: Missing Local Libs if you do not use them ignore this");
		}
	}
	public MavenStealer(String origin, String root) { this.origin = origin; this.root = root; }
	public void wget(String name, String group, String version) {
		String url = origin.replace("name", name).replace("group", group.replace(".", "/"));
		if (version == null) version = getLatest(url.split("version")[0]); 
		url = url.replace("version", version);
		stealFile(name+".jar", url);
	}
	public String getLatest(String url) {
		try {
			BufferedInputStream in = getBufferedURL(url+"maven-metadata.xml");
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			String latest = "";
			String read = "";
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				read += new String(dataBuffer, 0, bytesRead);
			}
			read.substring(read.indexOf("<latest>")+8, read.indexOf("</latest>"));
			latest = read.substring(read.indexOf("<latest>")+8, read.indexOf("</latest>"));
			in.close();
			return latest;
		} catch (IOException e) {
			System.out.println("Exception: " + e);
			System.out.println("File getting not working: "+this.root+" url getting from: "+url+"maven-metadata.xml");
		}
		return "";
	}
	public BufferedInputStream getBufferedURL(String url) {
		try { return new BufferedInputStream((new URI(url.replace("\"", ""))).toURL().openStream()); } 
		catch (IOException e) { 
			System.out.println("Exception: " + e); System.out.println("File getting not working: "+this.root+" url getting from: "+url.replace("\"", ""));
		} catch (URISyntaxException e) { 
			System.out.println("Exception: " + e); System.out.println("File getting not working: "+this.root+" url getting from: "+url.replace("\"", ""));
		}
		return null;
	}
	public void stealFile(String name, String url) {
		try {
			BufferedInputStream in = getBufferedURL(url);
			FileOutputStream fileOutputStream = new FileOutputStream(this.root+name);
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}
			fileOutputStream.close();
			in.close();
			System.out.println(name);
		} catch (IOException e) { 
			System.out.println("Exception: " + e); System.out.println("File getting not working: "+this.root+" url getting from: "+url.replace("\"", ""));
		}
	}
}
