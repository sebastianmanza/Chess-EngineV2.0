package utils.MCTutils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.AtomicDouble;

import utils.MoveGeneration.GameState;

/**
 * The Nodes that make up the Monte Carlo Tree (Min variation)
 * 
 * @author Sebastian Manza
 */
public class CNNode {
    /**
     * The current boardstate
     */
    public GameState state;

    /**
     * The list of all possible next nodes
     */
    public ConcurrentLinkedQueue<CNNode> nextMoves;
    /**
     * The win probability of the node
     */
    public AtomicDouble winProb;
    /**
     * The number of times this was attempted
     */
    public AtomicInteger timesAnalyzed;

    /**
     * The parent node (last game state)
     */
    public CNNode lastMove;

    /** Has the board been given children? */
    public boolean isExpanded;

    /** The most recent move played */
    public short move;

    public AtomicInteger childmaxPlayouts;

    public AtomicDouble greatestChild;


    /**
     * Create a new Monte Carlo node for use in the tree
     * 
     * @param curState   The current board.
     * @param parentNode The node that came before
     */
    public CNNode(GameState curState, CNNode parentNode) {
        this.state = curState;
        this.winProb = new AtomicDouble(0.0);
        this.timesAnalyzed = new AtomicInteger(0);
        this.lastMove = parentNode;
        this.nextMoves = new ConcurrentLinkedQueue<>();
        this.isExpanded = false;
        this.move = 0;
        this.childmaxPlayouts = new AtomicInteger(0);
        this.greatestChild = new AtomicDouble(0);
    } // MCNode(Board, MCNode)

    /**
     * Add a new child to the node.
     * 
     * @param childNode The childNode to be added.
     */
    public void newChild(CNNode childNode) {
        this.nextMoves.offer(childNode);
    } // newChild(CNNode)

} // CNNode
