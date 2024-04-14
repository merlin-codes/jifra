package dev.levia.jifra;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Project {
	Map<String, Map<String, String>> map;
	public Project() {
		map = Files.exists(Paths.get("app.toml")) ? 
			Toml.parse("app.toml") : new HashMap<String, Map<String, String>>();
		System.out.println(Files.exists(Paths.get("app.toml")));
	}

	/**
	 * deletes folders and files generated from the `JiFra`
	 * @param libs if the libs should be deleted
	 */
	public void clean(boolean libs) {
		// @TODO check if clean is set in map if is clean after target and src folder
		if (Toml.Info.maven == null) {
			// if (libs) this.deleteLibsFolders();
			this.deleteIfEndsWith(".", new File(".").list(), ".class", "target");
		}
	}

	public void searchMaven(String text) {
		var searchURI = Toml.Info.search.replace("SEARCHING_FOR", text);
		System.out.println("searching: "+searchURI);
		MavenStealer.stealFile("maven-search.json", searchURI, "");
	}

	private List<String> ignoreExt = List.of( ".txt", ".xml", ".jar");
	public void mapRepo(String repo) {
		MavenStealer.stealFile("maven-search.html", repo, "");
		String read = "";
		try { read = Files.readString(Paths.get("maven-search.html")); } 
		catch (Exception e) {}

		Arrays.stream(read.split("<a "))
			.filter(i -> i.contains("</a>"))
			.forEach(item -> {
				var c = item.split("/</a>")[0].split(">")[1];
				if (c.contains("."))
					System.out.println(c.replaceAll("\\.", "_"));
				ignoreExt.stream()
					.filter(i -> c.contains(i))
					.forEach(i -> System.out.println(c));;
			});
	}

	/**
	 * copies jar file to local location
	 * @param is if the file should be replaced in the location
	 */
	public void copyJar(boolean is) {
		var name = Toml.Info.name;

		var to = Toml.Info.local;
		System.out.println(name+".jar"+" "+to+"/"+name+".jar");
		if (!new File(to).exists()) new File(to).mkdirs();
		try {
			Files.deleteIfExists(Paths.get(to+"/"+name+".jar"));
			Files.copy(Paths.get(name+".jar"), Paths.get(to+"/"+name+".jar"));
			if (is) new File(to+"/"+name+".jar").delete();
		} catch (FileAlreadyExistsException e) {
			Simple.err(e, DefaultFile.Erno.failedJarAlreadyExist(to+"/"+name));
			System.out.println("overwrite the file, using `jifra save`");
		} catch (Exception e) {
			Simple.err(e, "Failed to copy app.jar");
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
	 * Downloads dependencies:
	 *		maven, test_maven, local
	 */
	public void installDeps() { 
		// @TODO check if Toml.Info is empty or not
		MavenStealer.stealLibs("libs", Toml.Info.libs); 
		MavenStealer.stealLibs("test-libs", Toml.Info.testLibs); 
		MavenStealer.localLibs("local-libs");
	}

	/**
	 * deletes folders and files generated from the `JiFra`
	 */	
	public void cleanDeps() { 
		FileControl.deleteDir("libs");
		FileControl.deleteDir("test-libs"); 
		FileControl.deleteDir("local-libs"); 
	}
	public void init(String name) {
		var title = name;
		if (name == null) title = System.getProperty("user.dir");
		try {
			Simple.w("app.toml", DefaultFile.getAppToml(title));
			Simple.w("Main.java", DefaultFile.getMainJava());
		} catch (Exception e) { 
			Simple.err(e, "Failed to init of app named "+title); 
		}
	}
	public void makeJar() {
		List<String> fully = List.of(new File("libs").list());
		if (fully.size() == 0) fully = List.of(new File("local-libs").list());
		if (fully.size() == 0) fully = List.of();
		OwlControl.compileCmds(fully);
	}
	public void makeWar() {
		OwlControl.buildArchive(Simple.arrToOne(
			new File("libs").list(), new File("local-libs").list()
		), Toml.Info.name+".war");
		Simple.runBash(
			DefaultFile.Cmd.compile(Toml.Info.name, "war"),
			DefaultFile.Done.archive("war"), DefaultFile.Erno.FailedWar.val()
		);
	}
	public void compileWeb() {
		System.out.println("Name: "+Toml.Info.name+" Group: "+Toml.Info.group);

		MavenStealer.deleteDir("target");
		try { Files.createDirectory(Paths.get("target")); } 
		catch (Exception e) {Simple.err(e, "Failed to create directory target");}

		// @TODO mkdirs should not need to create every folder before that 
		Simple.mkdirs("target/META-INF", "target/WEB-INF/classes", "target/WEB-INF/lib");
		Simple.w("target/META-INF/MANIFEST.MF", DefaultFile.getManifestXml("Main"));

		this.makeMetaMF();
		this.structure();
		Simple.w("target/WEB-INF/web.xml",
			DefaultFile.getWebToml(Toml.Info.name, Toml.Info.group));
		Simple.w("target/META-INF/MANIFEST.MF", 
				DefaultFile.getManifestXml("Main"));
		this.compile("target/WEB-INF/classes");
		Simple.w("target/META-INF/context.xml", 
				DefaultFile.getContextXml(Toml.Info.name));
		if (new File("template").exists()) 
			FileControl.copyFolder("template", "target");
		for (String s: new File("libs").list()) 
			FileControl.copyFolder("libs/"+s, "target/WEB-INF/lib/"+s);
		Arrays.stream(new File("local-libs").list())
			.forEach(i -> FileControl.copyFolder("local-libs/"+i, "target/WEB-INF/lib/"+i));
		System.out.println("Done making war file");
	}

	// @TODO move to class FileManager
	public void makeDotEnv() {
		var env = this.map.get("env");
		if (env != null) Simple.w(".env", DefaultFile.getDotEnv(env));
	}

	// @TODO move to class FileManager
	public void makeMetaMF() {
		var main = Toml.Info.group;
		new File("target/META-INF").mkdir();
		Simple.w("target/META-INF/MANIFEST.MF", DefaultFile.getManifestXml(main));
	}
	/**
	 * Rebuilds ./src/ folder structure
	 */
	public void structure() {
		FileControl.deleteDir("src");
		(new File("./src")).mkdirs();

		File index = new File(".");

		System.out.println("structure created at: "+Toml.Info.group);
		try {
			System.out.println("Create dir at: "+"src/"+Toml.Info.dirGroup());
			Files.createDirectories(Paths.get("src/"+Toml.Info.dirGroup()));
			System.out.println(new File("src/"+Toml.Info.dirGroup()).exists());
		} catch (Exception e) {
			Simple.err(e, "failed creation of directories group in src folder");
		}
		this.inter(index, index.list());
	}

	/**
	 * Appends package to index
	 * @param index - index
	 * @param entries - list of files
	 * @param group - group
	 */
	public void inter(File index, String[] entries) {
		System.out.println("Starting intereting");
		FileControl.ifEnds(index.getPath(), entries, ".java", "libs", (String s) -> {
			if (s.contains("src") || s.contains("target")) return null;
			var split = s.split("/");
			var group_ext = "";
			if (split.length > 2) {
				System.out.println("splitting answer is: "+split[0]+" >> "+split[split.length-1]);
				group_ext = "."+List.of(split).stream()
					.skip(1).limit(split.length-2)
					.collect(Collectors.joining("."));
				try {
					Files.createDirectories(Paths.get(
								"./src/"+Toml.Info.dirGroup()+group_ext.replace(".", "/")));
				} catch (Exception e) {
					Simple.err(e, "couldn't create sub dir in the specified sources");
				}
			}

			System.out.println("Appending package from: " + "./src/"+Toml.Info.dirGroup()+s.substring(1)+" to "+s);
			var groupStr = "package "+Toml.Info.group+group_ext;
			String content = FileControl.read(s);
			var needsPackage = false;

			// @CHECK if there is already package defined then append root group
			if (content.contains(";")) {
				var firstLine = content.substring(0, content.indexOf(";"));
				if (firstLine.contains("package")) {
					groupStr += "."+content.substring(content.indexOf("package "), content.indexOf(";"));
					content = content.substring(content.indexOf(";"));
				} else { needsPackage = true; }
			}
			if (needsPackage)
				Simple.w("./src/"+Toml.Info.dirGroup()+s.substring(1), (groupStr+";\n\n"+content).getBytes());
			return null;
		});
	}

	/**
	 * Unzips all jar files from libs and local-libs
	 * and copies them to target file with target_libs to show what files were included
	 */
	public void unjar() {
		String[] libs = new File("libs").exists() ? 
			new File("libs").list() : new String[]{};
		String[] localLibs = new File("local-libs").exists() ? 
			new File("local-libs").list() : new String[]{};
		var root = new File("target_libs");
		if (!root.exists()) root.mkdir();

		// @TODO think about better solution for this
		// too many lines (KISS)
		Simple.multipleExec(DefaultFile.Cmd::extract2, 
				"Extractioin of local lib done...", 
				"extraction of local lib failed", 
				root, localLibs, "local");

		// @TODO think about better solution for this
		Simple.multipleExec(DefaultFile.Cmd::extract, 
				"Extractioin of local lib done...", 
				"extraction of local lib failed", 
				root, libs);

		System.out.println("Copy something: "+root.getName());

		MavenStealer.deleteRecursive("target", new File("target").list());
		try {
			Files.deleteIfExists(Paths.get("target"));
			Simple.copyDirectory(root.getName(), "target");
		} catch (Exception e) { Simple.err(e, "Failed to unjar"); }
	}

	// @TODO should use generic way of getting all java files
	public String getJavaFiles(String uri, String[] entries) {
		if (entries == null) return "";
		var sb = new StringBuilder();
		try {
			// return FileControl.returnIfEndsWith(".", new File(".").list(), "java", "libs");
			for(String s : entries) {
				if (s.endsWith(".java")) {
					sb.append("./"+uri+"/"+s+"\n");
					System.out.println("./"+uri+"/"+s);
				} else if (new File(uri+"/"+s).isDirectory()) {
					sb.append(this.getJavaFiles(uri+"/"+s, new File(uri+"/"+s).list()));
				}
			}
			return sb.toString();
		} catch (Exception e) {
			Simple.err(e, "Failed to get java files");
			return "";
		}
	}

	// @TODO what the fuck is this? oveloading method should not be required
	public void compile(String to) { this.compile(to, false); }
	public void run() {
		Simple.runBash(
			"java "+this.getLibs("target")+" "+Toml.Info.group+".Main", 
			"Failed to run project", "Failed to run project"
		);
	}
	public String getLibs(String end) {
		String libs_e = null;
		libs_e = Stream.concat(
			List.of(new File("libs").list()).stream().map(s -> "libs/"+s), 
			List.of(new File("local-libs").list()).stream().map(s -> "local-libs/"+s)
		).collect(Collectors.joining(":"));
		return libs_e == null ? "-cp "+end : "-cp "+libs_e+":"+end;
	}
	public void compile(String to, boolean deleteSrc) {
		System.out.println("compiling...");
		FileControl.deleteDir("target");
		FileControl.copyFileStructure("src", "target");
		this.makeMetaMF();
			var sources = this.getJavaFiles("src", new File("src").list());
		try {
			Files.write(Paths.get("src/sources.txt"), sources.getBytes());
		} catch (Exception e) { Simple.err(e, "Failed to compile project"); }
		Simple.runBash(DefaultFile.Cmd.javac(getLibs(""), to), 
				"Done compiling", "Failed to compile project");
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
}
