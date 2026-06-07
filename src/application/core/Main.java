package application.core;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * CORE BREAKER
 *
 * Main.java
 * Entry point of the application.
 * Creates the JFrame window and initializes the GamePanel on the
 * Event Dispatch Thread (EDT) as required by Swing's threading model.
 *
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) {
        // All Swing UI creation must happen on the EDT
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("CORE BREAKER");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false); // Fixed 800x600 window
                frame.add(new GamePanel());
                frame.pack();
                frame.setLocationRelativeTo(null); // Center on screen
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}