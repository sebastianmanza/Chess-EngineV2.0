package engine;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import utils.MCTutils.Engine;

public class TARSUCI {
    private static boolean running = true;
    private static final Engine engine = new Engine();
    private static final PrintWriter pen = new PrintWriter(System.out, true);

    public static void main(String[] args) {
        Scanner input = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (running) {

            String command = input.nextLine().trim();

            // pen.println("Received command: " + command);

            handleCommand(command);
        }

        input.close();
    }

    private static void handleCommand(String command) {
        if (command.equals("uci")) {
            pen.println("id name TARS-2.0-jar-with-dependencies.jar");
            pen.println("id author Sebastian Manza");
            pen.println("uciok");
        } else if (command.startsWith("isready")) {
            pen.println("readyok");
        } else if (command.startsWith("ucinewgame")) {
            engine.reset();
        } else if (command.startsWith("position")) {
            handlePositionCommand(command);
        } else if (command.startsWith("go")) {
            handleGoCommand(command);
        } else if (command.startsWith("quit")) {
            running = false;
        }
    }


    private static void handlePositionCommand(String command) {
        if (command.contains("startpos")) {
            engine.setPosition("startpos");
        } else if (command.contains("fen")) {
            String fen = command.split("fen ")[1].split(" moves")[0];
            engine.setPosition(fen);
        }

        if (command.contains("moves")) {
            String[] moves = command.substring(command.indexOf("moves") + 6).split(" ");
            for (String move : moves) {
                engine.applyMove(move);
            }
        }
    }

    private static void handleGoCommand(String command) {
        int depth = parseOption(command, "depth", 1);
        int movetime = parseOption(command, "movetime", 1);
        int wtime = parseOption(command, "wtime", 1);
        int btime = parseOption(command, "btime", 1);
        int winc = parseOption(command, "winc", 1);
        int binc = parseOption(command, "binc", 1);

        String bestMove = engine.search(depth, movetime, wtime, btime, winc, binc);
        pen.println("bestmove " + bestMove);
    }

    private static int parseOption(String command, String option, int defaultValue) {
        if (command.contains(option)) {
            return Integer.parseInt(command.split(option + " ")[1].split(" ")[0]);
        }
        return defaultValue;
    }
}
