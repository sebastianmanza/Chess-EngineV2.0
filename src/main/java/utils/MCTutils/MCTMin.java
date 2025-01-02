package utils.MCTutils;

import java.io.File;
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

import utils.CNNutils.TARSCNN;
import utils.CNNutils.TrainingGen;
import utils.MoveGeneration.GameState;
import utils.MoveGeneration.MoveGen;
import utils.TablebaseUtils.JavaTablebaseBridge;
import utils.UserInterface.UIUtils;

/**
 * The class that runs the Monte Carlo Tree.
 * Only real applicable calls to this class lie with MCTCNN.search().
 * This MCT uses a CNN as a heuristic, and backpropagates in a special way
 * that I think I invented but may have been done before. See readme for
 * (slightly) more detail,
 * or try to make out what I'm tryihng to do in my poorly commented code
 * 
 * @author Sebastian Manza
 */
public class MCTMin {

    /** The exploration parameter, used to balance exploration vs exploitation */
    private static double EXPLORATION_PARAM = 0.5;

    /** The root node of the move. (i.e. the move we are exploring from) */
    final CNNode root;

    public static final MultiLayerNetwork tars = TARSCNN.loadModel(new File("TARS-V5.8.zip"));

    /**
     * Creates a new Monte Carlo Tree
     * 
     * @param state The most recent GameState
     */
    public MCTMin(GameState state) {
        this.root = new CNNode(state, null);
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
    public CNNode search(Duration duration, boolean printScenarios) throws Exception {
        Instant start = Instant.now();
        Instant deadline = start.plus(duration);
        PrintWriter pen = new PrintWriter(System.out, true);
        // int processors = Runtime.getRuntime().availableProcessors();
        // ExecutorService executor = Executors.newFixedThreadPool(processors);

        // Runnable MCTCNNworker = () -> {
        while (Instant.now().isBefore(deadline)) {
            try {
                CNNode selectedNode = select(root);
                INDArray winPoints;
                if (selectedNode.state.numPieces() < 6) {
                    ArrayList<CNNode> endNode = new ArrayList<>();
                    endNode.add(selectedNode);
                    JavaTablebaseBridge bridge = new JavaTablebaseBridge();
                    winPoints = Nd4j.scalar(bridge.probeWDL(selectedNode.state));
                    backPropagate(endNode, winPoints);
                    continue;
                }
                ArrayList<CNNode> expandedNodes = expand(selectedNode);

                if (expandedNodes == null) {
                    ArrayList<CNNode> endNode = new ArrayList<>();
                    endNode.add(selectedNode);
                    double wins = selectedNode.state.vicPoints();
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
          // };
          // for (int i = 0; i < processors; i++) {
          // executor.submit(MCTCNNworker);
          // }

        // executor.shutdown();
        // executor.awaitTermination(duration.toMillis(), TimeUnit.MILLISECONDS);
        /* Find the best move based on the node that was played the most */
        if (root.nextMoves.isEmpty()) {
            return null;
        } // if
        CNNode bestNode = Collections.max(root.nextMoves, Comparator.comparingInt(n -> n.timesAnalyzed.get()));

        if (printScenarios) {
            printMoveChoices(root);
            pen.printf("Simulated %d games. Simulated win rate: %.2f\n", root.timesAnalyzed.get(),
                    bestNode.winProb.get());
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
    private static double UCT(CNNode node) {

        /* Make sure the last moves playouts isn't null */
        double lastMovePlayouts = node.lastMove != null ? node.lastMove.timesAnalyzed.get() : 1;

        /* Return a high value if the node has never been played */
        if (node.timesAnalyzed.get() == 0) {
            return 1000.0;
        } // if

        /* Calculate the Upper Confidence Bound */
        double UCB1 = (node.winProb.get())
                + (EXPLORATION_PARAM * Math.sqrt((Math.log(lastMovePlayouts)) / node.timesAnalyzed.get()));
        return UCB1;
    } // UCT(node)

    /**
     * Selects the best possible node from the current root with current knowledge.
     * 
     * @param node The beginning node.
     * @return The best possible node from the beginning node.
     */
    private static CNNode select(CNNode node) {
        while (true) {
            if (node.nextMoves.isEmpty() || node.timesAnalyzed.get() == 0 || node.state.numPieces() < 6) {
                return node;
            }
            /* Return the node with the highest UCB */
            node = Collections.max(node.nextMoves, Comparator.comparingDouble(MCTMin::UCT));
        }
    } // select(MCTCNNNode)

    /**
     * Expands the tree one level deeper to continue searching
     * 
     * @param node The first node reached with no children.
     * @return The list of expanded nodes.
     */
    private static ArrayList<CNNode> expand(CNNode node) {
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
                            CNNode newNode = new CNNode(gameState, node);
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
    private static INDArray simulateWithTars(ArrayList<CNNode> nextMoves, MultiLayerNetwork tars) {
        int numMoves = nextMoves.size();
        if (numMoves == 0) {
            return null;
            // needs to be evaluated based on terminal state.
        }
        INDArray output = null;
        // System.out.println("Evaluating " + numMoves + " children in batches...");

        for (int i = 0; i < numMoves; i += 32) {
            int end = Math.min(i + 32, numMoves);
            List<CNNode> batch = nextMoves.subList(i, end);
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
    private static void backPropagate(ArrayList<CNNode> nodes, INDArray winProb) {

        for (int i = 0; i < nodes.size(); i++) {
            boolean continueBackPropagation = true;
            CNNode node = nodes.get(i);
            double winProbability = winProb.getDouble(i); // currently in terms of white

            if ((nodes.size() != 1) && (!node.state.engineColor)) {
                winProbability = (1 - winProbability);
            } // now in terms of engine color

            /* While it hasn't reached the root */
            while (node != null) {
                synchronized (node) {
                    /* Update the win probability */
                    if (continueBackPropagation) {
                        if (node.state.engineColor != node.state.turnColor) {
                            double avgWin = (node.winProb.get() == 0) ? winProbability
                                    : ((winProbability + node.winProb.get()) / 2);
                            node.winProb.set(avgWin);
                        } else {
                            double avgWin = (node.winProb.get() == 0) ? (1 - winProbability)
                                    : (((1 - winProbability) + node.winProb.get()) / 2);
                            node.winProb.set(avgWin);
                        }
                    }
                    node.timesAnalyzed.incrementAndGet();
                    /*
                     * Stop the back propagation if the child hasn't been played the most, or has a
                     * lower winrate if equal plays
                     */
                    if (node.lastMove == null) {
                        break;
                    }
                    if (node.timesAnalyzed.get() < node.lastMove.childmaxPlayouts.get()) {
                        continueBackPropagation = false;
                    } else if (node.timesAnalyzed.get() == node.lastMove.childmaxPlayouts.get()) {
                        if (node.winProb.get() < node.lastMove.greatestChild.get()) {
                            continueBackPropagation = false;
                        } else {
                            node.lastMove.greatestChild.set(node.winProb.get());
                        }
                    } else {
                        node.lastMove.childmaxPlayouts.set(node.timesAnalyzed.get());
                    }
                }
                node = node.lastMove;
            }
        }
    } // backPropogate(MCTCNNNode, double, MCTCNNNode)

    /**
     * Prints the computers most likely scenario.
     * 
     * @param pen  The printwriter object to print with
     * @param root The root of the tree
     * @throws Exception if the printWriter object fails
     */
    private static void printLikelyScenario(CNNode root) throws Exception {
        CNNode node = root;
        while (node != null) {
            node.state.printBoard();
            System.out.printf(
                    "Board was played %d times with a winrate of %.2f%% \n",
                    node.timesAnalyzed.get(), (node.winProb.get() / node.timesAnalyzed.get()) * 100);
            if (node.nextMoves.isEmpty()) {
                node = null;
            } else {
                node = Collections.max(node.nextMoves, Comparator.comparingDouble(n -> n.winProb.get()));
            }
        } // while
    } // printLikelyScenario(PrintWriter, MCTCNNNode)

    private static String getMoveSequence(CNNode node) throws Exception {
        CNNode curNode = node;
        StringBuilder str = new StringBuilder();
        str.append("[");
        boolean addLetter = true;
        int numFullMoves = 1;
        while (!curNode.nextMoves.isEmpty()) {
            if (addLetter) {
                if (numFullMoves != 1) {
                    str.append(" ");
                }
                str.append(numFullMoves);
                str.append(".");
                numFullMoves++;
            }
            addLetter = !addLetter;
            str.append(" ");
            str.append(UIUtils.moveToUCI(curNode.move));
            curNode = Collections.max(curNode.nextMoves, Comparator.comparingInt(n -> n.timesAnalyzed.get()));
        }
        str.append("]");
        return str.toString();
    }

    /**
     * Print the move choices ranked from worst to best stemming from the root.
     */
    private static void printMoveChoices(CNNode root) throws Exception {
        int size = root.nextMoves.size();
        for (int i = 0; i < size; i++) {
            CNNode worst = Collections.min(root.nextMoves, Comparator.comparingInt(n -> n.timesAnalyzed.get()));
            System.out.printf(
                    "Move: %s | Win rate: %.2f | Playouts: %d | %s\n",
                    UIUtils.moveToUCI(worst.move), (worst.winProb.get() * 100),
                    worst.timesAnalyzed.get(), getMoveSequence(worst));
            root.nextMoves.remove(worst);
        } // for
    } // printMoveChoices
} // MCTCNN
