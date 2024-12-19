package me.scorez.utils.jarrelocatorcli;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Edited from the ChatGPT
 */
public class Main {
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("i").withRequiredArg().required().describedAs("Input JAR file path");
        parser.accepts("o").withRequiredArg().describedAs("Output JAR file path (optional, defaults to overwriting input)");
        parser.accepts("rule").withRequiredArg().required().describedAs("Relocation rules in format <from>:<to>,<from2>:<to2>");
        parser.accepts("help").forHelp();

        OptionSet options;
        try {
            options = parser.parse(args);
        } catch (Exception e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printHelp(parser);
            return;
        }

        if (options.has("help")) {
            printHelp(parser);
            return;
        }

        String inputJar = (String) options.valueOf("i");
        String outputJar = options.has("o") ? (String) options.valueOf("o") : inputJar;
        String rulesArg = (String) options.valueOf("rule");

        if (rulesArg == null || rulesArg.trim().isEmpty()) {
            System.err.println("No relocation rules provided. Please specify at least one rule using --rules.");
            printHelp(parser);
            return;
        }
        List<Relocation> relocations = parseRules(rulesArg);

        if (relocations.isEmpty()) {
            System.err.println("No valid relocation rules provided. Please check your --rules argument.");
            printHelp(parser);
            return;
        }

        try {
            File input = new File(inputJar);
            File output = new File(outputJar);

            if (!input.exists() || input.length() == 0) {
                System.err.println("Error: Input JAR file does not exist or is empty: " + input.getAbsolutePath());
                return;
            }

            File tmpOutput = inputJar.equals(outputJar) ? File.createTempFile("relocated-", ".jar") : output;

            JarRelocator relocator = new JarRelocator(input, tmpOutput, relocations);
            relocator.run();

            if (inputJar.equals(outputJar)) {
                if (!tmpOutput.renameTo(output)) {
                    System.err.println("Error: Unable to overwrite the input file. Temporary file: " + tmpOutput.getAbsolutePath());
                    return;
                }
            }

            System.out.println("JAR relocation completed successfully!");
        } catch (IOException e) {
            System.err.println("An error occurred during JAR relocation:");
            e.printStackTrace();
        }
    }

    private static List<Relocation> parseRules(String rulesArg) {
        List<Relocation> relocations = new ArrayList<>();
        String[] rules = rulesArg.split(",");
        for (String rule : rules) {
            String[] parts = rule.split(":");
            if (parts.length == 2) {
                relocations.add(new Relocation(parts[0], parts[1]));
            } else {
                System.err.println("Invalid rule format: " + rule);
            }
        }
        return relocations;
    }

    private static void printHelp(OptionParser parser) {
        System.out.println("Usage: java -jar jar-relocator-cli.jar --input <input-jar> [--output <output-jar>] --rules <rules>");
        System.out.println("Example: java -jar jar-relocator-cli.jar --input input.jar --rules com.old1:com.new1,com.old2:com.new2");
        try {
            parser.printHelpOn(System.out);
        } catch (IOException e) {
            System.err.println("Error displaying help: " + e.getMessage());
        }
    }
}