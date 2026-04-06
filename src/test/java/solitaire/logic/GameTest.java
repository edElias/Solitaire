package solitaire.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import solitaire.model.BoardType;
import solitaire.model.CellState;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    private ManualGame manualGame;
    private AutoGame autoGame;

    @BeforeEach
    void setUp() {
        manualGame = new ManualGame(7, BoardType.ENGLISH);
        autoGame = new AutoGame(7, BoardType.ENGLISH);
    }

    // User Story 1: Choose a board size and type

    @Test
    void testBoardSizeIsCorrect() {
        ManualGame size9 = new ManualGame(9, BoardType.ENGLISH);
        assertEquals(9, size9.getBoard().getSize());
    }

    @Test
    void testBoardTypeEnglishHasCorrectPegCount() {
        assertEquals(32, manualGame.countPegs());
    }

    @Test
    void testDifferentBoardSizesHaveDifferentPegCounts() {
        ManualGame size9 = new ManualGame(9, BoardType.ENGLISH);
        assertNotEquals(manualGame.countPegs(), size9.countPegs());
    }

    // User Story 2: Choose the game mode (manual/automated)

    @Test
    void testManualGameIsInstanceOfGame() {
        assertInstanceOf(Game.class, manualGame);
    }

    @Test
    void testAutoGameIsInstanceOfGame() {
        assertInstanceOf(Game.class, autoGame);
    }

    @Test
    void testManualAndAutoAreDistinctTypes() {
        assertNotEquals(manualGame.getClass(), autoGame.getClass());
    }

    // User Story 3: Start a new game

    @Test
    void testNewGameResetsManualBoard() {
        manualGame.makeMove(new Move(3, 1, 3, 3));
        manualGame.newGame(7, BoardType.ENGLISH);
        assertEquals(32, manualGame.countPegs());
    }

    @Test
    void testNewGameResetsAutoBoard() {
        autoGame.playTurn();
        autoGame.newGame(7, BoardType.ENGLISH);
        assertEquals(32, autoGame.countPegs());
    }

    @Test
    void testNewGameWithDifferentSize() {
        manualGame.newGame(9, BoardType.ENGLISH);
        assertEquals(9, manualGame.getBoard().getSize());
    }

    // User Story 4: Make a move in a manual game

    @Test
    void testManualIsValidMoveReturnsTrueForValidMove() {
        assertTrue(manualGame.isValidMove(new Move(3, 1, 3, 3)));
    }

    @Test
    void testManualIsValidMoveReturnsFalseForInvalidMove() {
        assertFalse(manualGame.isValidMove(new Move(0, 0, 0, 2)));
    }

    @Test
    void testManualMakeMoveUpdatesBoardCorrectly() {
        manualGame.makeMove(new Move(3, 1, 3, 3));
        assertEquals(CellState.EMPTY, manualGame.getBoard().getCell(3, 1));
        assertEquals(CellState.EMPTY, manualGame.getBoard().getCell(3, 2));
        assertEquals(CellState.PEG,   manualGame.getBoard().getCell(3, 3));
    }

    @Test
    void testManualMakeMoveReturnsFalseForInvalidMove() {
        assertFalse(manualGame.makeMove(new Move(0, 0, 0, 2)));
    }

    @Test
    void testManualMoveReducesPegCountByOne() {
        int before = manualGame.countPegs();
        manualGame.makeMove(new Move(3, 1, 3, 3));
        assertEquals(before - 1, manualGame.countPegs());
    }

    //User Story 5: A manual game is over

    @Test
    void testManualGameIsNotOverAtStart() {
        assertFalse(manualGame.isGameOver());
    }

    @Test
    void testManualIsWinReturnsFalseAtStart() {
        assertFalse(manualGame.isWin());
    }

    @Test
    void testManualGameIsOverAfterManualMoves() {
        // play until no valid moves using manual makeMove
        while (!manualGame.isGameOver()) {
            Move found = null;
            int size = manualGame.getBoard().getSize();
            int[][] directions = {{-2,0},{2,0},{0,-2},{0,2}};
            outer:
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    for (int[] d : directions) {
                        Move m = new Move(row, col, row + d[0], col + d[1]);
                        if (manualGame.isValidMove(m)) {
                            found = m;
                            break outer;
                        }
                    }
                }
            }
            if (found == null) break; // safety guard
            manualGame.makeMove(found);
        }
        assertTrue(manualGame.isGameOver());
    }

    //User Story 6: Make a move in an automated game

    @Test
    void testAutoPlayTurnReducesPegCount() {
        int before = autoGame.countPegs();
        autoGame.playTurn();
        assertEquals(before - 1, autoGame.countPegs());
    }

    @Test
    void testAutoGetAllValidMovesNotEmptyAtStart() {
        assertFalse(autoGame.getAllValidMoves().isEmpty());
    }

    @Test
    void testAutoCountPegsDecreasesAfterEachTurn() {
        for (int i = 0; i < 5; i++) {
            int before = autoGame.countPegs();
            autoGame.playTurn();
            assertEquals(before - 1, autoGame.countPegs());
        }
    }

    // User Story 7: An automated game is over

    @Test
    void testAutoGameIsNotOverAtStart() {
        assertFalse(autoGame.isGameOver());
    }

    @Test
    void testAutoGameIsOverWhenNoPegsCanMove() {
        while (!autoGame.isGameOver()) {
            autoGame.playTurn();
        }
        assertTrue(autoGame.isGameOver());
    }

    @Test
    void testAutoPlayTurnDoesNothingWhenGameOver() {
        while (!autoGame.isGameOver()) {
            autoGame.playTurn();
        }
        int pegCount = autoGame.countPegs();
        autoGame.playTurn();
        assertEquals(pegCount, autoGame.countPegs());
    }

    // User Story 8: Randomize the board

    @Test
    void testRandomizeBoardChangesPegCount() {
        int before = manualGame.countPegs();
        boolean changed = false;
        for (int i = 0; i < 10; i++) {
            manualGame.randomizeBoard();
            if (manualGame.countPegs() != before) {
                changed = true;
                break;
            }
        }
        assertTrue(changed);
    }

    @Test
    void testRandomizeBoardKeepsInvalidCellsIntact() {
        manualGame.randomizeBoard();
        assertEquals(CellState.INVALID, manualGame.getBoard().getCell(0, 0));
    }

    @Test
    void testRandomizeBoardDoesNotSetInvalidCellsToPeg() {
        for (int i = 0; i < 20; i++) {
            manualGame.randomizeBoard();
            assertNotEquals(CellState.PEG, manualGame.getBoard().getCell(0, 0));
        }
    }
}