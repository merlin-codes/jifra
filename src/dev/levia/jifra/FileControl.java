package dev.levia.jifra;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileControl {
	public static void deleteDir(String name) {
		if (new File(name).exists()) return;
		try (Stream<Path> walk = Files.walk(Paths.get(name))) {
			walk.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.peek(System.out::println)
				.forEach(File::delete);
		} catch (Exception e) {
			Simple.err(e, "Deleting files had failed to be done correctly: "+name);
		}
	}
	/**
	 * Note: !recurive
	 * @param index - origin route
	 * @param list - list of files
	 * @param endsWith - (whitelist) filter files that doesn't end with this
	 * @param ignore - (blacklist) ignore files that end with this
	 */
	public static void deleteIfEndsWith(
			String index, String[] list, String endsWith, String ignore) {
		FileControl.ifEnds(index, list, endsWith, ignore, (String s) -> {
			File currentFile = new File(index+"/"+s);
			currentFile.delete();
			return null;
		});
	}
	/**
	 * Note: !recurive
	 * @param index - origin route
	 * @param list - list of files
	 * @param endsWith - (whitelist) filter files that doesn't end with this
	 * @param ignore - (blacklist) ignore files that end with this
	 */
	public static String returnIfEndsWith(
			String index, String[] list, String endsWith, String ignore) {
		StringBuilder sb = new StringBuilder();
		FileControl.ifEnds(index, list, endsWith, ignore, (String s) -> {
			sb.append(index+"/"+s+"\n");
			return null;
		});
		return sb.toString();
	}
	/**
	 * Note: !recurive
	 * @paraam index - origin route
	 * @param list - list of files
	 * @param endsWith - (whitelist) filter files that doesn't end with this
	 * @param ignore - (blacklist) ignore files that end with this
	 * @param func - function to execution of success of ending
	 */
	public static void ifEnds(
			String index, String[] list, String endsWith, String ignore,
			Function<String, Void> func) {
		if (list == null) return;
		for(String s: list) {
			if (s.endsWith(ignore)) continue;
			if (s.endsWith(endsWith)) {
				func.apply(index+"/"+s);
			} else if (new File(index+"/"+s).isDirectory()) {
				ifEnds(index+"/"+s, new File(index+"/"+s).list(), endsWith, ignore, func);
			}
		}
	}
	/**
	 * Note: !recurive
	 * Note: will use Files static class
	 * @param index - origin route
	 * @param list - list of files
	 * @param endsWith - (whitelist) filter files that doesn't end with this
	 * @param ignore - (blacklist) ignore files that end with this
	 * @param append - text to append
	 */
	public static void appendIfEndsWith(
			String index, String[] list, String endsWith, String ignore, String append) {
		FileControl.ifEnds(
			index, list, endsWith, ignore, (String s) -> {
				System.out.println("appendIfEndsWith => "+index+"/"+s+", "+append);
				File currentFile = new File(index+"/"+s);
				try {
					String content = Files.readString(currentFile.toPath());
					Files.write(currentFile.toPath(), (append+content).getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		);
	}
	/**
	 * Copies a folder to destination
	 * Note: !recurive
	 * Note: will use Files static class
	 * @param from - origin route
	 * @param to - destination
	 */
	public static void copyFolder(String from, String to) {
		try {
			Files.createDirectories(new File(to).toPath());
			var file = new File(from);
			if (!file.isDirectory()) {
				Files.copy(
					file.toPath(),
					new File(to).toPath(),
					StandardCopyOption.REPLACE_EXISTING
				);
				return;
			}
			Files.walk(file.toPath()).forEach(source -> {
				var destination = Paths.get(to, source.toString()
						.substring(from.length()));
				try {
					Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void copyFileStructure(String source, String target) {
        File targetFile = new File(target);
        if (!targetFile.exists()) {
            targetFile.mkdir();
        }
		Arrays.stream(new File(source).listFiles()).filter(File::isDirectory)
			.forEach(f -> {
                String append = "/" + f.getName();
                System.out.println("Creating '" + target + append + "': "
                        + new File(target + append).mkdir());
                copyFileStructure(source + append, target + append);
			});
    }
	public static void copy(String from, String to) {
		try {
			Files.copy(Paths.get(from), Paths.get(to), 
					StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			Simple.err(e, "ERROR: failed to copy from "+from+" to "+to);
		}
	}
	public static String read(String url) {
		try {
			return Files.readString(Paths.get(url));
		} catch (Exception e) {
			Simple.err(e, "ERROR: failed to read file at "+url);
			return "";
		}
	}
}
