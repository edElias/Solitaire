package solitaire.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import solitaire.model.BoardType;
import solitaire.model.CellState;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagerTest {

    //Manual Game Tests
    private ManualGame manualGame;

    @BeforeEach
    void setUp() {
        manualGame = new ManualGame(7, BoardType.ENGLISH);
    }

    @Test
    void testManualIsValidMoveReturnsTrueForValidMove() {
        Move move = new Move(3, 1, 3, 3);
        assertTrue(manualGame.isValidMove(move));
    }

    @Test
    void testManualIsValidMoveReturnsFalseForInvalidMove() {
        Move move = new Move(0, 0, 0, 2); // INVALID cell
        assertFalse(manualGame.isValidMove(move));
    }

    @Test
    void testManualMakeMoveUpdatesBoardCorrectly() {
        Move move = new Move(3, 1, 3, 3);
        assertTrue(manualGame.makeMove(move));
        assertEquals(CellState.EMPTY, manualGame.getBoard().getCell(3, 1));
        assertEquals(CellState.EMPTY, manualGame.getBoard().getCell(3, 2));
        assertEquals(CellState.PEG,   manualGame.getBoard().getCell(3, 3));
    }

    @Test
    void testManualMakeMoveReturnsFalseForInvalidMove() {
        Move move = new Move(0, 0, 0, 2);
        assertFalse(manualGame.makeMove(move));
    }

    @Test
    void testManualGameIsNotOverAtStart() {
        assertFalse(manualGame.isGameOver());
    }

    @Test
    void testManualCountPegsAtStart() {
        // Standard 7x7 English board starts with 32 pegs (center empty)
        assertEquals(32, manualGame.countPegs());
    }

    @Test
    void testManualIsWinReturnsFalseAtStart() {
        assertFalse(manualGame.isWin());
    }

    @Test
    void testManualGetCenterIsCorrect() {
        int[] center = manualGame.getCenter();
        assertEquals(3, center[0]);
        assertEquals(3, center[1]);
    }

    @Test
    void testManualNewGameResetsBoard() {
        manualGame.makeMove(new Move(3, 1, 3, 3));
        manualGame.newGame(7, BoardType.ENGLISH);
        assertEquals(32, manualGame.countPegs());
    }

    //Auto Game Tests

    private AutoGame autoGame;

    @Test
    void testAutoPlayTurnReducesPegCount() {
        autoGame = new AutoGame(7, BoardType.ENGLISH);
        int before = autoGame.countPegs();
        autoGame.playTurn();
        assertEquals(before - 1, autoGame.countPegs());
    }

    @Test
    void testAutoPlayTurnDoesNothingWhenGameOver() {
        autoGame = new AutoGame(7, BoardType.ENGLISH);
        // force game over by making it so no valid moves exist
        autoGame.newGame(7, BoardType.ENGLISH);
        // play until game over
        while (!autoGame.isGameOver()) {
            autoGame.playTurn();
        }
        int pegCount = autoGame.countPegs();
        autoGame.playTurn(); // should do nothing
        assertEquals(pegCount, autoGame.countPegs());
    }

    @Test
    void testAutoGetAllValidMovesNotEmptyAtStart() {
        autoGame = new AutoGame(7, BoardType.ENGLISH);
        assertFalse(autoGame.getAllValidMoves().isEmpty());
    }

    @Test
    void testAutoGameIsNotOverAtStart() {
        autoGame = new AutoGame(7, BoardType.ENGLISH);
        assertFalse(autoGame.isGameOver());
    }

    @Test
    void testAutoCountPegsDecreasesAfterEachTurn() {
        autoGame = new AutoGame(7, BoardType.ENGLISH);
        for (int i = 0; i < 5; i++) {
            int before = autoGame.countPegs();
            autoGame.playTurn();
            assertEquals(before - 1, autoGame.countPegs());
        }
    }
}