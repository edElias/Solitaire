package solitaire.ui;

import solitaire.logic.AutoGame;
import solitaire.logic.ManualGame;
import solitaire.logic.Game;
import solitaire.logic.GameRecorder;
import solitaire.logic.GameReplayer;
import solitaire.logic.Move;
import solitaire.model.Board;
import solitaire.model.BoardType;
import solitaire.model.CellState;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.IOException;

public class SolitaireGUI extends JFrame {

    // Core game state
    private Game game;
    private JButton[][] buttons;
    private int selectedRow = -1;
    private int selectedCol = -1;

    // Top-panel controls
    private JComboBox<String> sizeComboBox;
    private JRadioButton englishButton, diamondButton, hexagonButton;
    private JRadioButton manualButton, autoButton;
    private JCheckBox recordCheckBox;

    // Board panel
    private JPanel boardPanel;

    // Bottom-panel controls
    private JButton newGameButton, playTurnButton, playAllButton;
    private JButton randomizeButton, replayButton;

    // Replay-controls panel (hidden until replay starts)
    private JPanel replayControlPanel;
    private JButton restartBtn, rewindBtn, pausePlayBtn, stepBtn, stopReplayBtn;
    private JSlider speedSlider;
    private JLabel stepLabel;

    // Recording
    private final GameRecorder recorder = new GameRecorder();

    // Replaying
    private GameReplayer replayer = null;
    private boolean inReplayMode = false;
    private boolean replayPlaying = false;
    private Timer replayTimer;

    // Autoplay timer
    private Timer autoplayTimer;


    public SolitaireGUI() {
        setTitle("Peg Solitaire");
        setSize(950, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        game = createSelectedGame();
        add(createBoardPanel(), BorderLayout.CENTER);

        JPanel southWrapper = new JPanel(new BorderLayout());
        southWrapper.add(createBottomPanel(), BorderLayout.NORTH);
        southWrapper.add(createReplayControlPanel(), BorderLayout.SOUTH);
        add(southWrapper, BorderLayout.SOUTH);

        refreshBoard();
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Panel builders
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel createTopPanel() {
        JPanel wrapper = new JPanel(new GridLayout(2, 1));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        // Row 1: Mode + Size + Type
        row1.add(new JLabel("Mode:"));
        manualButton = new JRadioButton("Manual", true);
        autoButton   = new JRadioButton("Automated");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(manualButton);
        modeGroup.add(autoButton);
        row1.add(manualButton);
        row1.add(autoButton);

        row1.add(new JLabel("Board Size:"));
        sizeComboBox = new JComboBox<>(new String[]{"7", "9", "11"});
        row1.add(sizeComboBox);

        row1.add(new JLabel("Board Type:"));
        englishButton = new JRadioButton("English", true);
        diamondButton = new JRadioButton("Diamond");
        hexagonButton = new JRadioButton("Hexagon");
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(englishButton);
        typeGroup.add(diamondButton);
        typeGroup.add(hexagonButton);
        row1.add(englishButton);
        row1.add(diamondButton);
        row1.add(hexagonButton);

        // Row 2: Record checkbox
        recordCheckBox = new JCheckBox("Record Game");
        row2.add(recordCheckBox);

        manualButton.addActionListener(e -> resetGame());
        autoButton.addActionListener(e -> resetGame());

        wrapper.add(row1);
        wrapper.add(row2);
        return wrapper;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));

        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> resetGame());

        playTurnButton = new JButton("Play Turn");
        playAllButton  = new JButton("Play All");
        randomizeButton = new JButton("Randomize");
        replayButton   = new JButton("Replay");

        boolean isAuto = autoButton.isSelected();
        playTurnButton.setEnabled(isAuto);
        playAllButton.setEnabled(isAuto);

        manualButton.addActionListener(e -> {
            playTurnButton.setEnabled(false);
            playAllButton.setEnabled(false);
        });
        autoButton.addActionListener(e -> {
            playTurnButton.setEnabled(true);
            playAllButton.setEnabled(true);
        });

