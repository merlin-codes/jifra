package dev.levia.jifra;

import java.util.Map;
import java.util.Random;

public class DefaultFile {
	/**
	 * Returns Default content of app.toml
	 * @param title - name of the project
	 */
	public static byte[] getAppToml(String title) {
		return (
			"name = \""+title+"\"\n"+
			"version = \"1.0.0\"\n"+
			"group = \"com.example."+title+"\"\n"+
			"\n"+
			"[support]\n"+
			"maven = \"https://repo1.maven.org/maven2/group/name/version/name-version.jar\"\n"+
			"search = \"https://search.maven.org/search?q\"\n"+
			"\n"+
			"[libs]\n"+
			"[test-libs]\n"+
			"[local-libs]\n"
		).getBytes();
	}
	/**
	 * Returns Default content of Main.java (with syntax of original java)
	 */
	public static byte[] getMainJava() {
		return (
			"public class Main {\n"+
			"\tpublic static void main(String[] args) {\n"+
			"\t\tSystem.out.println(\"Hello World!\");\n"+
			"\t}\n"+
			"}\n"
		).getBytes();
	}
	/**
	 * Returns Default content of web.toml
	 */
	public static byte[] getWebToml(String name, String group) {
		var war = new WebApp(name, group);
		var sb = new StringBuilder(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
			"<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n"+
			"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"+
			"\txsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee "+
			"https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd\"\n"+
			"\tversion=\"5.0\"\n"+
			"\tmetadata-complete=\"true\"\n"+
			"\tdisplay-name="+name+">\n"+
			war.iterFilters()+
			war.iterServlets()+
			"</web-app>"
		);
		return sb.toString().getBytes();
	}
	/**
	 * Returns Default content of MANIFEST.MF
	 * Note: using basic syntax Manifest and Main-Class location (Main.java)
	 */
	public static byte[] getManifestXml(String main) {
		return (
			"Manifest-Version: 1.0\nMain-Class: "+
			main + ".Main\n"
		).getBytes();
	}
	/**
	 * Returns Default content of .env
	 * Note: UUID-* will be replaced with random UUID
	 * @param env - Map of env values and their names
	 */
	public static byte[] getDotEnv(Map<String, String> env) {
		var builder = new StringBuilder();
		for (Map.Entry<String, String> entry : env.entrySet()) {
			if (entry.getValue().startsWith("UUID-")) {
				var r = new Random();
				var uuid = Long.toHexString(r.nextLong());
				uuid = uuid.substring(0, 8)+"-"+uuid.substring(8, 12)+"-"+uuid.substring(12, 16)+"-"+uuid.substring(16, 20)+"-"+uuid.substring(20, 32);
				builder.append(entry.getKey()+"="+uuid+"\n");
			} else if (entry.getValue().equals("")) { continue;
			} else { 
				builder.append(entry.getKey()+"="+entry.getValue()+"\n");
			}
		}
		return builder.toString().getBytes();
	}
	public static String getSomeHelp() {
		return (
			"commands: \n"+
			"\tinit [name]\t\t- initialize project \n"+
			"\tclean\t\t\t- clean project \n"+
			"\tunjar\t\t\t- extract all libraries to the target folder \n"+
			"\tinstall\t\t\t- install all dependencies \n"+
			"\trun\t\t\t- run program \n"+
			"\thelp\t\t\t- this help \n"+
			"\tjar\t\t\t- make jar \n"+
			"\twar\t\t\t- make war\n\n"+
			"chaining commands: \n"+
			"\t[command] -<flags>\n"+
			"\tn - clean (new)\n"+
			"\ti - install\n"+
			"\tu - unjar\n"+
			"\tc - compile\n"+
			"\tr - run\n"+
			"\tj - make jar\n"+
			"\tw - make war\n"+
			"\te - make .env\n"+
			"\ts - save jar"
		);
	}
	public static byte[] getContextXml(String name) {
		return (
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
			"<Context path="+name+"/>\n"
		).getBytes();
	}

	enum Cmd {
		Compile("jar cmvf target/META-INF/MANIFEST.MF "),
		;
		private String value;
		Cmd(String name) { this.value = name; }
		public String val() { return value; }
		public static String compile(String name, String ext) {
			return compile(name, ext, "target", ".");
		}
		public static String javac(String libs, String to) {
			return "javac "+libs+" -d "+to+" @src/sources.txt";
		}
		public static String extract(String lib) {
			return extract2(lib, "");
		}
		public static String extract2(String lib, String name) {
			return "jar xf "+"../"+name+"libs/"+lib+" .";
		}

		public static String compile(String name, String ext, String to, String from) {
			return Compile + name + "." + ext + " -C target " + from;
		}
	}
	enum Erno {
		TomlSyntaxError(
			"Failed to get search url from app.toml (should be in support)\n"+
			"don't use `=` in the url it will be used url=args[1..]\n"+
			"example toml: search = \"https://search.maven.org/search?q\""
		),
		LibrariesMissing(
			"Failed to list libraries from filesystem -- skipping libraries outside `libs` folder"
		),
		FailedWar("Failed to create archive of typ war")

		;
		public static String failedJarAlreadyExist(String name) {
			return "Failed to copy to location "+name+".jar because file already exists";
		}

		private String value;
		Erno(String name) { this.value = name; }
		public String val() { return value; }
	}
	enum Done {
		DoneArchive("Completed creation archive of type "),

		;
		private String value;
		Done(String name) { this.value = name; }
		public String val() { return value; }
		public static String archive(String ext) {
			return DoneArchive + ext;
		}
	}
}
