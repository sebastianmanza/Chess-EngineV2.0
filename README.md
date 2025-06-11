<h3 align="center">TARS Chess Engine V2.0</h3>

  <p align="center">
    An updated version of https://github.com/sebastianmanza/ChessEngine.


TARS Chess Engine is a chess engine written from scratch in Java. It was developed beginning in October 2024. It can be found on lichess.org as TARS-bot (although isn't always running).

<!-- ## Usage

To install and build the project (assuming maven is installed), start the terminal and run:
```
git clone https://github.com/sebastianmanza/Chess-EngineV2.0.git
cd Chess-EngineV2.0
mvn clean compile
mvn exec:java
```
Now, from the command line, follow the prompts. -->

## Engine

Here we describe the main components of the engine. Code can be found in the `src` directory.

### Board representation

The board is stored in a piece centric bitboard representation, meaning for every color and piece (12 total), a 64-bit integer (long) is stored, containing squares from A1 to H1 with a 1 representing a piece on that square and a 0 elsewhere. This is a common representation in chess engines, and allows precomputation of many move combinations, making it exceptionally efficient on modern hardware.

### Monte Carlo Tree Search

A Monte Carlo Tree Search is employed for move selection: for every game state, nodes representing the next moves are created, with the node of the best Upper Confidence Bound selected (representing our best possible expected result from the move), and continually selected until no game has been played. The ending node is then expanded and then evaluated through a combination of playouts and heuristics, before backpropagating the result (win/loss/draw) to every node that reached that state.

### Convolutional Neural Network
Two forms of tree search are used: for moves with less than 5.5s of thinking time, a traditional MCTS is used, as it is quick and uses an instant evaluation function purely based off of material to evaluate wins. For moves with more than 5.5s of thinking time, a convolutional neural network (trained on 7 million lichess positions with stockfish evaluations) is used to estimate the position. This is combined with a special type of backpropagation which I call Mini-Max Backed Monte Carlo Tree Search, where the 'most robust' position (that with the highest playouts) is backpropagated as a new winrate, until it is no longer the child with the most playouts. If two children are played the same number, it takes the better winrate. In this way it combines the common minimax principle with a MCTS. 

The CNN is used as the evaluation heuristic after the node is expanded. It has currently been trained with 2 convolutional layers of 25 and 50 filters and kernels of 5x5 and 3x3 respectively, followed by a dense layer of 500 neurons and an output layer of a single neuron scaled using a sigmoid function. I tried multiple other architectures of the CNN, including up to 4 convolutional layers and 2 dense layers, but was limited by computational power, and found that it performed more poorly, particularly since it took much longer to produce an output.


### Precomputation

To save time when generating moves, attack maps for every piece are generated. I then use the 'blockers and beyond' method to finish off sliding moves.

### Opening Book

An opening book created for chess engine baron30 has been incorporated for play on lichess.com.

### Multithreading

The simulations attempt to run on multiple threads at once, for greater efficiency. Nodes are chosen, expanded, simulated, and backpropagated in parallel, increasing the number of simulations for a position significantly

## Sources

* [Chess Programming Wiki](https://www.chessprogramming.org/Main_Page)
* [Magic BitBoards](https://analog-hors.github.io/site/magic-bitboards/)
* [CNN in DL4J](https://www.baeldung.com/java-cnn-deeplearning4j)
* [Blockers and Beyond](https://www.chessprogramming.org/Blockers_and_Beyond)

  </p>
</div>

## Roadmap

- [X] Create GameState to store boards
    - [X] Calculate moves
        - [X] Create attack maps
        - [ ] Create magic bitboards to store
        - [X] Apply attack maps to current board state
        - [X] Remove illegal moves (where the king is in check)
    - [X] Apply moves to board to update
- [X] Update the MCT search
    - [X] Allow multi-threading
    - [X] Update move generation with new move type
- [X] Create an opening database
    - [X] Read a PGN into a GameState
    - [ ] Create opening database tree
- [X] Build Neural Network to improve evaluation
    - [X] Generate training data
    - [ ] Train CNN
- [X] Enable UCI protocol for communication with GUI's
- [X] Download and enable tablebases for use in endgame situations and early terminating playouts

Note that the engine is currently designed for performance on my macbook pro m2 chip. Further experimentation is being done on AWS cloud servers, so updates can be expected in the next few weeks. I did not upload my tablebases or PGN files, and many of the TARS-versions to github, as the files are too large.

See the [open issues](https://github.com/sebastianmanza/Chess-EngineV2.0/issues) for a full list of proposed features (and known issues).
<p align="right">(<a href="#readme-top">back to top</a>)</p>
