package solitaire.logic;

import solitaire.model.Board;
import solitaire.model.BoardType;
import solitaire.model.CellState;

public class AutoGame extends Game {

    public AutoGame(int size, BoardType type) {
        super(size, type);
    }

    @Override
    public void playTurn() {

        Board board = getBoard();
        int size = board.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {

                if (board.getCell(row, col) == CellState.PEG) {

                    Move[] moves = {
                            new Move(row, col, row - 2, col),
                            new Move(row, col, row + 2, col),
                            new Move(row, col, row, col - 2),
                            new Move(row, col, row, col + 2)
                    };

                    for (Move move : moves) {
                        if (isValidMove(move)) {
                            makeMove(move);
                            return;
                        }
                    }
                }
            }
        }
    }
}
