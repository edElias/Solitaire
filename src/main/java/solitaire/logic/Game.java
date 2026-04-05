package solitaire.logic;

import solitaire.model.Board;
import solitaire.model.BoardType;
import solitaire.model.CellState;

import java.util.Random;

public abstract class Game {

    private Board board;

    public Game(int size, BoardType type) {
        initGame(size, type);
    }

    public Board getBoard() {
        return board;
    }

    public int[] getCenter() {
        int mid = board.getSize() / 2;
        return new int[]{mid, mid};
    }

    public void newGame(int size, BoardType type) {
        initGame(size, type);
    }
    private void initGame(int size, BoardType type) {
        board = new Board(size, type);
    }

    public boolean isValidMove(Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        if (!board.isInBounds(fromRow, fromCol) || !board.isInBounds(toRow, toCol)) {
            return false;
        }

        else if (board.getCell(fromRow, fromCol) != CellState.PEG) {
            return false;
        }

        else if (board.getCell(toRow, toCol) != CellState.EMPTY) {
            return false;
        }

        int rowDiff = Math.abs(fromRow - toRow);
        int colDiff = Math.abs(fromCol - toCol);

        boolean horizontalMove = rowDiff == 0 && colDiff == 2;
        boolean verticalMove = rowDiff == 2 && colDiff == 0;

        if (!horizontalMove && !verticalMove) {
            return false;
        }

        int middleRow = (fromRow + toRow) / 2;
        int middleCol = (fromCol + toCol) / 2;

        return board.getCell(middleRow, middleCol) == CellState.PEG;
    }

    public boolean makeMove(Move move) {
        if (!isValidMove(move)) {
            return false;
        }

        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        int middleRow = (fromRow + toRow) / 2;
        int middleCol = (fromCol + toCol) / 2;

        board.setCell(fromRow, fromCol, CellState.EMPTY);
        board.setCell(middleRow, middleCol, CellState.EMPTY);
        board.setCell(toRow, toCol, CellState.PEG);

        return true;
    }

    public int countPegs() {
        int count = 0;
        int size = board.getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board.getCell(r, c) == CellState.PEG) {
                    count++;
                }
            }
        }

        return count;
    }

    public boolean isWin() {
        return countPegs() == 1 &&
                board.getCell(getCenter()[0], getCenter()[1]) == CellState.PEG;
    }

    public boolean isGameOver() { //For every peg on the board check if there's a valid move
        int size = board.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {

                if (board.getCell(row, col) == CellState.PEG) {

                    if (isValidMove(new Move(row, col, row - 2, col))) {
                        return false;
                    }
                    else if (isValidMove(new Move(row, col, row + 2, col))) {
                        return false;
                    }
                    else if (isValidMove(new Move(row, col, row, col - 2))) {
                        return false;
                    }
                    else if (isValidMove(new Move(row, col, row, col + 2))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public void randomizeBoard() {
        Random random = new Random();
        int size = getBoard().getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (getBoard().getCell(row, col) != CellState.INVALID) {
                    getBoard().setCell(row, col,
                            random.nextBoolean() ? CellState.PEG : CellState.EMPTY);
                }
            }
        }
    }
    public abstract void playTurn();
}