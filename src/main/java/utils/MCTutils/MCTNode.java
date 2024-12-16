package utils.MCTutils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.AtomicDouble;

import utils.MoveGeneration.GameState;

/**
 * The Nodes that make up the Monte Carlo Tree.
 * 
 * @author Sebastian Manza
 */
public class MCTNode {
    /**
     * The current boardstate
     */
    public GameState state;

    /**
     * The list of all possible next nodes
     */
    public ConcurrentLinkedQueue<MCTNode> nextMoves;
    /**
     * The total wins/draws of the node
     */
    public AtomicDouble wins;
    /**
     * The number of times this was attempted
     */
    public AtomicInteger playOuts;

    /**
     * The parent node (last game state)
     */
    public MCTNode lastMove;

    /** Has the board been given children? */
    public boolean isExpanded;

    /** The most recent move played */
    public short move;

    /**
     * Create a new Monte Carlo node for use in the tree
     * 
     * @param curState   The current board.
     * @param parentNode The node that came before
     */
    public MCTNode(GameState curState, MCTNode parentNode) {
        this.state = curState;
        this.wins = new AtomicDouble(0.0);
        this.playOuts = new AtomicInteger(0);
        this.lastMove = parentNode;
        this.nextMoves = new ConcurrentLinkedQueue<>();
        this.isExpanded = false;
        this.move = 0;
    } // MCNode(Board, MCNode)

    /**
     * Add a new child to the node.
     * 
     * @param childNode The childNode to be added.
     */
    public void newChild(MCTNode childNode) {
        this.nextMoves.offer(childNode);
    } // newChild(MCTNode)
} // MCTNode