        playTurnButton.addActionListener(e -> {
            if (!autoButton.isSelected()) return;
            Move move = peekAutoMove();
            game.playTurn();
            if (recorder.isRecording() && move != null) recorder.recordMove(move);
            refreshBoard();
            if (game.isGameOver()) handleGameOver();
        });

        playAllButton.addActionListener(e -> {
            playTurnButton.setEnabled(false);
            stopAutoplayTimer();
            autoplayTimer = new Timer(500, null);
            autoplayTimer.addActionListener(tick -> {
                if (game.isGameOver()) {
                    autoplayTimer.stop();
                    playTurnButton.setEnabled(true);
                    handleGameOver();
                    return;
                }
                Move move = peekAutoMove();
                game.playTurn();
                if (recorder.isRecording() && move != null) recorder.recordMove(move);
                refreshBoard();
            });
            autoplayTimer.start();
        });

        randomizeButton.addActionListener(e -> {
            game.randomizeBoard();
            if (recorder.isRecording()) {
                int size = game.getBoard().getSize();
                int[][] snapshot = new int[size][size];
                for (int r = 0; r < size; r++)
                    for (int c = 0; c < size; c++)
                        snapshot[r][c] = cellToInt(game.getBoard().getCell(r, c));
                recorder.recordRandomize(snapshot, size);
            }
            refreshBoard();
        });

        replayButton.addActionListener(e -> startReplay());

        bottomPanel.add(newGameButton);
        bottomPanel.add(playTurnButton);
        bottomPanel.add(playAllButton);
        bottomPanel.add(randomizeButton);
        bottomPanel.add(replayButton);

