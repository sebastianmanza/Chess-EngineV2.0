package utils.MCTutils;

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

    
} //MCT
