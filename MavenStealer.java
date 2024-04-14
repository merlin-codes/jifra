import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
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
		System.out.println("deleted "+dir);
	}
	public static void stealLibs(String key, Map<String, String> map) {
		System.out.println("--------------------------------------------------");

		if (new File(key).mkdirs()) System.out.println("dependencies cloning for "+key);
		if (Toml.Info.libs == null) {
			System.out.println(key+" is not defined in app.toml");
			return;
		}

		map.forEach((k, v) -> {
			if (v == "" || (map.get("support") == null 
				&& Toml.Info.maven == null)) return;
			MavenStealer steal = new MavenStealer(Toml.Info.maven, key+"/");
			if (v.contains(":")) {
				String[] parts = v.split(":");
				steal.wget(k, parts[0], parts[1]);
			} else steal.wget(k, v, null);
		});
	}
	public static void localLibs(String key) {
		System.out.println("------------------------------------------");
		System.out.println("Copying Local Dependencies");
		if (new File(key).mkdirs()) System.out.println("dependencies cloning for "+key);
		if (Toml.Info.localLibs != null)
		Toml.Info.localLibs.forEach((i, v) -> {
			if (i == "" || i.isEmpty() || v == "" || v.isEmpty()) return;
			FileControl.copy(v.replace("\"", ""), key+"/"+i+".jar");
		});
	}
	public MavenStealer(String origin, String root) { this.origin = origin; this.root = root; }
	public void wget(String name, String group, String version) {
		String url = origin.replace("name", name).replace("group", group.replace(".", "/"));
		if (version == null) version = getLatest(url.split("version")[0]); 
		url = url.replace("version", version);
		stealFile(name+".jar", url);
	}
	public String getLatest(String url) {
		stealFile("maven-metadata.xml", url.replace("\"", "")+"maven-metadata.xml");
		var read = FileControl.read("maven-metadata.xml");
		return read.substring(read.indexOf("<latest>")+8, read.indexOf("</latest>"));
	}

	/**
	 * Just another shortcut - Steals a file from a url
	 * @param name - name of the file
	 * @param url - url of the file
	 */
	public void stealFile(String name, String url) { stealFile(name, url, this.root); }

	/**
	 * Steals a file from a url
	 * @param name - name of the file
	 * @param url - url of the file
	 * @param source - destination folder
	 */
	public static void stealFile(String name, String url, String source) {
		try {
			BufferedInputStream in = new BufferedInputStream(
					(new URI(url.replace("\"", ""))).toURL().openStream()); 
			FileOutputStream fileOutputStream = new FileOutputStream(source+name);
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}
			fileOutputStream.close();
			in.close();
			System.out.println(name);
		} catch (URISyntaxException e) { 
			System.out.println("Exception: " + e); 
			System.out.println("File getting not working: "+
					source+" url getting from: "+url.replace("\"", ""));
		} catch (IOException e) { 
			System.out.println("Exception: " + e); 
			System.out.println("File getting not working: "+
					source+" url getting from: "+url.replace("\"", ""));
		}
	}
}
