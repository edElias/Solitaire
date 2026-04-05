package solitaire.logic;

import solitaire.model.Board;
import solitaire.model.BoardType;
import solitaire.model.CellState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutoGame extends Game {

    private final Random random = new Random();

    public AutoGame(int size, BoardType type) {
        super(size, type);
    }

    @Override
    public void playTurn() {
        List<Move> validMoves = getAllValidMoves();

        if (!validMoves.isEmpty()) {
            Move move = validMoves.get(random.nextInt(validMoves.size()));
            makeMove(move);
        }
    }

    public List<Move> getAllValidMoves() {
        Board board = getBoard();
        int size = board.getSize();
        List<Move> validMoves = new ArrayList<>();
        int[][] directions = {{-2,0},{2,0},{0,-2},{0,2}};

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board.getCell(row, col) == CellState.PEG) {
                    for (int[] d : directions) {
                        Move move = new Move(row, col, row + d[0], col + d[1]);
                        if (isValidMove(move)) {
                            validMoves.add(move);
                        }
                    }
                }
            }
        }
        return validMoves;
    }
}