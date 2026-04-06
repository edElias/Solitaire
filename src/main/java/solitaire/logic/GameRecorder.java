package solitaire.logic;

import solitaire.model.BoardType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameRecorder {

    private final List<String> entries = new ArrayList<>();
    private boolean recording = false;
    private int boardSize;
    private BoardType boardType;

    public void startRecording(int size, BoardType type) {
        entries.clear();
        this.boardSize = size;
        this.boardType = type;
        this.recording = true;
    }

    public void stopRecording() {
        this.recording = false;
    }

    public boolean isRecording() {
        return recording;
    }

    public void recordMove(Move move) {
        if (!recording) return;
        entries.add("MOVE=" + move.getFromRow() + "," + move.getFromCol()
                + "," + move.getToRow() + "," + move.getToCol());
    }

    public void recordRandomize(int[][] boardSnapshot, int size) {
        if (!recording) return;
        StringBuilder sb = new StringBuilder("RANDOMIZE=");
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                sb.append(boardSnapshot[r][c]);
                if (r < size - 1 || c < size - 1) sb.append(",");
            }
        }
        entries.add(sb.toString());
    }

    public void saveToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("SIZE=" + boardSize);
            writer.newLine();
            writer.write("TYPE=" + boardType.name());
            writer.newLine();
            for (String entry : entries) {
                writer.write(entry);
                writer.newLine();
            }
        }
    }
}