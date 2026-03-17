package solitaire.ui;

import solitaire.logic.GameManager;
import solitaire.logic.Move;
import solitaire.model.Board;
import solitaire.model.CellState;

import javax.swing.*;
import java.awt.*;

public class SolitaireGUI extends JFrame {

    private GameManager gameManager;
    private JButton[][] buttons;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private JComboBox<String> sizeComboBox;
    private JRadioButton englishButton;
    private JRadioButton diamondButton;
    private JRadioButton hexagonButton;
    private JButton newGameButton;

    public SolitaireGUI() {
        gameManager = new GameManager();

        setTitle("Peg Solitaire");
        setSize(700, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        add(createBoardPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        refreshBoard();

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();

        topPanel.add(new JLabel("Board Size:"));

        sizeComboBox = new JComboBox<>(new String[]{"7"}); //for now only 7
        topPanel.add(sizeComboBox);

        topPanel.add(new JLabel("Board Type:"));

        englishButton = new JRadioButton("English", true);
        diamondButton = new JRadioButton("Diamond"); //unselectable
        hexagonButton = new JRadioButton("Hexagon"); //unselectable

        diamondButton.setEnabled(false);
        hexagonButton.setEnabled(false);

        ButtonGroup group = new ButtonGroup();
        group.add(englishButton);
        group.add(diamondButton);
        group.add(hexagonButton);

        topPanel.add(englishButton);
        topPanel.add(diamondButton);
        topPanel.add(hexagonButton);

        return topPanel;
    }

    private JPanel createBoardPanel() {
        Board board = gameManager.getBoard();
        int size = board.getSize();

        JPanel boardPanel = new JPanel(new GridLayout(size, size));
        buttons = new JButton[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                JButton button = new JButton();
                buttons[r][c] = button;

                int row = r;
                int col = c;

                button.addActionListener(e -> handleClick(row, col));

                boardPanel.add(button);
            }
        }

        return boardPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();

        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> {
            gameManager.newGame();
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
        });

        bottomPanel.add(newGameButton);

        return bottomPanel;
    }

    private void handleClick(int row, int col) {

        Board board = gameManager.getBoard();
        CellState clickedCell = board.getCell(row, col);

        // First click: only allow selecting a peg
        if (selectedRow == -1) {
            if (clickedCell == CellState.PEG) {
                selectedRow = row;
                selectedCol = col;
                refreshBoard();
            }
            return;
        }

        // If user clicks the same selected peg again, unselect it
        if (selectedRow == row && selectedCol == col) {
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
            return;
        }

        Move move = new Move(selectedRow, selectedCol, row, col);

        if (gameManager.makeMove(move)) {
            // valid move
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();

            if (gameManager.isGameOver()) {

                if (gameManager.isWin()) {
                    JOptionPane.showMessageDialog(this, "WINNER WINNER CHICKEN DINNER!");
                } else {
                    JOptionPane.showMessageDialog(this, "Game Over!");
                }

            }
        } else {
            // invalid move: cancel selection
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
        }
    }

    private void refreshBoard() {
        Board board = gameManager.getBoard();
        int size = board.getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                CellState state = board.getCell(r, c);
                JButton button = buttons[r][c];

                if (state == CellState.INVALID) {
                    button.setVisible(false);
                } else {
                    button.setVisible(true);
                    button.setEnabled(true);
                    button.setOpaque(true);
                    button.setBorderPainted(true);

                    if (state == CellState.PEG) {
                        button.setText("●");
                        button.setBackground(Color.white);
                        button.setForeground(Color.black);
                    } else {
                        button.setText("");
                        button.setBackground(Color.white);
                    }

                    // highlight selected peg
                    if (r == selectedRow && c == selectedCol) {
                        button.setBackground(Color.RED);
                    }
                }
            }
        }
    }
}