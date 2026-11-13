package com.example.paint;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class PaintApp {
    private PaintApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Swing will use its default look and feel.
            }
            new PaintFrame().setVisible(true);
        });
    }
}
