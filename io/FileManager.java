
import dev.levia.jifra.FileControl;
/**
 *
 * FileManager
 */
public class FileManager {
	public static void checker() {
		System.out.println("Something happened from filesystem");
		FileControl.deleteDir("something-else-could");
	}
}
