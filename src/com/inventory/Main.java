package com.inventory;

import com.inventory.service.InventoryManager;
import com.inventory.ui.InventoryGUI;

import javax.swing.*;

/**
 * Main entry point for the Store Inventory Management System.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Configuring the modern system look and feel for native platform aesthetics.</li>
 *   <li>Instantiating the domain service layer ({@link InventoryManager}).</li>
 *   <li>Launching the graphical user interface ({@link InventoryGUI}) on the Swing Event Dispatch Thread (EDT).</li>
 * </ul>
 *
 * @author Muhammad Hassan
 * @version 1.0
 */
public class Main {

    /**
     * Private constructor to prevent instantiation of launcher class.
     */
    private Main() {
        // Prevent instantiation
    }

    /**
     * Application entry point.
     *
     * @param args Command-line arguments (optional).
     */
    public static void main(String[] args) {
        // Configure Look and Feel to match the host platform (Windows native look)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Notice: Could not set system look and feel. Defaulting to standard Swing UI. " + e.getMessage());
        }

        // Launch the graphical user interface safely on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                InventoryManager manager = new InventoryManager();
                InventoryGUI gui = new InventoryGUI(manager);
                gui.setVisible(true);
                System.out.println("=================================================");
                System.out.println(" Store Inventory Management System (IMS) Started ");
                System.out.println(" Developed with Java Swing & Modern SC Tools      ");
                System.out.println(" Author: Muhammad Hassan                         ");
                System.out.println("=================================================");
            } catch (Exception ex) {
                System.err.println("Fatal Error during application startup: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
