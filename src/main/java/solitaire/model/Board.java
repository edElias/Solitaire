package solitaire.model;

public class Board {

    private CellState[][] grid;
    private int size;
    private BoardType type;

    public Board(int size, BoardType type) {
        this.size = size;
        this.type = type;
        grid = new CellState[size][size];
        initializeBoard();
    }

    private void initializeBoard() {

        if(this.type == BoardType.ENGLISH) {
            initializeEnglish();
        }

        // placeholders for later
        else if(this.type == BoardType.DIAMOND) {
            initializeDiamond();
        }

        else if(this.type == BoardType.HEXAGON) {
            initializeHexagon();
        }
    }

    private void initializeEnglish() {

        int mid = size / 2;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {

                // corners are invalid
                if((r < 2 || r > size-3) && (c < 2 || c > size-3)) {
                    grid[r][c] = CellState.INVALID;
                }
                else {
                    grid[r][c] = CellState.PEG;
                }

            }
        }
        // set center
        grid[mid][mid] = CellState.EMPTY;
    }

    private void initializeDiamond() {
        // done later
    }

    private void initializeHexagon() {
        // done later
    }

    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public CellState getCell(int row, int col) {
        if (!isInBounds(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    public int getSize() {
        return size;
    }

    public BoardType getType() {
        return type;
    }

    public void setCell(int row, int col, CellState state) {
        if (isInBounds(row, col)) {
            grid[row][col] = state;
        }
    }

}