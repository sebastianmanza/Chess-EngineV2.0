package utils.MCTutils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class MCTCNNParallel {

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
    public MCTCNNParallel(GameState state, MultiLayerNetwork tars) {
        this.root = new MCTNode(state, null);
        this.tars = tars;
    } // MCTCNN(Board)

    // /**
    //  * Searches for the best possible move in the tree.
    //  * 
    //  * @param duration       The amount of time to search for
    //  * @param printScenarios Whethere to print likely scenarios
    //  * @return A board representing the best possible move
    //  * @throws Exception if something goes wrong with the PrintWriter or
    //  *                   ExecutorService.
    //  */
    // public MCTNode search(Duration duration, boolean printScenarios) throws Exception {
    //     Instant start = Instant.now();
    //     Instant deadline = start.plus(duration);
    //     PrintWriter pen = new PrintWriter(System.out, true);

    //     int processors = Runtime.getRuntime().availableProcessors();


    //     ConcurrentLinkedQueue<MCTNode> queue = new ConcurrentLinkedQueue<>();

    //     ExecutorService executor = Executors.newFixedThreadPool(processors);

    //     Runnable nodeSelector = () -> {
    //         while ((!Thread.currentThread().isInterrupted()) && Instant.now().isBefore(deadline)) {
    //             while(true) {
    //                 if (queue.size() < 10) {
    //                     break;
    //                 }
    //                 try {
    //                 Thread.sleep(20);
    //                 if (!Instant.now().isBefore(deadline)) {
    //                     break;
    //                 }
    //                 } catch (InterruptedException e) {
    //                     Thread.currentThread().interrupt();
    //                 }

    //             }
    //             MCTNode selectedNode = selectNextNode(root);
    //             if (selectedNode != null) {
    //                 synchronized (selectedNode) {
    //                     if (!selectedNode.inQueue) {
    //                         selectedNode.inQueue = true; // Mark as in queue
    //                         queue.add(selectedNode); // Add to the queue
    //                     }
    //                 }
    //             } else {
    //                 // No unqueued nodes available; pause briefly to avoid busy waiting
    //                 try {
    //                     Thread.sleep(10);
    //                 } catch (InterruptedException e) {
    //                     Thread.currentThread().interrupt();
    //                 }
    //             }
    //         }
    //     };
    //     MultiLayerNetwork[] threadSafeTars = new MultiLayerNetwork[processors - 1];
    //     for (int i = 0; i < processors - 1; i++) {
    //         final int tarsV = i;
    //         threadSafeTars[i] = tars.clone();
    //         executor.submit(() -> {
    //             while (!Thread.currentThread().isInterrupted() && Instant.now().isBefore(deadline)) {
    //                     MCTNode node = queue.poll();
    //                     if (node != null) {
    //                         ArrayList<MCTNode> expandedNodes = expand(node);
    //                         INDArray winPoints;
    //                         if (expandedNodes == null) {
    //                             ArrayList<MCTNode> endNode = new ArrayList<>();
    //                             endNode.add(node);
    //                             winPoints = Nd4j.scalar(node.state.vicPoints());
    //                             backPropagate(endNode, winPoints);
    //                         } else {
    //                             winPoints = simulateWithTars(expandedNodes, threadSafeTars[tarsV]);
    //                             backPropagate(expandedNodes, winPoints);
    //                              }
    //                     } else {
    //                         try {
    //                             Thread.sleep(20);
    //                         } catch (InterruptedException e) {
    //                             System.err.println("Thread interrupted.");
    //                             Thread.currentThread().interrupt();
    //                         }
    //                     }
    //             }
    //         });
    //     }

    //     executor.submit(nodeSelector);

        

    //     // Runnable MCTCNNSworker = () -> {
    //     //     MultiLayerNetwork threadSafeTars = tars.clone();
    //     //     while (Instant.now().isBefore(deadline)) {
    //     //         try {
    //     //             MCTNode selectedNode = select(root);
    //     //             ArrayList<MCTNode> expandedNodes = expand(selectedNode);
    //     //             INDArray winPoints;
    //     //             if (expandedNodes == null) {
    //     //                 ArrayList<MCTNode> endNode = new ArrayList<>();
    //     //                 endNode.add(selectedNode);
    //     //                 winPoints = Nd4j.scalar(selectedNode.state.vicPoints());
    //     //                 backPropagate(endNode, winPoints);
    //     //             } else {
    //     //                 winPoints = simulateWithTars(expandedNodes, threadSafeTars);
    //     //                 backPropagate(expandedNodes, winPoints);
    //     //             }
    //     //         } catch (Exception e) {
    //     //             e.printStackTrace();
    //     //         } // try/catch
    //     //     } // while
    //     // };
    //     // for (int i = 0; i < processors; i++) {
    //     //     executor.submit(MCTCNNSworker);
    //     // } // for
    //     executor.shutdown();
    //     executor.awaitTermination(duration.toMillis(), TimeUnit.MILLISECONDS);

    //     /* Find the best move based on the node that was played the most */
    //     if (root.nextMoves.isEmpty()) {
    //         return null;
    //     } // if
    //     MCTNode bestNode = Collections.max(root.nextMoves, Comparator.comparingInt(n -> n.playOuts.get()));

    //     if (printScenarios) {
    //         printLikelyScenario(root);
    //         printMoveChoices(root);
    //         pen.println("Simulated " + root.playOuts.get() + " games. Simulated win rate: "
    //                 + (bestNode.wins.get() / bestNode.playOuts.get() * 100));
    //     } // if
    //     /* Return the move. */
    //     return bestNode;
    // } // search(Duration)

    public MCTNode search(Duration duration, boolean printScenarios) throws Exception {
    Instant deadline = Instant.now().plus(duration);
    int processors = Runtime.getRuntime().availableProcessors();
    ConcurrentLinkedQueue<MCTNode> queue = new ConcurrentLinkedQueue<>();
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    CountDownLatch latch = new CountDownLatch(processors);

    // Selector thread
    executor.submit(() -> {
        try {
            while (!Thread.currentThread().isInterrupted() && Instant.now().isBefore(deadline)) {
                if (queue.size() < 10) {
                    MCTNode selectedNode = selectNextNode(root);
                    if (selectedNode != null && selectedNode.tryMarkInQueue()) {
                        queue.add(selectedNode);
                        System.out.println("Selector added node: " + selectedNode);
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } // Backoff when queue is full
                }
            }
        } finally {
            latch.countDown();
        }
    });

    // Evaluator threads
    MultiLayerNetwork[] threadSafeTars = new MultiLayerNetwork[processors - 1];
    for (int i = 0; i < processors - 1; i++) {
        final int tarsV = i;
        threadSafeTars[i] = tars.clone();

        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && (Instant.now().isBefore(deadline) || !queue.isEmpty())) {
                    MCTNode node = queue.poll();
                    if (node == null) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            e.printStackTrace();
                        } // Backoff
                        continue;
                    }
                    System.out.println("Evaluator processing node: " + node);
                    ArrayList<MCTNode> expandedNodes = expand(node);
                    INDArray winPoints;
                    if (expandedNodes == null) {
                        winPoints = Nd4j.scalar(node.state.vicPoints());
                        backPropagate(new ArrayList<>(List.of(node)), winPoints);
                    } else {
                        winPoints = simulateWithTars(expandedNodes, threadSafeTars[tarsV]);
                        backPropagate(expandedNodes, winPoints);
                    }
                }
            } finally {
                latch.countDown();
            }
        });
    }

    executor.shutdown();
    latch.await(); // Ensure all threads finish

    MCTNode bestNode = Collections.max(root.nextMoves, Comparator.comparingInt(n -> n.playOuts.get()));

            if (printScenarios) {
            printLikelyScenario(root);
            printMoveChoices(root);
            System.out.println("Simulated " + root.playOuts.get() + " games. Simulated win rate: "
                    + (bestNode.wins.get() / bestNode.playOuts.get() * 100));
        } // if
    if (root.nextMoves.isEmpty()) return null;

    return bestNode;
}

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
            node = Collections.max(node.nextMoves, Comparator.comparingDouble(MCTCNNParallel::UCT));
        }
    } // select(MCTCNNNode)

    private static MCTNode selectNextNode(MCTNode root) {
        synchronized (root) {
            return findMostPromisingNode(root);
        }
    }
    
    private static MCTNode findMostPromisingNode(MCTNode node) {
        // If this node is not expanded and not queued, return it
        if (!node.isExpanded && !node.inQueue.get()) {
            return node;
        }
    
        // Sort child nodes by UCT value in descending order
        List<MCTNode> rankedNodes = new ArrayList<>(node.nextMoves);
        rankedNodes.sort(Comparator.comparingDouble(MCTCNNParallel::UCT).reversed());
    
        for (MCTNode child : rankedNodes) {
            synchronized (child) {
                // Skip nodes that are already in the queue
                if (child.inQueue.get()) {
                    continue;
                }
    
                // Recursively find the most promising node
                MCTNode promisingNode = findMostPromisingNode(child);
                if (promisingNode != null) {
                    return promisingNode;
                }
            }
        }
    
        // If no promising nodes are found, return null
        return null;
    }
    

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

    // /**
    // * Randomly simulates the finish of the game from the current game state.
    // *
    // * @param node The terminating node
    // * @param root The original root of the tree
    // * @return the number of win-points
    // */

    // private static double simulate(MCTNode node, MultiLayerNetwork tars) throws
    // Exception {
    // if (node.state.nextMoves().length == 0) {
    // return node.state.vicPoints();
    // } else {
    // INDArray input = TrainingGen.createTensor(node.state);
    // // Disable workspaces
    // //
    // tars.getLayerWiseConfigurations().setInferenceWorkspaceMode(org.deeplearning4j.nn.conf.WorkspaceMode.NONE)

    // INDArray output = tars.output(input, false);
    // double eval = output.getDouble(0);

    // if (!node.state.engineColor) {
    // eval = 1 - eval;
    // }

    // return eval;
    // }
    // }

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
                    if (curNode.state.turnColor != curNode.state.engineColor) {
                        curNode.wins.addAndGet(winPoints.getDouble(i));
                    } else {
                        curNode.wins.addAndGet(1 - winPoints.getDouble(i));
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
