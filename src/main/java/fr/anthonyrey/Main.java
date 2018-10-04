package fr.anthonyrey;

import fr.anthonyrey.stats.StatsCodeGen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        if(!(args.length == 1 || args.length == 2)) {
            System.out.println("usage : <path to dsl file> [destination directory]");
            return;
        }

        final String inputFile = args[0];
        Path savePath;

        if(args.length == 1) {
            savePath = Paths.get(".");
        }
        else {
            savePath = Paths.get(args[1]);
        }

        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(inputFile));
            final String inputContent = new String(encoded);
            new StatsCodeGen("stats_engine_codegen", savePath,inputContent).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
