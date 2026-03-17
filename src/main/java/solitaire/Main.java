package solitaire;

import javax.swing.SwingUtilities;
import solitaire.ui.SolitaireGUI;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SolitaireGUI::new);
    }
}