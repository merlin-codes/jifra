import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Project {
	Map<String, Map<String, String>> map;
	public Project() {
		map = Files.exists(Paths.get("app.toml")) ? 
			Toml.parse("app.toml") : new HashMap<String, Map<String, String>>();
	}

	/**
	 * deletes folders and files generated from the `JiFra`
	 * @param libs if the libs should be deleted
	 */
	public void clean(boolean libs) {
		if (map.get("root").get("clean") != null) {
			if (libs) this.deleteLibsFolders();
			this.deleteIfEndsWith(".", new File(".").list(), ".class", "target");
		}
	}

	/**
	 * copies jar file to local location
	 * @param is if the file should be replaced in the location
	 */
	public void copyJar(boolean is) {
		var name = this.map.get("root").get("name");
		var to = this.map.get("support").get("local");
		System.out.println(name+".jar"+" "+to+"/"+name+".jar");
		if (!new File(to).exists()) new File(to).mkdirs();
		try {
			if (is) new File(to+"/"+name+".jar").delete();
			Files.copy(Paths.get(name+".jar"), Paths.get(to+"/"+name+".jar"));
		} catch (FileAlreadyExistsException e) {
			this.err(e, "Failed to copy to location "+to+"/"+name+".jar because file already exists");
			System.out.println("overwrite the file, using `jifra save`");
		} catch (Exception e) {
			this.err(e, "Failed to copy app.jar");
		}
	}
	public void clean() { clean(false); }
	public void cleanAfter() {
		FileControl.deleteDir("src");
		FileControl.deleteDir("target");
		if (map.get("root").get("clean") != null) {
			this.deleteIfEndsWith("libs", new File(".").list(), "", "");
			this.deleteIfEndsWith("test-libs", new File(".").list(), "", "");
		}
	}

	/**
	 * downloads all dependencies and copies jar files from local
	 */
	public void installDeps() { 
		MavenStealer.stealLibs("libs", this.map); 
		MavenStealer.stealLibs("test-libs", this.map); 
		MavenStealer.localLibs("local-libs", this.map.get("local-libs"));
	}

	/**
	 * deletes folders and files generated from the `JiFra`
	 */	
	public void cleanDeps() { 
		MavenStealer.deleteDir("libs"); 
		MavenStealer.deleteDir("test-libs"); 
		MavenStealer.deleteDir("local-libs"); 
	}
	public void help() {
		System.out.println(DefaultFile.getSomeHelp());
	}
	public void init(String name) {
		var title = name;
		if (name == null) title = System.getProperty("user.dir");
		try {
			Files.write(Paths.get("app.toml"), DefaultFile.getAppToml(title));
			Files.write(Paths.get("Main.java"), DefaultFile.getMainJava());
		} catch (Exception e) { 
			this.errOut(e, "Failed to init of app named "+title); 
		}
	}
	public void makeJar() {
		List<String> fully = List.of(new File("libs").list());
		fully.addAll(List.of(new File("test-libs").list()));
		OwlControl.compileCmds(fully.toArray(new String[0]));
	}
	public void runBash(String cmd, String msg, String err) {
		runBash(cmd, msg, err, null);
	}
	public void runBash(String cmd, String msg, String err, File chdir) {
		try {
			Process p = Runtime.getRuntime().exec(cmd.split(" "), null, chdir);
			p.waitFor();
			this.printProcessError(p);
		} catch (Exception e) {
			e.printStackTrace(); 
			System.out.println(msg); 
		}
	}
	public void makeWar() {
		OwlControl.buildArchive(Simple.arrToOne(
			new File("libs").list(), new File("local-libs").list()
		), this.map.get("root").get("name")+".war");
		this.runBash(
			"jar cmvf target/META-INF/MANIFEST.MF "+this.map.get("root").get("name")+".war -C target .",
			"Done making war file", "Failed to make war file"
		);
	}
	public void compileWeb() {
		var name = this.map.get("root").get("name");
		System.out.println(
				"Name: "+name+" Group: "+this.map.get("root").get("group"));

		MavenStealer.deleteDir("target");
		Simple.mkdirs("target", "target/META-INF", "target/WEB-INF", 
			"target/WEB-INF/classes", "target/WEB-INF/lib");
		Simple.w("target/META-INF/MANIFEST.MF", DefaultFile.getManifestXml("Main"));
		try {
			this.makeMetaMF();
			Simple.w("target/WEB-INF/web.xml",
				DefaultFile.getWebToml(name, 
					this.map.get("root").get("group")));
			Simple.w("target/META-INF/MANIFEST.MF", 
					DefaultFile.getManifestXml("Main"));
			this.structure(false);
			this.compile("target/WEB-INF/classes");
			Simple.w("target/META-INF/context.xml", 
					DefaultFile.getContextXml(name));
			if (new File("template").exists()) 
				FileControl.copyFolder("template", "target");
			for (String s: new File("libs").list()) 
				FileControl.copyFolder("libs/"+s, "target/WEB-INF/lib/"+s);
			for (String s: new File("local-libs").list()) { 
				FileControl.copyFolder("local-libs/"+s, 
						"target/WEB-INF/lib/"+s);
			}

		} catch (Exception e) { 
			this.errOut(e, "Failed to compile web app"); 
		}
		System.out.println("Done making war file");
	}
	public void makeDotEnv() {
		var env = this.map.get("env");
		if (env != null) 
			Simple.w(".env", DefaultFile.getDotEnv(env));
	}
	public void makeMetaMF() {
		var main = this.map.get("root").get("group");
		new File("target/META-INF").mkdir();
		Simple.w("target/META-INF/MANIFEST.MF", 
				DefaultFile.getManifestXml(main));
	}
	public void err(Exception e, String msg) { e.printStackTrace(); System.out.println(msg); }
	public void structure(boolean deleteSource) {
		if (deleteSource) MavenStealer.deleteDir("src");
		File index = new File(".");
		String[] entries = index.list();
		if (entries == null) return;
		String group = "src/"+
			this.map.get("root").get("group").replace(".", "/");
		new File(group).mkdirs();
		System.out.println("structure created at: "+group);
		this.inter(index, entries, group);
	}
	public void inter(File index, String[] entries, String group) {
		var listing = FileControl.returnIfEndsWith(index.getPath(), entries, ".java", "libs");
		FileControl.ifEnds(index.getPath(), entries, ".java", "libs", (String s) -> {
			File newFile = new File(index.getPath(), group+"/"+s);
			InputStream is = null;
			OutputStream os = null;
			try {
				is = new FileInputStream(new File(s));
				os = new FileOutputStream(newFile);
				if (!new File("src/"+group).exists()) new File("src/"+group.replace("/", ".")).mkdirs();
				System.out.println("package "+group.replace("src/", "").replace("/", ".")+";");
				os.write(("package "+group.replace("src/", "").replace("/", ".")+";\n\n").getBytes());
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
				is.close();
				os.close();
			} catch (Exception e) { this.errOut(e, "Failed to append package to: "+s); }
			return null;
		});
	}

	/**
	 * Unzips all jar files from libs and local-libs
	 * and copies them to target file with target_libs to show what files were included
	 */
	public void unjar() {
		try {
			String[] libs = new File("libs").exists() ? 
				new File("libs").list() : new String[]{};
			String[] localLibs = new File("local-libs").exists() ? 
				new File("local-libs").list() : new String[]{};
			var root = new File("target_libs");
			if (!root.exists()) root.mkdir();

			for (int i = 0; i < localLibs.length; i++) {
				this.runBash("jar xf "+"../local-libs/"+localLibs[i], "extracting "+localLibs[i], "Failed to extract "+localLibs[i], root);
			}
			for (int i = 0; i < libs.length; i++) {
				this.runBash("jar xf "+"../libs/"+libs[i], "extracting "+libs[i], "Failed to extract "+libs[i], root);
			}
			System.out.println("Copy something: "+root.getName());
			new File("target").mkdirs();
			MavenStealer.deleteDir("target");
			copyDirectory(root.getName(), "target");
		} catch (Exception e) { this.errOut(e, "Failed to unjar"); }
	}

	/**
	 * Copied from reddit so implemented not by me
	 * @param from - directory to copy from (source)
	 * @param to - directory to copy from (destination)
	 */
	public static void copyDirectory(String from, String to) throws IOException {
		Files.walk(Paths.get(from)).forEach(source -> {
				var destination = Paths.get(to, source.toString()
						.substring(from.length()));
			try {
				Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	public void errOut(Exception e, String msg) { e.printStackTrace(); System.out.println(msg); }
	public String getJavaFiles(String uri, String[] entries) {
		if (entries == null) return "";
		var sb = new StringBuilder();
		try {
			for(String s: entries) {
				if (s.endsWith(".java")) {
					sb.append("./"+uri+"/"+s+"\n");
					System.out.println("./"+uri+"/"+s);
				} else if (new File(uri+"/"+s).isDirectory()) {
					sb.append(this.getJavaFiles(uri+"/"+s, new File(uri+"/"+s).list()));
				}
			}
			return sb.toString();
		} catch (Exception e) {
			this.errOut(e, "Failed to get java files");
			return "";
		}
	}
	public void compile(String to) {
		this.compile(to, false);
	}
	public void run() {
		this.runBash(
				"java "+this.getLibs("target")+" "+this.map.get("root").get("group")+".Main", 
				"Failed to run project", "Failed to run project"
		);
	}
	public String getLibs(String end) {
		List<String> libs_e = null;
		try {
			libs_e = List.of(new File("libs").list())
				.stream().map(s -> "libs/"+s)
				.collect(Collectors.toList());
			if (new File("local-libs").list() != null) {
				libs_e.addAll(List.of(new File("local-libs").list()).stream()
					.map(s -> "local-libs/"+s)
					.collect(Collectors.toList())
				);
			}
		} catch (Exception e) {
			System.out.println("Libs not found in `libs` or `local-libs`");
		}
		return libs_e == null ? "-cp "+end : "-cp "+libs_e.stream().collect(Collectors.joining(":"))+":"+end;
	}
	public void compile(String to, boolean deleteSrc) {
		System.out.println("compiling...");
		new File("target").mkdirs();
		this.makeMetaMF();
		try {
			var sources = this.getJavaFiles("src", new File("src").list());
			var sources_files = new BufferedWriter(new FileWriter("src/sources.txt"));
			sources_files.write(sources);
			sources_files.close();
			this.runBash(
					"javac "+getLibs("")+" -d "+to+" @src/sources.txt",
					"Done compiling", "Failed to compile project"
			);
		} catch (Exception e) { this.errOut(e, "Failed to compile project"); }
		if (deleteSrc) this.deleteIfEndsWith(".", (new File(".")).list(), ".class", to);
	}
	void deleteIfEndsWith(String index, String[] list, String endsWith, String ignore) {
		for(String s: list) {
			if (s.endsWith(ignore)) continue;
			if (s.endsWith(endsWith)) {
				File currentFile = new File(index+"/"+s);
				currentFile.delete();
			} else if (new File(index+"/"+s).isDirectory()) {
				this.deleteIfEndsWith(index+"/"+s, new File(index+"/"+s).list(), endsWith, ignore);
			}
		}
	}
	void printProcessError(Process build) throws IOException, InterruptedException {
		var buffer = new BufferedReader(new InputStreamReader(build.getErrorStream()));
		String line;
		while ((line = buffer.readLine()) != null) {
			System.out.println(line);
		}
	}
}
