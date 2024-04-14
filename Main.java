import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {
	public static void main(String[] args) {
		Project project = new Project();

		StrEq str = new StrEq(args);
		if (str.check("h", "help") || args.length == 0) 
			System.out.println(DefaultFile.getSomeHelp());

		if (str.check("n", "clean")) { project.clean(); project.cleanAfter(); }
		else if (str.check("i", "install")) project.installDeps();
		else if (str.check("c", "compile")) { 
			project.structure(); 
			project.compile("target"); 
			project.makeDotEnv();
		} else if (str.check("r", "run") || str.check("j", "jar") || str.check("w", "war")) {
			// @FIXME this is not required -> don't know if it should even be here
			if (new File("libs").exists() || new File("local-libs").exists()) 
				project.installDeps();

			project.structure(); 
			project.compile("target"); 
			project.makeDotEnv(); 

			if (str.check("r", "run")) { project.makeJar(); project.run(); }
			else if (str.check("j", "jar")) project.makeJar();
			else if (str.check("w", "war")) { project.compileWeb(); project.makeWar(); }
		} 
		else if (str.check("e", "env")) project.makeDotEnv();
		else if (str.check("s", "save") || str.check("local", "local")) {
			project.makeJar(); project.clean(); project.copyJar(false);
		} else if (str.check("f", "find")) {
			System.out.println("something");
			project.searchMaven(Arrays.stream(args).skip(1).collect(Collectors.joining("+")));
		} else if (str.check("o", "init")) {
			if (args.length < 2) project.init(null);
			project.init(args[1]);
		}
	}

	// @TODO remove this hall of shame
	public static class StrEq {
		private String[] args; 
		public StrEq(String[] args) { this.args = args; }
		public boolean check(String small, String big) {
			if (args.length < 1) return false;
			return (args[0].contains("-") && args[0].contains(small))
				|| args[0].equals(big);
		}
	}

}
