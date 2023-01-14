import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import utils.*;

public class Main {
    public static boolean debug = false;
    public static void main(String[] args) throws IOException {
        String testInput = null;
        try {
            String arg1 = args[1];
            String arg2 = args[0];
            if (arg1.equals("--debug")) {
                debug = true;
            } else if (arg2.equals("--debug")) {
                debug = true;
                arg2 = arg1;
            }
            if (debug) DebugPrint("Debug on!");
            System.out.println("Path to test: " + arg2);
            File file = new File(arg2);
            if (file.exists() && file.isFile()) {
                testInput = ReadInputFromFile(file);
            } else {
                System.out.println("Incorrect path to the test file");
                System.exit(1);
            }
;        } catch (ArrayIndexOutOfBoundsException e) {
            try {
                String arg1 = args[0];
                System.out.println("Path to test: " + arg1);
                File file = new File(arg1);
                if (file.exists() && file.isFile()) {
                    testInput = ReadInputFromFile(file);
                } else {
                    System.out.println("Incorrect path to the test file");
                    System.exit(1);
                }
            } catch (ArrayIndexOutOfBoundsException ee) {
                System.out.println("No command line arguments specified: can't get inputs");
                System.exit(3);
            }
        }
        DebugPrint("Successfully got inputs:\n" + testInput);
        Grammar[] CFGandRG = Parser.ParseGrammars(testInput, debug);
        Grammar CFGrammar = CFGandRG[0];
        Grammar regularGrammar = CFGandRG[1];

        NFA automaton = NFA.RGtoAutomaton(regularGrammar, debug);
    }

    private static String ReadInputFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(String.valueOf(file))));
    }

    public static void DebugPrint(String str) {
        if (debug) System.out.println(str);
    }
}
