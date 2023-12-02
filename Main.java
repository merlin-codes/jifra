import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.net.URL;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URI;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.Arrays;


public class Main {
	public static void main(String[] args) {
		Project project = new Project();

		if (args.length == 0 || args[0].equals("help")) {
			project.help();
		} else if (args[0].contains("install")) {
			project.installDeps();
		} else if (args[0].contains("run")) { 
			project.structure(false); 
			project.compile("target"); 
			project.makeDotEnv(); 
			project.run();
		} else if (args[0].contains("init")) { 
			if (args.length == 2) 
				project.init(args[1]); 
			else project.init(null);
		} else if (args[0].contains("clean")) { 
			project.clean();
		} else if (args[0].contains("jar")) { 
			project.structure(true); 
			project.compile("target");
			project.makeDotEnv(); 
			project.makeJar(); 
			project.clean();
		} else if (args[0].contains("war")) { 
			project.structure(true); 
			project.compile("target"); 
			project.makeDotEnv(); 
			project.compileWeb(); 
			project.makeWar(); 
			project.clean();
		} else if (args[0].contains("compile")) { 
			project.structure(true); 
			project.compile("target"); 
			project.makeDotEnv(); 
		} else { 
			System.out.println("not implemented"); 
		}
	}
}
