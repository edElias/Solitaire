package solitaire.logic;

import solitaire.model.BoardType;
import solitaire.model.CellState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameReplayer {

    public enum EntryType { MOVE, RANDOMIZE }

    public static class ReplayEntry {
        public final EntryType type;
        public final Move move;           // non-null if type == MOVE
        public final int[][] boardState;  // non-null if type == RANDOMIZE

        public ReplayEntry(Move move) {
            this.type = EntryType.MOVE;
            this.move = move;
            this.boardState = null;
        }

        public ReplayEntry(int[][] boardState) {
            this.type = EntryType.RANDOMIZE;
            this.move = null;
            this.boardState = boardState;
        }
    }

    private int boardSize;
    private BoardType boardType;
    private final List<ReplayEntry> entries = new ArrayList<>();
    private int cursor = 0; // points to the NEXT entry to apply

    // The board snapshots after each step (index 0 = initial board)
    private final List<int[][]> snapshots = new ArrayList<>();

    public void loadFromFile(String filePath) throws IOException {
        entries.clear();
        snapshots.clear();
        cursor = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("SIZE=")) {
                    boardSize = Integer.parseInt(line.substring(5));
                } else if (line.startsWith("TYPE=")) {
                    boardType = BoardType.valueOf(line.substring(5));
                } else if (line.startsWith("MOVE=")) {
                    String[] parts = line.substring(5).split(",");
                    int fr = Integer.parseInt(parts[0]);
                    int fc = Integer.parseInt(parts[1]);
                    int tr = Integer.parseInt(parts[2]);
                    int tc = Integer.parseInt(parts[3]);
                    entries.add(new ReplayEntry(new Move(fr, fc, tr, tc)));
                } else if (line.startsWith("RANDOMIZE=")) {
                    String[] parts = line.substring(10).split(",");
                    int[][] state = new int[boardSize][boardSize];
                    int idx = 0;
                    for (int r = 0; r < boardSize; r++) {
                        for (int c = 0; c < boardSize; c++) {
                            state[r][c] = Integer.parseInt(parts[idx++]);
                        }
                    }
                    entries.add(new ReplayEntry(state));
                }
            }
        }
    }

    /**
     * Must be called after loadFromFile. Pass in the freshly initialized game
     * so we can snapshot its starting board state.
     */
    public void initSnapshots(Game game) {
        snapshots.clear();
        cursor = 0;
        snapshots.add(copyBoard(game));
    }

    public int getBoardSize()  { return boardSize; }
    public BoardType getBoardType() { return boardType; }
    public int getCursor()     { return cursor; }
    public int getTotalSteps() { return entries.size(); }
    public boolean hasNext()   { return cursor < entries.size(); }
    public boolean hasPrev()   { return cursor > 0; }

    /**
     * Apply the next entry to the game. Returns the entry applied, or null if done.
     */
    public ReplayEntry stepForward(Game game) {
        if (!hasNext()) return null;

        ReplayEntry entry = entries.get(cursor);

        if (entry.type == EntryType.MOVE) {
            game.makeMove(entry.move);
        } else {
            applyBoardState(game, entry.boardState);
        }

        cursor++;

        // Store snapshot if not already cached
        if (cursor >= snapshots.size()) {
            snapshots.add(copyBoard(game));
        }

        return entry;
    }

    /**
     * Restore the previous board state (undo one step).
     */
    public ReplayEntry stepBack(Game game) {
        if (!hasPrev()) return null;
        cursor--;
        int[][] prevState = snapshots.get(cursor);
        applyBoardState(game, prevState);
        return cursor < entries.size() ? entries.get(cursor) : null;
    }

    /**
     * Restart replay: restore the initial board state.
     */
    public void restart(Game game) {
        cursor = 0;
        applyBoardState(game, snapshots.get(0));
    }


    private int[][] copyBoard(Game game) {
        int size = game.getBoard().getSize();
        int[][] copy = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                copy[r][c] = cellToInt(game.getBoard().getCell(r, c));
            }
        }
        return copy;
    }

    private void applyBoardState(Game game, int[][] state) {
        int size = game.getBoard().getSize();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                game.getBoard().setCell(r, c, intToCell(state[r][c]));
            }
        }
    }

    private int cellToInt(CellState s) {
        return switch (s) {
            case PEG -> 1;
            case EMPTY -> 0;
            case INVALID -> -1;
        };
    }

    private CellState intToCell(int v) {
        return switch (v) {
            case 1  -> CellState.PEG;
            case 0  -> CellState.EMPTY;
            default -> CellState.INVALID;
        };
    }
}