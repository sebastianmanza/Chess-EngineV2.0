package utils.MCTutils;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import utils.MoveGeneration.GameState;
import utils.MoveGeneration.MoveGen;
import utils.TablebaseUtils.JavaTablebaseBridge;
import utils.UserInterface.UIUtils;

/**
 * The class that runs the Monte Carlo Tree.
 * Only real applicable calls to this class lie with MCT.search().
 * 
 * @author Sebastian Manza
 */
public class MCT {

    /** The exploration parameter, used to balance exploration vs exploitation */
    private static final double EXPLORATION_PARAM = 0.8;

    /** The root node of the move. (i.e. the move we are exploring from) */
    MCTNode root;

    /**
     * Creates a new Monte Carlo Tree
     * 
     * @param state The most recent GameState
     */
    public MCT(GameState state) {
        this.root = new MCTNode(state, null);
    } // MCT(Board)

    /**
     * Searches for the best possible move in the tree.
     * 
     * @param duration       The amount of time to search for
     * @param printScenarios Whethere to print likely scenarios
     * @return A board representing the best possible move
     * @throws Exception if something goes wrong with the PrintWriter or
     *                   ExecutorService.
     */
    public MCTNode search(Duration duration, boolean printScenarios) throws Exception {
        Instant start = Instant.now();
        Instant deadline = start.plus(duration);
        PrintWriter pen = new PrintWriter(System.out, true);

        int processors = Runtime.getRuntime().availableProcessors();
        //int processors = 1;
        ExecutorService executor = Executors.newFixedThreadPool(processors);

        Runnable MCTSworker = () -> {
            while (Instant.now().isBefore(deadline)) {
                try {
                    MCTNode selectedNode = select(root);
                    MCTNode expandedNode = expand(selectedNode);
                    double winPoints = simulate(expandedNode);
                    backPropagate(expandedNode, winPoints);
                } catch (Exception e) {
                } // try/catch
            } // while
        };
        for (int i = 0; i < processors; i++) {
            executor.submit(MCTSworker);
        } // for
        executor.shutdown();
        executor.awaitTermination(duration.toMillis(), TimeUnit.MILLISECONDS);

        /* Find the best move based on the node that was played the most */
        if (root.nextMoves.isEmpty()) {
            return null;
        } // if
        MCTNode bestNode = Collections.max(root.nextMoves, Comparator.comparingInt(n -> n.playOuts.get()));

        if (printScenarios) {
            printLikelyScenario(root);
            printMoveChoices(root);
            pen.println("Simulated " + root.playOuts.get() + " games. Simulated win rate: "
                    + (bestNode.wins.get() / bestNode.playOuts.get() * 100));
        } // if
        /* Return the move. */
        return bestNode;
    } // search(Duration)

    /**
     * Calculates a value for a node to select using UCB1
     * 
     * @param node The node to calculate
     * @return The value of the node
     */
    private static double UCT(MCTNode node) {

        /* Make sure the last moves playouts isn't null */
        double lastMovePlayouts = node.lastMove != null ? node.lastMove.playOuts.get() : 1;

        /* Return a high value if the node has never been played */
        if (node.playOuts.get() == 0) {
            return 1000.0;
        } // if

        /* Calculate the Upper Confidence Bound */
        double UCB1 = (node.wins.get() / node.playOuts.get())
                + (EXPLORATION_PARAM * Math.sqrt((Math.log(lastMovePlayouts)) / node.playOuts.get()));
        return UCB1;
    } // UCT(node)

    /**
     * Selects the best possible node from the current root with current knowledge.
     * 
     * @param node The beginning node.
     * @return The best possible node from the beginning node.
     */
    private static MCTNode select(MCTNode node) {
        while (true) {
            if (node.nextMoves.isEmpty() || node.playOuts.get() == 0) {
                return node;
            }
            /* Return the node with the highest UCB */
            node = Collections.max(node.nextMoves, Comparator.comparingDouble(MCT::UCT));
        }
    } // select(MCTNode)

    /**
     * Expands the tree one level deeper to continue searching
     * 
     * @param node The first node reached with no children.
     * @return The expanded node.
     */
    private static MCTNode expand(MCTNode node) {
        /*
         * Synchronize the expansion process so multiple threads don't attempt to expand
         * the same node
         */
        if (!node.isExpanded) {
            synchronized (node) {
                if (!node.isExpanded) {
                    node.isExpanded = true;

                    /* Add all possible children to the node */
                    short[] nextMoves = node.state.nextMoves();
                    for (short move : nextMoves) {
                        try {
                            GameState gameState = MoveGen.applyMove(move, node.state);
                            /* If the ending board is not legal */
                            if (gameState == null) {
                                continue;
                            } // if
                            MCTNode newNode = new MCTNode(gameState, node);
                            node.newChild(newNode);
                            newNode.move = move;
                        } catch (Exception e) {
                        } // try/catch
                    } // for
                }
            }
        }
        /* Return the node if theres no possible next move */
        if (node.nextMoves.isEmpty()) {
            return node;
        } // if

        return node.nextMoves.peek();
    } // expand(node)

