import java.io.File;

public class Main {
	public static void main(String[] args) {
		Project project = new Project();

		if (args.length == 0 || args[0].equals("help")) {
			project.help();
			return;
		} 
		StrEq str = new StrEq(args);

		if (str.contains("-")) {
			if (str.contains("n")) {
				project.clean();
				project.cleanAfter();
			}
			if (str.contains("i")) project.installDeps();
			if (str.contains("c")) {
				project.structure(false); 
				project.compile("target"); 
			} 
			if (str.contains("r")) {
				project.makeJar();
				project.run();
			}
			if (str.contains("j")) project.makeJar(); 
			if (str.contains("w")) {
				project.compileWeb();
				project.makeWar();
			}
			if (str.contains("e")) project.makeDotEnv();
			if (str.contains("s")) {
				project.makeJar();
				project.clean();
				project.copyJar(true);
			}
		}

		if (str.equals("run", "jar", "war")) {
			if (new File("libs").exists() || new File("libs").exists()) 
				project.installDeps();

			project.structure(false); 
			project.compile("target"); 
			project.makeDotEnv(); 

			if (str.equals("run")) {
				project.makeJar();
				project.run();
			}
			if (str.equals("jar")) project.makeJar(); 
			if (str.equals("war")) {
				project.compileWeb(); 
				project.makeWar();
			}
			project.clean();
		} else if (str.equals("unjar")) {
			project.unjar();
		} else if (str.equals("local", "save")) {
			project.makeJar(); 
			project.clean();
			project.copyJar(false);
		} else if (str.equals("clean")) {
			project.clean();
			project.cleanAfter();
		} else if (str.equals("install")) {
			project.installDeps();
		} else if (str.equals("help")) {
			project.help();
		} else if (str.equals("init")) {
			if (args.length < 2) project.init(null);
			else project.init(args[1]);
		} else {
			System.out.println("Not implemented yet");
		}
	}
	public static class StrEq {
		private String[] args;
		public StrEq(String[] args) { this.args = args; }
		public boolean contains(String arg) { return args[0].contains(arg); }
		public boolean contains(String ...args) {
			for (int i = 0; i < args.length; i++)
				if (this.args[0].contains(args[i])) return true;
			return false;
		}
		public boolean equals(String arg) { return args[0].equals(arg); }
		public boolean equals(String ...args) {
			for (int i = 0; i < args.length; i++)
				if (this.args[0].equals(args[i])) return true;
			return false;
		}
	}
}
