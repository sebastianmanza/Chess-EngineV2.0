package engine;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import utils.MCTutils.Engine;

public class TARSUCI {
    
    /** A boolean to represent if the engine should be running */
    private static boolean running = true;

    /** The engine that is communicating */
    private static final Engine engine = new Engine();

    /** The printwriter object doing the communication */
    private static final PrintWriter pen = new PrintWriter(System.out, true);

    public static void main(String[] args) {
        /* Scan the input */
        Scanner input = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (running) {
            /* Take in a command and deal with it */
            String command = input.nextLine().trim();
            handleCommand(command);
        } //while
        input.close();
    } //main(String[])

    /**
     * Handle the command given to the engine by the GUI
     * @param command the command given.
     */
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
        } //if/else
    } //handleCommand(String)

    /**
     * Handle commands involving positions.
     * @param command The command.
     */
    private static void handlePositionCommand(String command) {
        if (command.contains("startpos")) {
            engine.setPosition("startpos");
        } else if (command.contains("fen")) {
            String fen = command.split("fen ")[1].split(" moves")[0];
            engine.setPosition(fen);
        } //if/else

        if (command.contains("moves")) {
            String[] moves = command.substring(command.indexOf("moves") + 6).split(" ");
            for (String move : moves) {
                engine.applyMove(move);
            } //for
        } //if
    } //handlePositionCommand(String)

    /**
     * Handle go commands.
     * @param command the command to handle
     */
    private static void handleGoCommand(String command) {
        int depth = parseOption(command, "depth", 1);
        int movetime = parseOption(command, "movetime", 1);
        int wtime = parseOption(command, "wtime", 1);
        int btime = parseOption(command, "btime", 1);
        int winc = parseOption(command, "winc", 1);
        int binc = parseOption(command, "binc", 1);

        String bestMove = engine.search(depth, movetime, wtime, btime, winc, binc);
        pen.println("bestmove " + bestMove);
    } //handleGoCommand(String)

    /**
     * Helper function to parse the option from the go command.
     * @param command The original command.
     * @param option The option (depth/movetime/etc)
     * @param defaultValue (The default value assigned if option not given)
     * @return An integer that is the new value;
     */
    private static int parseOption(String command, String option, int defaultValue) {
        if (command.contains(option)) {
            return Integer.parseInt(command.split(option + " ")[1].split(" ")[0]);
        } // if
        return defaultValue;
    } //parseOption(String, String, int)
}
