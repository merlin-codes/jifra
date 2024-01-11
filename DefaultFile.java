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
			"[support]\n"+
			"maven = \"https://repo1.maven.org/maven2/group/name/version/name-version.jar\"\n"+
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
			"\tinstall\t\t\t- install all dependencies \n"+
			"\trun\t\t\t- run program \n"+
			"\thelp\t\t\t- this help \n"+
			"\tjar\t\t\t- make jar \n"+
			"\twar\t\t\t- make war\n\n"+
			"chaining commands: \n"+
			"\t[command] -<flags>\n"+
			"\tn - clean (new)\n"+
			"\ti - install\n"+
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
}
