package solitaire.logic;

import org.junit.jupiter.api.Test;
import solitaire.model.CellState;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagerTest {

    @Test
    void testIsValidMoveReturnsTrueForValidOpeningMove() {
        GameManager gameManager = new GameManager();

        // On a standard 7x7 English solitaire board,
        // one valid opening move is jumping into the center:
        // from (3,1) over (3,2) to (3,3)
        Move move = new Move(3, 1, 3, 3);

        assertTrue(gameManager.isValidMove(move));
    }

    @Test
    void testMakeMoveUpdatesBoardCorrectly() {
        GameManager gameManager = new GameManager();
        Move move = new Move(3, 1, 3, 3);

        boolean result = gameManager.makeMove(move);

        assertTrue(result);

        // from position should now be empty
        assertEquals(CellState.EMPTY, gameManager.getBoard().getCell(3, 1));

        // jumped-over peg should now be empty
        assertEquals(CellState.EMPTY, gameManager.getBoard().getCell(3, 2));

        // destination should now contain a peg
        assertEquals(CellState.PEG, gameManager.getBoard().getCell(3, 3));
    }

    @Test
    void testGameIsNotOverAtStart() {
        GameManager gameManager = new GameManager();

        assertFalse(gameManager.isGameOver());
    }

    @Test
    void testIsValidMoveReturnsFalseForInvalidMove() {
        GameManager gameManager = new GameManager();

        Move move = new Move(0, 0, 0, 2);

        assertFalse(gameManager.isValidMove(move));
    }
}