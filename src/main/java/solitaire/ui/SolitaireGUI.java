package solitaire.ui;

import solitaire.logic.AutoGame;
import solitaire.logic.ManualGame;
import solitaire.logic.Game;
import solitaire.logic.Move;
import solitaire.model.Board;
import solitaire.model.BoardType;
import solitaire.model.CellState;


import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;

public class SolitaireGUI extends JFrame {

    private Game game;
    private JButton[][] buttons;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private JComboBox<String> sizeComboBox;
    private JRadioButton englishButton;
    private JRadioButton diamondButton;
    private JRadioButton hexagonButton;
    private JButton newGameButton;
    private JRadioButton manualButton;
    private JRadioButton autoButton;
    private JPanel boardPanel;

    public SolitaireGUI() {

        setTitle("Peg Solitaire");
        setSize(700, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        game = createSelectedGame();
        add(createBoardPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        refreshBoard();

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();

        //Game Mode
        topPanel.add(new JLabel("Mode:"));

        manualButton = new JRadioButton("Manual", true);
        autoButton = new JRadioButton("Automated");
        ButtonGroup modeGroup = new ButtonGroup();

        modeGroup.add(manualButton);
        modeGroup.add(autoButton);

        topPanel.add(manualButton);
        topPanel.add(autoButton);

        //Board Size
        topPanel.add(new JLabel("Board Size:"));

        sizeComboBox = new JComboBox<>(new String[]{"7", "9", "11"});
        topPanel.add(sizeComboBox);

        //Board Type
        topPanel.add(new JLabel("Board Type:"));

        englishButton = new JRadioButton("English", true);
        diamondButton = new JRadioButton("Diamond"); //unselectable
        hexagonButton = new JRadioButton("Hexagon"); //unselectable

        ButtonGroup group = new ButtonGroup();
        group.add(englishButton);
        group.add(diamondButton);
        group.add(hexagonButton);

        topPanel.add(englishButton);
        topPanel.add(diamondButton);
        topPanel.add(hexagonButton);

        manualButton.addActionListener(e -> resetGame());
        autoButton.addActionListener(e -> resetGame());

        return topPanel;
    }

    private int getSelectedSize() {
        return Integer.parseInt((String) sizeComboBox.getSelectedItem());
    }

    private BoardType getSelectedBoardType() {
        if (englishButton.isSelected()) return BoardType.ENGLISH;
        else if (diamondButton.isSelected()) return BoardType.DIAMOND;
        else if (hexagonButton.isSelected()) return BoardType.HEXAGON;
        return null;
    }

    private Game createSelectedGame() {
        int size = getSelectedSize();
        BoardType type = getSelectedBoardType();

        if (manualButton.isSelected()) {
            return new ManualGame(size, type);
        } else {
            return new AutoGame(size, type);
        }
    }

    private JPanel createBoardPanel() {
        Board board = game.getBoard();
        int size = board.getSize();

        boardPanel = new JPanel(new GridLayout(size, size));
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

        newGameButton = new JButton("Reset");
        newGameButton.addActionListener(e -> resetGame());

        JButton playTurnButton = new JButton("Play Turn");
        JButton playAllButton = new JButton("Play All");

        // Disable both buttons if in manual mode by default
        boolean isAuto = autoButton.isSelected();
        playTurnButton.setEnabled(isAuto);
        playAllButton.setEnabled(isAuto);

        // Also toggle when mode is switched
        manualButton.addActionListener(e -> {
            playTurnButton.setEnabled(false);
            playAllButton.setEnabled(false);
        });
        autoButton.addActionListener(e -> {
            playTurnButton.setEnabled(true);
            playAllButton.setEnabled(true);
        });

        playTurnButton.addActionListener(e -> {
            if (autoButton.isSelected()) {
                game.playTurn();
                refreshBoard();
                if (game.isGameOver()) {
                    handleGameOver();
                }
            }
        });

        playAllButton.addActionListener(e -> {
            playTurnButton.setEnabled(false); // disable play turn while running
            Timer timer = new Timer(500, null);
            timer.addActionListener(tick -> {
                if (game.isGameOver()) {
                    timer.stop();
                    playTurnButton.setEnabled(true); // re-enable after done
                    handleGameOver();
                    return;
                }
                game.playTurn();
                refreshBoard();
            });
            timer.start();
        });

        JButton randomizeButton = new JButton("Randomize");
        randomizeButton.addActionListener(e -> {
            game.randomizeBoard();
            refreshBoard();
        });

        bottomPanel.add(newGameButton);
        bottomPanel.add(playTurnButton);
        bottomPanel.add(playAllButton);
        bottomPanel.add(randomizeButton);

        return bottomPanel;
    }

    private void handleClick(int row, int col) {

        if (autoButton.isSelected()) return;
        Board board = game.getBoard();
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

        if (game.makeMove(move)) {
            // valid move
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();

            if (game.isGameOver()) {
                handleGameOver();
            }
        } else {
            // invalid move: cancel selection
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
        }
    }

    private void handleGameOver() {
        if (game.isWin()) {
            JOptionPane.showMessageDialog(this, "WINNER WINNER CHICKEN DINNER!");
        } else {
            JOptionPane.showMessageDialog(this, "Game Over!");
        }

        resetGame();
    }

    private void resetGame() {
        game = createSelectedGame();
        selectedRow = -1;
        selectedCol = -1;
        remove(boardPanel);
        add(createBoardPanel(), BorderLayout.CENTER);
        revalidate();
        repaint();
        refreshBoard();
    }

    private void refreshBoard() {
        Board board = game.getBoard();
        int size = board.getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                CellState state = board.getCell(r, c);
                JButton button = buttons[r][c];

                if (state == CellState.INVALID) {
                    button.setVisible(true);
                    button.setEnabled(false);
                    button.setBackground(Color.LIGHT_GRAY);
                    button.setText("");
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

//