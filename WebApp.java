import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebApp {
	private Map<String, Map<String, String>> config;
	private String group = "com.group";
	public WebApp(String name, String group) {
		System.out.println("Plugin Bean");
		this.group = group;
		config = Toml.parse("web.toml");
	}
	public String iterFilters() {
		var sb = new StringBuilder("");
		for (Map.Entry<String, Map<String, String>> entry : config.entrySet()) {
			if (entry.getKey().contains("filter.")) {
				var lib = entry.getValue();
				sb.append("\t\t<filter>\n");
				System.out.println(Arrays.toString(entry.getKey().split("\\.")));
				sb.append("\t\t\t<filter-name>"+entry.getKey().split("\\.")[1]+"</filter-name>\n");
				sb.append("\t\t\t<filter-class>"+lib.get("class").split(":")+"</filter-class>\n");
				var param = lib.get("param").split(",");
				for (int i = 0; i < param.length; i++) {
					sb.append("\t\t\t<init-param>\n");
					System.out.println(Arrays.toString(param[i].split(":")));
					sb.append("\t\t\t\t<param-name>"+param[i].split(":")[0].trim()+"</param-name>\n");
					sb.append("\t\t\t\t<param-value>"+param[i].split(":")[1].trim()+"</param-value>\n");
					sb.append("\t\t\t</init-param>\n");
				}
				sb.append("\t\t</filter>\n");
				var mappings = lib.get("class").split(":");
				for (int i = 1; i < mappings.length; i++) {
					sb.append("\t\t<filter-mapping>\n");
					sb.append("\t\t\t<filter-name>"+entry.getKey().split("\\.")[1]+"</filter-name>\n");
					sb.append("\t\t\t<url-pattern>"+mappings[i]+"</url-pattern>\n");
					sb.append("\t\t</filter-mapping>\n");
				}
			}
		}
		return sb.toString();
	}
	public String iterServlets() {
		var sb = new StringBuilder("");
		var servlets = config.get("servlet");
		for (Map.Entry<String, String> entry : servlets.entrySet()) {
			var info = entry.getValue().split(":");
			var next = 1;
			sb.append("\t\t<servlet>\n\t\t\t<servlet-name>"+entry.getKey()+"</servlet-name>\n\t\t\t<servlet-class>"+group+"."+info[0]+"</servlet-class>\n");
			if (info[info.length-1] == "0") {
				sb.append("\t\t\t<load-on-startup>0</load-on-startup>\n");
				next++;
			}
			sb.append("\t\t</servlet>\n");
			if (info.length > next)
				for (int i = 1; i < (info.length-next)+1; i++)
					sb.append("\t\t<servlet-mapping>\n\t\t\t<servlet-name>"+entry.getKey()+"</servlet-name>\n\t\t\t<url-pattern>"+info[i]+"</url-pattern>\n\t\t</servlet-mapping>\n");
		}
		return sb.toString();
	}
}
