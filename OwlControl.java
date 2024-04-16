import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OwlControl - automatic returns logic Control Flow
 */
public class OwlControl {
	private static final Runtime runtime = Runtime.getRuntime();
	private static final String target = "target";
	private static final String sources = "@src/sources.txt";
	/**
	 * Process multiple execution
	 * @param cmds - commands
	 */
	public static void execs(String... cmds) {
		for (var cmd : cmds) exec(cmd);
	}
	/**
	 * Process execution
	 * @param cmd - command
	 */
	public static void exec(String cmd) {
		try {
			var p = runtime.exec(cmd.split(" "));
			System.out.println("cmd: "+cmd);
			printLines("output: ", p.getInputStream());
			printLines("errors: ", p.getErrorStream());
			p.waitFor();
			System.out.println(cmd+ " "+p.exitValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process execution
	 * @param cmd - command
	 * @param path - path
	 */
	public static void exec(String cmd, String path) {
		try {
			var p = runtime.exec(cmd.split(" "), null, new File(path));
			System.out.println("cmd: "+cmd);
			printLines("output: ", p.getInputStream());
			printLines("errors: ", p.getErrorStream());
			p.waitFor();
			System.out.println(cmd+ " "+p.exitValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Glory kill of inputStream printer
	 * @param cmd - command name 
	 * @param ins - inputStream
	 */
	private static void printLines(
			String cmd, InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(
            new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            System.out.println(cmd + " " + line);
        }
	}

	/**
	 * Compiles target with libs to jar
	 * @param libs - libs to 
	 */
	public static void compileCmds(List<String> libs) {
		var libs_line = "";
		if (libs.size() > 1)
			libs_line = "-cp "+
				libs.stream().collect(Collectors.joining(":")).substring(1, -1);

		OwlControl.exec("javac "+libs_line+"-d "+target+" "+sources);
	}
	public static void buildArchive(String[] libs, String name) {
		// creates place for libs to be pasted
		new File("target/libs").delete();
		new File("target/libs").mkdirs();

		FileControl.copyFolder("libs", "target/libs");
		FileControl.copyFolder("local-libs", "target/libs");

		OwlControl.exec(
			"jar cmvf target/META-INF/MANIFEST.MF "+name+" -C "+target+" ."
		);
	}
	/**
	 * Extract multiple jar files to target
	 * @param target - target
	 * @param ...names - names (full name from root)
	 */
	public static void extractCmds(String target, String ...names) {
		if (!(new File(target).exists())) new File(target).mkdirs();
		for (int i = 0; i < names.length; i++) {
			OwlControl.exec("jar xvf "+names[i]+".jar");
		}
	}
}
