import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


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
	 * @param names - String[]
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
}
