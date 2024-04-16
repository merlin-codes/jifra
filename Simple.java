import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * Simple is class with fully simplefy methods usage
 */
public class Simple {
	/**
	 * Just Files.write shortcut
	 * @param path -String path
	 * @param data - byte[] data
	 */
	public static void w(String path, byte[] data) {
		try {
			Files.write(Paths.get(path), data);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * Just Files.createDirectories shortcut with multiple inputS
	 * @param names - ...String names
	 */
	public static void mkdirs(String ...names) {
		for (int i = 0; i < names.length; i++) {
			try {
				Files.createDirectories(Paths.get(names[i]));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Concat of multiple arrays to one
	 * @param arrs - String[]
	 */
	public static String[] arrToOne(String[] ...arrs) {
		List<String> list = List.of();
		var len = 0;
		for (int i = 0; i < arrs.length; i++) {
			list.addAll(List.of(arrs[i]));
			len += arrs[i].length;
		}
		return list.toArray(new String[len]);
	}


	public static void multipleExec(
			Consumer<String> exec, String msg, String err, File chdir, String[] items) {
		for (String item : items) exec.accept(item);
	}
	public static void multipleExec(
			BiConsumer<String, String> exec, String msg, String err, File chdir, 
			String[] items, String second) {
		for (String item : items) exec
			.accept(item, second);
	}

	// runBash list
	public static void runBash(String cmd, String msg, String err) {
		runBash(cmd, msg, err, null);
	}
	public static void runBash(String cmd, String msg, String err, File chdir) {
		try {
			Process p = Runtime.getRuntime().exec(cmd.split(" "), null, chdir);
			p.waitFor();
			printProcessError(p);
		} catch (Exception e) {
			e.printStackTrace(); 
			System.out.println(msg); 
		}
	}

	static void printProcessError(Process build) throws IOException, InterruptedException {
		var buffer = new BufferedReader(new InputStreamReader(build.getErrorStream()));
		String line;
		while ((line = buffer.readLine()) != null) {
			System.out.println(line);
		}
	}
	public static void err(Exception e, String msg) { e.printStackTrace(); System.out.println(msg); }

	public static void copyDirectory(String from, String to) throws IOException {
		Files.walk(Paths.get(from)).forEach(source -> {
			var destination = Paths.get(to, source.toString().substring(from.length()));
			try { Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING); } 
			catch (IOException e) { e.printStackTrace(); }
		});
	}
}