    /**
     * Randomly simulates the finish of the game from the current game state.
     * 
     * @param node The terminating node
     * @param root The original root of the tree
     * @return the number of win-points
     */

    private static double simulate(MCTNode node) throws Exception {
        /* Set the current board and the maximum depth to simulate to */
        GameState gameState = node.state;
        int depth = 0;
        SplittableRandom random = new SplittableRandom();
        int pieceCount = gameState.numPieces();
        long PawnPos = gameState.bitBoards[GameState.WPAWNS] & gameState.bitBoards[GameState.BPAWNS];
        int FiftyMoveRule = 0;
        JavaTablebaseBridge bridge = new JavaTablebaseBridge();
        /* Run the loop while the game is undecided */
        while (true) {
            short[] nextMoves = gameState.nextMoves();
            int numMov = nextMoves.length;
            int lastPieceCount = pieceCount;
            long lastPawnPos = PawnPos;
            pieceCount = gameState.numPieces();
            PawnPos = gameState.bitBoards[GameState.WPAWNS] & gameState.bitBoards[GameState.BPAWNS];

            if (pieceCount == lastPieceCount && PawnPos == lastPawnPos) {
                FiftyMoveRule++;
            } else {
                FiftyMoveRule = 0;
            }

            while (true) {
                // Ensure nextMoves has elements to select
                if (numMov == 0) {
                    return gameState.vicPoints(); // No valid moves, return victory points
                }
                if (pieceCount < 6) {
                    double score = bridge.probeWDL(gameState);
                    return score;
                }

                int rand = random.nextInt(numMov);
                short move = nextMoves[rand];
                GameState nextState = MoveGen.applyMove(move, gameState);

                // Check if the move leads to a valid state
                if (nextState != null) {
                    gameState = nextState;
                    break;
                } else {
                    nextMoves[rand] = nextMoves[--numMov];
                }
            }
            if (FiftyMoveRule > 50) {
                return 0.5; // Draw
            }
            if (depth++ > 0) {
                double eval = Evaluate.evaluate(gameState);
                if (!gameState.engineColor) {
                    eval = 1 - eval;
                }
                return eval;
            }
        }
    }

    /**
     * Increments the total wins of every previous node by the points based on
     * reward.
     * Increments total playouts by 1 for each regardless.
     * 
     * @param node      The node to backpropagate from (terminating node)
     * @param winPoints The number of points to be given.
     * @param length    The length of the simulation
     */
    private static void backPropagate(MCTNode node, double winPoints) {
        MCTNode curNode = node;

        /* Synchronize the calculations */
        while (curNode != null) {
            synchronized (curNode) {

                /* Add the rewards. */
                curNode.playOuts.incrementAndGet();
                if (curNode.state.turnColor != curNode.state.engineColor) {
                    curNode.wins.addAndGet(winPoints);
                } else {
                    curNode.wins.addAndGet(1 - winPoints);
                } // if/else

            } // synchronized(node)
            curNode = curNode.lastMove;
        } // while
    } // backPropogate(MCTNode, double, MCTNode)

    /**
     * Prints the computers most likely scenario.
     * 
     * @param pen  The printwriter object to print with
     * @param root The root of the tree
     * @throws Exception if the printWriter object fails
     */
    private static void printLikelyScenario(MCTNode root) throws Exception {
        MCTNode node = root;
        while (!node.nextMoves.isEmpty()) {
            node.state.printBoard();
            System.out.printf(
                    "Board was played %d times with a winrate of %.2f%% \n",
                    node.playOuts.get(), (node.wins.get() / node.playOuts.get()) * 100);
            node = Collections.max(node.nextMoves, Comparator.comparingInt(n -> n.playOuts.get()));
        } // while
    } // printLikelyScenario(PrintWriter, MCTNode)

    /**
     * Print the move choices ranked from worst to best stemming from the root.
     */
    private static void printMoveChoices(MCTNode root) {
        int size = root.nextMoves.size();
        for (int i = 0; i < size; i++) {
            MCTNode worst = Collections.min(root.nextMoves, Comparator.comparingInt(n -> n.playOuts.get()));
            System.out.printf(
                    "Move: %s | Win rate: %.2f | Playouts: %d\n",
                    UIUtils.toNotation(worst.move), ((worst.wins.get() / worst.playOuts.get()) * 100),
                    worst.playOuts.get());
            root.nextMoves.remove(worst);
        } // for
    } // printMoveChoices

    public INDArray getPolicy() {
        INDArray policy = Nd4j.zeros(4096);
        for (MCTNode node : root.nextMoves) {
            int index = Long.numberOfTrailingZeros(MoveGen.moveParts[node.move][0]) * 64
                    + Long.numberOfTrailingZeros(MoveGen.moveParts[node.move][1]);
            policy.putScalar(index, ((double) node.playOuts.get() / (double) root.playOuts.get()));
        }
        return policy;
    }
} // MCT
