package utils.MCTutils;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import utils.CNNutils.TrainingGen;
import utils.MoveGeneration.GameState;
import utils.MoveGeneration.MoveGen;
import utils.UserInterface.UIUtils;

/**
 * The class that runs the Monte Carlo Tree.
 * Only real applicable calls to this class lie with MCTCNN.search().
 * 
 * @author Sebastian Manza
 */
public class MCTCNN {

    /** The exploration parameter, used to balance exploration vs exploitation */
    private static final double EXPLORATION_PARAM = 0.8;

    /** The root node of the move. (i.e. the move we are exploring from) */
    private final MCTNode root;

    MultiLayerNetwork tars;

    /**
     * Creates a new Monte Carlo Tree
     * 
     * @param state The most recent GameState
     */
    public MCTCNN(GameState state, MultiLayerNetwork tars) {
        this.root = new MCTNode(state, null);
        this.tars = tars;
    } // MCTCNN(Board)

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

        while (Instant.now().isBefore(deadline)) {
            try {
                MCTNode selectedNode = select(root);
                ArrayList<MCTNode> expandedNodes = expand(selectedNode);
                INDArray winPoints;
                if (expandedNodes == null) {
                    ArrayList<MCTNode> endNode = new ArrayList<>();
                    endNode.add(selectedNode);
                    double wins = selectedNode.state.vicPoints();
                    if (!selectedNode.state.engineColor) {
                        wins = 1 - wins;
                    }
                    winPoints = Nd4j.scalar(wins);
                    backPropagate(endNode, winPoints);
                } else {
                    winPoints = simulateWithTars(expandedNodes, tars);
                    backPropagate(expandedNodes, winPoints);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } // try/catch
            } // while

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
            node = Collections.max(node.nextMoves, Comparator.comparingDouble(MCTCNN::UCT));
        }
    } // select(MCTCNNNode)

    /**
     * Expands the tree one level deeper to continue searching
     * 
     * @param node The first node reached with no children.
     * @return The list of expanded nodes.
     */
    private static ArrayList<MCTNode> expand(MCTNode node) {
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
        /*
         * Return the node if theres no possible next move. We need to evaluate the
         * gamestate
         */
        if (node.nextMoves.isEmpty()) {
            return null;
        } // if

        return new ArrayList<>(node.nextMoves);
    } // expand(node)

    /**
     * Evaluate nodes
     * 
     * @param moves the list of nodes to evaluate
     * @param tars
     * @return
     * @throws Exception
     */
    private static INDArray simulateWithTars(ArrayList<MCTNode> nextMoves, MultiLayerNetwork tars) {
        int numMoves = nextMoves.size();
        if (numMoves == 0) {
            return null;
            // needs to be evaluated based on terminal state.
        }
        INDArray output = null;
        // System.out.println("Evaluating " + numMoves + " children in batches...");

        for (int i = 0; i < numMoves; i += 32) {
            int end = Math.min(i + 32, numMoves);
            List<MCTNode> batch = nextMoves.subList(i, end);
            // Prepare input tensor for batch
            INDArray batchInput = Nd4j.create(new int[] { batch.size(), 13, 8, 8 });
            for (int j = 0; j < batch.size(); j++) {
                INDArray tensor = TrainingGen.createTensor(batch.get(j).state);
                tensor = tensor.reshape(13, 8, 8);
                batchInput.putSlice(j, tensor);
            }

            // Run inference for batch
            INDArray batchOutput = tars.output(batchInput);
            if (output == null) {
                output = batchOutput;
            } else {
                output = Nd4j.concat(0, output, batchOutput);
            }
        }
        return output;
    }

    /**
     * Increments the total wins of every previous node by the points based on
     * reward.
     * Increments total playouts by 1 for each regardless.
     * 
     * @param node      The node to backpropagate from (terminating node)
     * @param winPoints The number of points to be given.
     * @param length
     * 
     */
    private static void backPropagate(ArrayList<MCTNode> nodes, INDArray winPoints) {
        for (int i = 0; i < nodes.size(); i++) {
            MCTNode curNode = nodes.get(i);

            /* Synchronize the calculations */
            while (curNode != null) {
                synchronized (curNode) {

                    /* Add the rewards. */
                    curNode.playOuts.incrementAndGet();
                    double wins = winPoints.getDouble(i);
                    if (nodes.get(i).state.turnColor) {
                        wins = 1 - wins;
                    }
                    if (curNode.state.turnColor != curNode.state.engineColor) {
                        curNode.wins.addAndGet(wins);
                    } else {
                        curNode.wins.addAndGet(1 - wins);
                    } // if/else

                } // synchronized(node)
                curNode = curNode.lastMove;
            } // while
        }
    } // backPropogate(MCTCNNNode, double, MCTCNNNode)

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
    } // printLikelyScenario(PrintWriter, MCTCNNNode)

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

    public static MoveProb[] getMoveProbabilities(GameState state, INDArray rawPolicy) {

        /* Get the legal moves */
        short[] legalMoves = state.nextMoves();
        MoveProb[] moves = new MoveProb[legalMoves.length];

        for (int i = 0; i < legalMoves.length; i++) {
            short move = legalMoves[i];
            int fromIndex = Long.numberOfTrailingZeros(MoveGen.moveParts[move][0]);
            int toIndex = Long.numberOfTrailingZeros(MoveGen.moveParts[move][1]);
            int policyIndex = fromIndex * 64 + toIndex;

            moves[i] = new MoveProb(move, rawPolicy.getDouble(policyIndex));
        }
        return moves;
    }

} // MCTCNN
