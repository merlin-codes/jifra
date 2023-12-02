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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Project {
	Map<String, Map<String, String>> map;
	public Project() {
		if (Files.exists(Paths.get("app.toml"))) { map = Toml.parse("app.toml");
		} else { map = new HashMap<String, Map<String, String>>(); }
	}
	public void clean(boolean libs) {
		if (map.get("root").get("clean") != null) {
			if (libs) this.deleteLibsFolders();
			this.deleteIfEndsWith(".", new File(".").list(), ".class", "target");
			this.deleteSourceFolder();
			this.deleteTargetFolder();
		}
	}
	public void clean() { clean(false); }
	public void installDeps() { 
		System.out.println("installing dependencies..."); 
		this.deleteLibsFolders(); 
		MavenStealer.stealLibs("libs", this.map); 
		MavenStealer.stealLibs("test-libs", this.map); 
	}
	public void deleteLibsFolders() { 
		MavenStealer.deleteDir("libs"); 
		MavenStealer.deleteDir("test-libs"); 
	}
	public void deleteSourceFolder() { 
		MavenStealer.deleteDir("src"); 
	}
	public void deleteTargetFolder() { 
		MavenStealer.deleteDir("target"); 
	}
	public void help() {
		System.out.println(
				"commands: \n"+
				"\tinit [name]\t\t- initialize project \n"+
				"\tclean\t\t\t- clean project \n"+
				"\tinstall\t\t\t- install all dependencies \n"+
				"\trun\t\t\t- run program \n"+
				"\thelp\t\t\t- this help \n"+
				"\tjar\t\t\t- make jar \n"+
				"\twar\t\t\t- make war"
		);
	}
	public void init(String name) {
		var title = name;
		if (name == null) title = System.getProperty("user.name");
		String app_content = "name = \""+title+"\"\n"+
			"version = \"1.0.0\"\n"+
			"group = \"com.example."+title+"\"\n"+
			"[support]\n"+
			"maven = \"https://repo1.maven.org/maven2/group/name/version/name-version.jar\"\n"+
			"[libs]\n"+
			"[test-libs]\n";
		String main_content = "public class Main {\n"+
			"\tpublic static void main(String[] args) {\n"+
			"\t\tSystem.out.println(\"Hello World!\");\n"+
			"\t}\n"+
			"}\n";
		try {
			Files.write(Paths.get("app.toml"), app_content.getBytes());
			Files.write(Paths.get("Main.java"), main_content.getBytes());
			System.out.println("installing dependencies");
			this.installDeps();
		} catch (Exception e) { this.errOut(e, "Failed to init of app named "+title); }
	}
	public void makeJar() {
		try {
			Process p = Runtime.getRuntime().exec(( "jar cmvf target/META-INF/MANIFEST.MF " +this.map.get("root").get("name").replace("\"", "") +".jar -C target .").split(" "));
			p.waitFor();
			this.printProcessError(p);
			System.out.println(p.toString());
			System.out.println("Done making jar file");
		} catch (Exception e) { this.errOut(e, "Failed to make jar file"); }
	}
	public void makeWar() {
		try {
			Process p = Runtime.getRuntime().exec(("jar cmvf target/META-INF/MANIFEST.MF "+this.map.get("root").get("name").replace("\"", "")+".war -C target .").split(" "));
			p.waitFor();
			this.printProcessError(p);
			System.out.println(p.toString());
			System.out.println("Done making war file");
		} catch (Exception e) { this.errOut(e, "Failed to make war file"); }
	}
	public void compileWeb() {
		var name = this.map.get("root").get("name");
		System.out.println("Name: "+name+" Group: "+this.map.get("root").get("group"));
		var war = new WebApp(name, map.get("root").get("group"));
		var sb = new StringBuilder( "<!DOCTYPE web-app PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN' 'http://java.sun.com/dtd/web-app_2_3.dtd'>\n<webapp>\t<display-name>"+name+"</display-name>\n<distributable>\n</distributable>\n"+war.iterFilters()+war.iterServlets()+"</webapp>");
		MavenStealer.deleteDir("target");
		new File("target").mkdirs();
		new File("target/META-INF").mkdirs();
		new File("target/WEB-INF").mkdirs();
		new File("target/WEB-INF/classes").mkdirs();
		try {
			this.makeMetaMF();
			Files.write(Paths.get("target/WEB-INF/web.xml"), sb.toString().getBytes());
			Files.write(Paths.get("target/META-INF/context.xml"), ( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+ "<Context path="+name+"/>\n").getBytes());
			this.structure(false);
			this.compile("target/WEB-INF/classes");
			Files.write(Paths.get("target/index.html"), ("<!DOCTYPE html><html><head><title>"+name+"</title></head><body><h1>Hello World!</h1></body></html>").getBytes());
			if (new File("template").exists()) Files.copy(Paths.get("template"), Paths.get("target"));
		} catch (Exception e) { this.errOut(e, "Failed to compile web app"); }
		System.out.println("Done making war file");
	}
	public void makeDotEnv() {
		try {
			var env = this.map.get("env");
			if (env != null) {
				var buffer = new BufferedWriter(new FileWriter(".env"));
				for (Map.Entry<String, String> entry : env.entrySet()) {
					if (entry.getValue().startsWith("UUID-")) {
						var r = new Random();
						var uuid = Long.toHexString(r.nextLong());
						uuid = uuid.substring(0, 8)+"-"+uuid.substring(8, 12)+"-"+uuid.substring(12, 16)+"-"+uuid.substring(16, 20)+"-"+uuid.substring(20, 32);
						buffer.write(entry.getKey()+"="+uuid+"\n");
					} else if (entry.getValue().equals("")) { continue;
					} else { buffer.write(entry.getKey()+"="+entry.getValue()+"\n"); }
				}
				buffer.close();
			}
		} catch (Exception e) { this.errOut(e, "Failed to make .env file"); }
	}
	public void makeMetaMF() {
		try {
			var main = this.map.get("root").get("group").replace("\"", "");
			new File("target/META-INF").mkdir();
			var buffer = new BufferedWriter(new FileWriter("target/META-INF/MANIFEST.MF"));
			buffer.write("Manifest-Version: 1.0\nMain-Class: "+main+".Main\n");
			buffer.close();
		} catch (Exception e) {
			this.err(e, "Failed to make MANIFEST.MF file");
			System.out.println("please check: if you have defined group in app.toml next to name...");
		}
	}
	public void err(Exception e, String msg) { e.printStackTrace(); System.out.println(msg); }
	public void structure(boolean deleteSource) {
		if (deleteSource) MavenStealer.deleteDir("src");
		File index = new File(".");
		String[] entries = index.list();
		if (entries == null) return;
		String group = "src/"+this.map.get("root").get("group").replace(".", "/").replace("\"", "");
		System.out.println("Was created: "+new File(group).mkdirs());
		
		System.out.println("structure created in: "+group);
		System.out.println("creating structure...");
		this.interateEntries(index, entries, group);
	}
	public void interateEntries(File index, String[] entries, String group) {
		for(String s: entries) {
			File currentFile = new File(index.getPath(),s);
			var folder = group+"/"+currentFile.getName();
			if (s.startsWith("src") || s.startsWith("libs") || s.startsWith("test-libs") || s.startsWith("target")) continue;
			if (s.endsWith(".java")) {
				File newFile = new File(index.getPath(), group+"/"+s);
				InputStream is = null;
				OutputStream os = null;
				try {
					is = new FileInputStream(currentFile);
					os = new FileOutputStream(newFile);
					os.write(("package "+group.replace("../", "").replace("/", ".").substring(4)+";\n\n").getBytes());
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) > 0) {
						os.write(buffer, 0, length);
					}
					is.close();
					os.close();
				} catch (Exception e) { this.errOut(e, "Failed to append package to: "+s); }
			}
			if (currentFile.isDirectory()) {
				if (currentFile.list().length > 0)
				new File(folder).mkdirs();

				this.interateEntries(currentFile, currentFile.list(), "../"+folder);
			}
		}
	}
	public void errOut(Exception e, String msg) { e.printStackTrace(); System.out.println(msg); }
	public String getJavaFiles(String uri, String[] entries) {
		if (entries == null) return "";
		var sb = new StringBuilder();
		try {
			for(String s: entries) {
				if (s.endsWith(".java")) {
					sb.append("./"+uri+"/"+s+"\n");
				} 
				else if (new File(uri+"/"+s).isDirectory()) {
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
		try {
			var check = Runtime.getRuntime();
			var solve = "java -cp target "+this.map.get("root").get("group").replace("\"", "")+".Main";
			var process = check.exec(solve.split(" "));
			process.waitFor();
			this.printProcessError(process);
			System.out.println(process.inputReader().readLine());
		} catch (Exception e) { 
			this.errOut(e, "Failed to run project");
		}
	}
	public void compile(String to, boolean deleteSrc) {
		System.out.println("compiling...");
		Runtime runtime = Runtime.getRuntime();
		new File("target").mkdirs();
		this.makeMetaMF();
		try {
			var sources = this.getJavaFiles("src", new File("src").list());
			var sources_files = new BufferedWriter(new FileWriter("src/sources.txt"));
			sources_files.write(sources);
			sources_files.close();

			var build = runtime.exec(("javac -d "+to+" @src/sources.txt").split(" "));

			build.waitFor();
			if (deleteSrc) this.deleteIfEndsWith(".", (new File(".")).list(), ".class", to);
			this.printProcessError(build);

			MavenStealer.deleteDir("src");
		} catch (Exception e) { this.errOut(e, "Failed to compile project"); }
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
