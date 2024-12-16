package utils.OpeningBook;

import java.util.ArrayList;
import java.util.List;

public class OTNode {
    short move;
    List<OTNode> nextMoves;

    public OTNode(short move) {
        this.move = move;
        this.nextMoves = new ArrayList<>();
    }

    
    public void addMove(OTNode child) {
        this.nextMoves.add(child);
    }

    public OTNode findMove(short move) {
        for (OTNode node : nextMoves) {
            if (node.move == move) {
                return node;
            }
        }
        return null;
    }
}

