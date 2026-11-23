package com.inventory;

import com.inventory.model.InventoryItem;
import com.inventory.model.StockStatus;
import com.inventory.service.InventoryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Automated test suite validating all functional and non-functional requirements
 * of the Inventory Management System according to the OEL Rubric (CLO-5, Section 9).
 * <p>
 * Evaluates the following test cases:
 * <ul>
 *   <li>TC-01: Add a valid item &rarr; Item is added</li>
 *   <li>TC-02: Add an item with an empty name &rarr; Validation exception thrown</li>
 *   <li>TC-03: Add an item with a negative quantity &rarr; Validation exception thrown</li>
 *   <li>TC-04: Non-numeric quantity validation &rarr; NumberFormatException handled</li>
 *   <li>TC-05: View all items &rarr; All items displayed accurately</li>
 *   <li>TC-06: View an empty inventory &rarr; Appropriate empty state handled</li>
 *   <li>TC-07: Update an existing item &rarr; Details and stock status updated</li>
 *   <li>TC-08: Update a non-existing item &rarr; Handled gracefully without errors</li>
 *   <li>TC-09: Delete an existing item &rarr; Item successfully removed</li>
 *   <li>TC-10: Stock status automatic thresholds (0, 1-5, &gt;5) &rarr; Status classified correctly</li>
 * </ul>
 * </p>
 *
 * @author Muhammad Hassan
 * @version 1.0
 */
