<h3 align="center">TARS Chess Engine V2.0</h3>

  <p align="center">
    An updated version of https://github.com/sebastianmanza/ChessEngine.


TARS Chess Engine is a chess engine written from scratch in Java. It was developed beginning in October 2024.

## Usage

To install and build the project (assuming maven is installed), start the terminal and run:
```
git clone https://github.com/sebastianmanza/ChessEngineV2.0.git
cd ChessEngineV2.0
mvn clean compile
mvn exec:java
```
Now, from the command line, follow the prompts.

## Engine

Here we describe the main components of the engine. Code can be found in the `src` directory.

### Board representation

The board is stored in a piece centric bitboard representation, meaning for every color and piece (12 total), a 64-bit integer (long) is stored, containing squares from A1 to H1 with a 1 representing a piece on that square and a 0 elsewhere. This allows precomputation of many move combinations, making it exceptionally efficient on modern hardware.

### Monte Carlo Tree Search

A Monte Carlo Tree Search is employed for move selection: for every game state, nodes representing the next moves are created, with the node of the best Upper Confidence Bound selected (representing our best possible expected result from the move), and continually selected until no game has been played. The ending node is then expanded and then evaluated through a combination of playouts and heuristics, before backpropagating the result (win/loss/draw) to every node that reached that state.


### Magic Bitboards

Magic bitboards are precomputed at the beginning phase of the game: This means possible combinations of moves, including moves with "blockers" (rook/queen/bishop moves with pieces in the way), that can efficiently be looked up in a table.

### Opening Book

To save time during the opening phase, I have implemented an opening book.

## Multithreading

The simulations attempt to run on multiple threads at once, for greater efficiency. Nodes are chosen, expanded, and simulated in parallel, increasing the number of simulations for a position significantly

## Sources

* [Chess Programming Wiki](https://www.chessprogramming.org/Main_Page)
  </p>
</div>

## Roadmap

- [ ] Calculating Moves
    - [ ] Create magic bitboards
    - [ ] Move generation
- [ ] MCT Search
- [ ] Opening Database
    - [ ] Read GameStates
- [ ] Neural Network for Evaluation

See the [open issues](https://github.com/sebastianmanza/Chess-EngineV2.0/issues) for a full list of proposed features (and known issues).
<p align="right">(<a href="#readme-top">back to top</a>)</p>