        return bottomPanel;
    }

    /**
     * The replay-controls bar — hidden until a replay is loaded.
     */
    private JPanel createReplayControlPanel() {
        replayControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        replayControlPanel.setVisible(false);

        restartBtn  = new JButton("⏮ Restart");
        rewindBtn   = new JButton("◀ Back");
        pausePlayBtn = new JButton("▶ Play");
        stepBtn     = new JButton("Step ▶");
        stopReplayBtn = new JButton("■ Stop");

        stepLabel = new JLabel("Step 0 / 0");

        // Speed: 100 ms (fast) → 2000 ms (slow). Slider left = slow, right = fast.
        speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 2000, 600);
        speedSlider.setInverted(true); // left = slow, right = fast feels more natural inverted
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setToolTipText("Playback speed");
        Dimension sliderSize = speedSlider.getPreferredSize();
        sliderSize.width = 150;
        speedSlider.setPreferredSize(sliderSize);

        speedSlider.addChangeListener(e -> {
            if (replayTimer != null) replayTimer.setDelay(speedSlider.getValue());
        });

        restartBtn.addActionListener(e -> {
            pauseReplay();
            replayer.restart(game);
            refreshBoard();
            updateStepLabel();
        });

        rewindBtn.addActionListener(e -> {
            pauseReplay();
            replayer.stepBack(game);
            refreshBoard();
            updateStepLabel();
        });

        pausePlayBtn.addActionListener(e -> {
            if (replayPlaying) pauseReplay();
            else               resumeReplay();
        });

        stepBtn.addActionListener(e -> {
            pauseReplay();
            replayer.stepForward(game);
            refreshBoard();
            updateStepLabel();
            if (!replayer.hasNext()) handleReplayEnd();
        });

        stopReplayBtn.addActionListener(e -> endReplayMode());

        replayControlPanel.add(restartBtn);
        replayControlPanel.add(rewindBtn);
        replayControlPanel.add(pausePlayBtn);
        replayControlPanel.add(stepBtn);
        replayControlPanel.add(new JLabel("Speed:"));
        replayControlPanel.add(speedSlider);
        replayControlPanel.add(stepLabel);
        replayControlPanel.add(stopReplayBtn);

        return replayControlPanel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Replay lifecycle
    // ══════════════════════════════════════════════════════════════════════════

    private void startReplay() {
        String path = "game_record.txt";
        replayer = new GameReplayer();
        try {
            replayer.loadFromFile(path);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load file:\n" + ex.getMessage());
            return;
        }

        // Set up the game to match the recording
        int size = replayer.getBoardSize();
        BoardType type = replayer.getBoardType();
        game = new ManualGame(size, type);
        replayer.initSnapshots(game);

        // Rebuild board UI for potentially different size
        remove(boardPanel);
        add(createBoardPanel(), BorderLayout.CENTER);
        revalidate();
        repaint();
        refreshBoard();

        inReplayMode = true;
        setReplayControlsEnabled(true);
        replayControlPanel.setVisible(true);
        setGameControlsEnabled(false);
        updateStepLabel();

        // Auto-start playing
        resumeReplay();
    }

    private void resumeReplay() {
        replayPlaying = true;
        pausePlayBtn.setText("⏸ Pause");
        if (replayTimer != null) replayTimer.stop();
        replayTimer = new Timer(speedSlider.getValue(), null);
        replayTimer.addActionListener(tick -> {
            if (!replayer.hasNext()) {
                replayTimer.stop();
                replayPlaying = false;
                pausePlayBtn.setText("▶ Play");
                handleReplayEnd();
                return;
            }
            replayer.stepForward(game);
            refreshBoard();
            updateStepLabel();
        });
        replayTimer.start();
    }

    private void pauseReplay() {
        replayPlaying = false;
        pausePlayBtn.setText("▶ Play");
        if (replayTimer != null) replayTimer.stop();
    }

    private void handleReplayEnd() {
        pauseReplay();
        JOptionPane.showMessageDialog(this, "Replay finished!");
    }

    private void endReplayMode() {
        pauseReplay();
        inReplayMode = false;
        replayControlPanel.setVisible(false);
        setGameControlsEnabled(true);
        resetGame();
    }

    private void updateStepLabel() {
        if (replayer != null) {
            stepLabel.setText("Step " + replayer.getCursor() + " / " + replayer.getTotalSteps());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Board interaction
    // ══════════════════════════════════════════════════════════════════════════

    private void handleClick(int row, int col) {
        if (autoButton.isSelected() || inReplayMode) return;

        Board board = game.getBoard();
        CellState clickedCell = board.getCell(row, col);

        if (selectedRow == -1) {
            if (clickedCell == CellState.PEG) {
                selectedRow = row;
                selectedCol = col;
                refreshBoard();
            }
            return;
        }

        if (selectedRow == row && selectedCol == col) {
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
            return;
        }

        Move move = new Move(selectedRow, selectedCol, row, col);

        if (game.makeMove(move)) {
            if (recorder.isRecording()) recorder.recordMove(move);
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
            if (game.isGameOver()) handleGameOver();
        } else {
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Game lifecycle
    // ══════════════════════════════════════════════════════════════════════════

    private void handleGameOver() {
        stopAutoplayTimer();
        if (recorder.isRecording()) {
            recorder.stopRecording();
            promptSaveRecording();
        }

        if (game.isWin()) {
            JOptionPane.showMessageDialog(this, "WINNER WINNER CHICKEN DINNER!");
        } else {
            JOptionPane.showMessageDialog(this, "Game Over!");
        }
        resetGame();
    }

    private void resetGame() {
        stopAutoplayTimer();
        if (recorder.isRecording()) recorder.stopRecording();

        game = createSelectedGame();
        selectedRow = -1;
        selectedCol = -1;

        if (recordCheckBox.isSelected()) {
            recorder.startRecording(getSelectedSize(), getSelectedBoardType());
        }

        remove(boardPanel);
        add(createBoardPanel(), BorderLayout.CENTER);
        revalidate();
        repaint();
        refreshBoard();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Save recording
    // ══════════════════════════════════════════════════════════════════════════

    private void promptSaveRecording() {
        String path = "game_record.txt";
        try {
            recorder.saveToFile(path);
            JOptionPane.showMessageDialog(this, "Recording saved to: " + path);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save:\n" + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Board rendering
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel createBoardPanel() {
        Board board = game.getBoard();
        int size = board.getSize();

        boardPanel = new JPanel(new GridLayout(size, size));
        buttons = new JButton[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                JButton button = new JButton();
                buttons[r][c] = button;
                int row = r, col = c;
                button.addActionListener(e -> handleClick(row, col));
                boardPanel.add(button);
            }
        }
        return boardPanel;
    }

    private void refreshBoard() {
        Board board = game.getBoard();
        int size = board.getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                CellState state = board.getCell(r, c);
                JButton button = buttons[r][c];

                if (state == CellState.INVALID) {
                    button.setEnabled(false);
                    button.setBackground(Color.LIGHT_GRAY);
                    button.setText("");
                } else {
                    button.setEnabled(!inReplayMode);
                    button.setOpaque(true);
                    button.setBorderPainted(true);

                    if (state == CellState.PEG) {
                        button.setText("●");
                        button.setBackground(Color.WHITE);
                        button.setForeground(Color.BLACK);
                    } else {
                        button.setText("");
                        button.setBackground(Color.WHITE);
                    }

                    if (r == selectedRow && c == selectedCol) {
                        button.setBackground(Color.RED);
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /** Grab the move AutoGame would make without applying it yet. */
    private Move peekAutoMove() {
        if (game instanceof AutoGame ag) {
            var moves = ag.getAllValidMoves();
            return moves.isEmpty() ? null : moves.get(new java.util.Random().nextInt(moves.size()));
        }
        return null;
    }

    private void stopAutoplayTimer() {
        if (autoplayTimer != null) { autoplayTimer.stop(); autoplayTimer = null; }
    }

    private void setGameControlsEnabled(boolean enabled) {
        newGameButton.setEnabled(enabled);
        randomizeButton.setEnabled(enabled);
        replayButton.setEnabled(enabled);
        recordCheckBox.setEnabled(enabled);
        sizeComboBox.setEnabled(enabled);
        englishButton.setEnabled(enabled);
        diamondButton.setEnabled(enabled);
        hexagonButton.setEnabled(enabled);
        manualButton.setEnabled(enabled);
        autoButton.setEnabled(enabled);
        boolean autoEnabled = enabled && autoButton.isSelected();
        playTurnButton.setEnabled(autoEnabled);
        playAllButton.setEnabled(autoEnabled);
    }

    private void setReplayControlsEnabled(boolean enabled) {
        restartBtn.setEnabled(enabled);
        rewindBtn.setEnabled(enabled);
        pausePlayBtn.setEnabled(enabled);
        stepBtn.setEnabled(enabled);
        stopReplayBtn.setEnabled(enabled);
        speedSlider.setEnabled(enabled);
    }

    private int cellToInt(CellState s) {
        return switch (s) {
            case PEG -> 1;
            case EMPTY -> 0;
            case INVALID -> -1;
        };
    }

    private int getSelectedSize() {
        return Integer.parseInt((String) sizeComboBox.getSelectedItem());
    }

    private BoardType getSelectedBoardType() {
        if (englishButton.isSelected()) return BoardType.ENGLISH;
        else if (diamondButton.isSelected()) return BoardType.DIAMOND;
        else if (hexagonButton.isSelected()) return BoardType.HEXAGON;
        return BoardType.ENGLISH;
    }

    private Game createSelectedGame() {
        int size = getSelectedSize();
        BoardType type = getSelectedBoardType();
        return manualButton.isSelected() ? new ManualGame(size, type) : new AutoGame(size, type);
    }
}