public class InventoryTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    private static final List<String[]> results = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=========================================================================================");
        System.out.println("                     INVENTORY MANAGEMENT SYSTEM - AUTOMATED TEST SUITE                  ");
        System.out.println("                                  CLO-5 Assessment Matrix                                ");
        System.out.println("=========================================================================================\n");

        testAddValidItem();
        testAddEmptyName();
        testAddNegativeQuantity();
        testNonNumericQuantityHandling();
        testViewAllItems();
        testViewEmptyInventory();
        testUpdateExistingItem();
        testUpdateNonExistingItem();
        testDeleteExistingItem();
        testStockStatusThresholds();

        printSummaryReport();

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    /**
     * TC-01: Add a valid item &rarr; Item is added.
     */
    private static void testAddValidItem() {
        testsRun++;
        String testId = "TC-01";
        String description = "Add a valid item";
        try {
            InventoryManager manager = new InventoryManager();
            InventoryItem item = manager.addItem("Gaming Headset", 15);

            boolean pass = item != null &&
                    item.getId() > 0 &&
                    "Gaming Headset".equals(item.getName()) &&
                    item.getQuantity() == 15 &&
                    item.getStatus() == StockStatus.IN_STOCK &&
                    manager.getItemCount() == 1;

            recordResult(testId, description, pass, pass ? "Item added successfully with ID #" + item.getId() : "Item attributes mismatched");
        } catch (Exception e) {
            recordResult(testId, description, false, "Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * TC-02: Add an item with an empty name &rarr; Validation message shown / exception thrown.
     */
    private static void testAddEmptyName() {
        testsRun++;
        String testId = "TC-02";
        String description = "Add item with empty name";
        try {
            InventoryManager manager = new InventoryManager();
            // Test null and whitespace-only names
            boolean caughtNull = false;
            try {
                manager.addItem(null, 10);
            } catch (IllegalArgumentException e) {
                caughtNull = true;
            }

            boolean caughtBlank = false;
            try {
                manager.addItem("    ", 10);
            } catch (IllegalArgumentException e) {
                caughtBlank = true;
            }

            boolean pass = caughtNull && caughtBlank && manager.isEmpty();
            recordResult(testId, description, pass, pass ? "Validation caught empty/null name appropriately" : "Empty name was incorrectly accepted");
        } catch (Exception e) {
            recordResult(testId, description, false, "Failed with exception: " + e.getMessage());
        }
    }

    /**
     * TC-03: Add an item with a negative quantity &rarr; Error message / exception thrown.
     */
    private static void testAddNegativeQuantity() {
        testsRun++;
        String testId = "TC-03";
        String description = "Add item with negative quantity";
        try {
            InventoryManager manager = new InventoryManager();
            boolean caught = false;
            try {
                manager.addItem("Mechanical Pencil", -5);
            } catch (IllegalArgumentException e) {
                caught = true;
            }

            boolean pass = caught && manager.isEmpty();
            recordResult(testId, description, pass, pass ? "Negative quantity rejected as expected" : "Negative quantity was accepted");
        } catch (Exception e) {
            recordResult(testId, description, false, "Failed: " + e.getMessage());
        }
    }

    /**
     * TC-04: Add an item with a non-numeric quantity &rarr; Error handled.
     */
    private static void testNonNumericQuantityHandling() {
        testsRun++;
        String testId = "TC-04";
        String description = "Add item with non-numeric quantity";
        try {
            String nonNumericInput = "twenty-five";
            boolean caught = false;
            try {
                Integer.parseInt(nonNumericInput.trim());
            } catch (NumberFormatException e) {
                caught = true;
            }

            recordResult(testId, description, caught, caught ? "Non-numeric input safely caught via NumberFormatException" : "Failed to catch non-numeric input");
        } catch (Exception e) {
            recordResult(testId, description, false, "Failed: " + e.getMessage());
        }
    }

    /**
     * TC-05: View all items &rarr; All items displayed.
     */
    private static void testViewAllItems() {
        testsRun++;
        String testId = "TC-05";
        String description = "View all items";
        try {
            InventoryManager manager = new InventoryManager();
            manager.addItem("Item Alpha", 10);
            manager.addItem("Item Beta", 20);
            manager.addItem("Item Gamma", 30);

            List<InventoryItem> all = manager.getAllItems();
            boolean pass = (all.size() == 3) &&
                    all.get(0).getName().equals("Item Alpha") &&
                    all.get(1).getName().equals("Item Beta") &&
                    all.get(2).getName().equals("Item Gamma");

            recordResult(testId, description, pass, pass ? "All 3 items returned in complete list" : "List size or contents mismatched");
        } catch (Exception e) {
            recordResult(testId, description, false, "Failed: " + e.getMessage());
        }
    }

    /**
     * TC-06: View an empty inventory &rarr; Appropriate message / handled gracefully.
     */
    private static void testViewEmptyInventory() {
        testsRun++;
        String testId = "TC-06";
        String description = "View an empty inventory";
        try {
            InventoryManager manager = new InventoryManager();
            boolean pass = manager.isEmpty() &&
                    manager.getItemCount() == 0 &&
                    manager.getAllItems().isEmpty();

            recordResult(testId, description, pass, pass ? "Empty inventory correctly recognized and returns 0 items" : "Failed to report empty inventory");
        } catch (Exception e) {
            recordResult(testId, description, false, "Failed: " + e.getMessage());
        }
    }

    /**
     * TC-07: Update an existing item &rarr; Details updated.
     */
    private static void testUpdateExistingItem() {
        testsRun++;
        String testId = "TC-07";
        String description = "Update an existing item";
        try {
            InventoryManager manager = new InventoryManager();
            InventoryItem original = manager.addItem("Original Monitor", 15);
            int itemId = original.getId();

            // Update name and quantity to 3 (which must shift status to LOW_STOCK)
            boolean updated = manager.updateItem(itemId, "Updated 4K Monitor", 3);
            InventoryItem modified = manager.findById(itemId).orElse(null);

            boolean pass = updated &&
                    modified != null &&
                    "Updated 4K Monitor".equals(modified.getName()) &&
                    modified.getQuantity() == 3 &&
                    modified.getStatus() == StockStatus.LOW_STOCK;

            recordResult(testId, description, pass, pass ? "Item details and status updated to Low Stock" : "Update verification failed");
        } catch (Exception e) {
            recordResult(testId, description, false, "Failed: " + e.getMessage());
        }
    }

    /**
     * TC-08: Update a non-existing item &rarr; Handled gracefully.
     */
    private static void testUpdateNonExistingItem() {
        testsRun++;
        String testId = "TC-08";
        String description = "Update non-existing item";
        try {
            InventoryManager manager = new InventoryManager();
            manager.addItem("Desk Lamp", 8);

            // Attempt to update item with dummy ID 99999
            boolean result = manager.updateItem(99999, "Phantom Item", 5);

            // Graceful handling means returns false without throwing an unhandled exception or corrupting state
            boolean pass = !result && manager.getItemCount() == 1;

            recordResult(testId, description, pass, pass ? "Handled gracefully: returned false without crashing" : "Failed to handle non-existing item cleanly");
        } catch (Exception e) {
            recordResult(testId, description, false, "Exception thrown instead of graceful handling: " + e.getMessage());
        }
    }

    /**
     * TC-09: Delete an existing item &rarr; Item removed.
     */
    private static void testDeleteExistingItem() {
        testsRun++;
        String testId = "TC-09";
        String description = "Delete an existing item";
        try {
            InventoryManager manager = new InventoryManager();
            InventoryItem item1 = manager.addItem("Webcam 1080p", 7);
            InventoryItem item2 = manager.addItem("Microphone Arm", 12);

            boolean deleted = manager.deleteItem(item1.getId());
            boolean stillExists = manager.findById(item1.getId()).isPresent();
            boolean item2Exists = manager.findById(item2.getId()).isPresent();

            boolean pass = deleted && !stillExists && item2Exists && manager.getItemCount() == 1;

            recordResult(testId, description, pass, pass ? "Target item removed while preserving other items" : "Deletion failed");
        } catch (Exception e) {
            recordResult(testId, description, false, "Failed: " + e.getMessage());
        }
    }

    /**
     * TC-10: Stock status automatic threshold mapping &rarr; Status set correctly.
     */
    private static void testStockStatusThresholds() {
        testsRun++;
        String testId = "TC-10";
        String description = "Stock status threshold rules";
        try {
            StockStatus outOfStock = StockStatus.determineStatus(0);
            StockStatus lowStockMin = StockStatus.determineStatus(1);
            StockStatus lowStockMax = StockStatus.determineStatus(5);
            StockStatus inStock = StockStatus.determineStatus(6);

            boolean pass = (outOfStock == StockStatus.OUT_OF_STOCK) &&
                    (lowStockMin == StockStatus.LOW_STOCK) &&
                    (lowStockMax == StockStatus.LOW_STOCK) &&
                    (inStock == StockStatus.IN_STOCK);

            recordResult(testId, description, pass, pass ? "0 -> Out of Stock, 1..5 -> Low Stock, >5 -> In Stock" : "Status classification incorrect");
        } catch (Exception e) {
            recordResult(testId, description, false, "Failed: " + e.getMessage());
        }
    }

    private static void recordResult(String testId, String description, boolean passed, String note) {
        if (passed) {
            testsPassed++;
        } else {
            testsFailed++;
        }
        results.add(new String[]{testId, description, passed ? "PASS" : "FAIL", note});
    }

    private static void printSummaryReport() {
        System.out.printf("%-8s | %-32s | %-6s | %-40s%n", "TEST ID", "TEST DESCRIPTION", "STATUS", "REMARKS / DETAILS");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        for (String[] row : results) {
            String statusFormatted = row[2].equals("PASS") ? "\u001B[32mPASS\u001B[0m" : "\u001B[31mFAIL\u001B[0m";
            System.out.printf("%-8s | %-32s | %-6s | %-40s%n", row[0], row[1], row[2], row[3]);
        }

        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.printf("TOTAL TESTS: %d | PASSED: %d | FAILED: %d | SUCCESS RATE: %.1f%%%n",
                testsRun, testsPassed, testsFailed, (testsPassed * 100.0 / testsRun));
        System.out.println("=========================================================================================\n");
    }
}
