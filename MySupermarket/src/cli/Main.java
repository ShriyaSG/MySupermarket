package cli;

import store.SupermarketSystem;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Entry point for the MySupermarket CLUI application.
 * Optionally loads an .ini configuration file at startup.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        SupermarketSystem system = new SupermarketSystem();
        CLI cli = new CLI(system);

        // Load ini file if present
        String iniFile = "my_supermarket.ini";
        if (Files.exists(Path.of(iniFile))) {
            System.out.println("[BOOT] Loading configuration: " + iniFile);
            List<String> lines = Files.readAllLines(Path.of(iniFile));
            for (String line : lines) {
                if (!line.isBlank() && !line.startsWith("#")) {
                    System.out.println(">> " + line);
                    cli.processLine(line);
                }
            }
            System.out.println("[BOOT] Configuration loaded.\n");
        }

        cli.run();
    }
}
