package dev.levia.jifra;

import java.util.Map;

public class WebApp {
	private Map<String, Map<String, String>> config;
	private String group = "com.group";
	public WebApp(String name, String group) {
		System.out.println("Plugin Bean");
		this.group = group;
		config = Toml.parse("web.toml");
	}
	public class XmlBuilder {
		private StringBuilder sb = new StringBuilder("");
		public byte level = 0;
		public XmlBuilder add(String tag, String value) {
			sb.append("\t".repeat(level)+"<"+tag+">"+value+"</"+tag+">\n"); return this;
		}
		public XmlBuilder start(String tag) {
			sb.append("\t".repeat(level)+"<"+tag+">\n"); level++; return this;
		}
		public XmlBuilder end(String tag) {
			level--; sb.append("\t".repeat(level)+"</"+tag+">\n"); return this;
		}
		public String render() { return this.sb.toString(); }
	}
	public String iterFilters() {
		var xml = new XmlBuilder();
		xml.level = 2;
		config.entrySet().forEach((entry) -> {
			if (entry.getKey().contains("filter.")) {
				var lib = entry.getValue();
				xml.start("filter")
					.add("filter-name", entry.getKey().split("\\.")[1])
					.add("filter-class", lib.get("class").split(":")[0]);
				var urls = lib.get("class").split(":")[1].split(",");
				for (int i = 0; i < urls.length; i++) {
					xml.start("filter-mapping")
						.add("filter-name", entry.getKey().split("\\.")[1])
						.add("url-pattern", urls[i])
						.end("filter-mapping");
				}
				xml.end("filter");
			}
		});
		return xml.render();
	}
	public String iterServlets() {
		var xml = new XmlBuilder();
		xml.level = 2;
		var serlvets = config.get("servlet");
		serlvets.forEach((key, value) -> {
			var info = value.split(":");
			var next = 1;
			xml.start("servlet")
				.add("servlet-name", key)
				.add("servlet-class", group.replace("\"","")+"."+info[0].replace("\"",""));
			if (info[info.length-1] == "0") {
				xml.add("load-on-startup", "0");
				next++;
			}
			xml.end("servlet");
			if (info.length > next)
				for (int i = 1; i < (info.length-next)+1; i++)
					xml.start("servlet-mapping")
						.add("servlet-name", key)
						.add("url-pattern", info[i])
						.end("servlet-mapping");
		});
		return xml.render();
	}
}
