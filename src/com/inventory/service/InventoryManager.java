package com.inventory.service;

import com.inventory.model.InventoryItem;
import com.inventory.model.StockStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service class that coordinates and executes core business logic and CRUD operations
 * for the Inventory Management System.
 * <p>
 * Responsibilities include:
 * <ul>
 *   <li>Adding new inventory items with unique auto-incremented identifiers, category, pricing, and validation.</li>
 *   <li>Retrieving all items or querying specific items by ID, name, or category.</li>
 *   <li>Modifying existing items while handling non-existent updates gracefully.</li>
 *   <li>Removing items safely by ID.</li>
 *   <li>Monitoring stock level thresholds (In Stock, Low Stock, Out of Stock).</li>
 *   <li>Calculating total inventory valuation (Net Worth).</li>
 *   <li>Exporting inventory data to CSV reports.</li>
 * </ul>
 *
 * @author Muhammad Hassan
 * @version 2.0
 */
public class InventoryManager {

    /** Internal in-memory repository of items. */
    private final List<InventoryItem> items;

    /** Thread-safe counter for generating unique item identifiers. */
    private final AtomicInteger idCounter;

    /**
     * Constructs a new {@code InventoryManager} with an empty inventory repository.
     */
    public InventoryManager() {
        this.items = new ArrayList<>();
        this.idCounter = new AtomicInteger(100);
    }

    /**
     * Adds a new item to the inventory with default category and zero price.
     *
     * @param name     Name of the product. Cannot be empty or null.
     * @param quantity Initial quantity on hand. Must be non-negative.
     * @return The newly created {@link InventoryItem}.
     */
    public InventoryItem addItem(String name, int quantity) {
        return addItem(name, "General", 0.0, quantity);
    }

    /**
     * Adds a new item to the inventory with category and unit price.
     *
     * @param name      Name of the product. Cannot be empty or null.
     * @param category  Category or department tag.
     * @param unitPrice Unit price. Must be non-negative.
     * @param quantity  Initial quantity on hand. Must be non-negative.
     * @return The newly created {@link InventoryItem}.
     */
    public InventoryItem addItem(String name, String category, double unitPrice, int quantity) {
        InventoryItem.validateName(name);
        InventoryItem.validatePrice(unitPrice);
        InventoryItem.validateQuantity(quantity);

        int newId = idCounter.incrementAndGet();
        InventoryItem item = new InventoryItem(newId, name, category, unitPrice, quantity);
        items.add(item);
        return item;
    }

    /**
     * Adds an existing item instance directly into the repository.
     *
     * @param item Item to insert. Must not be null.
     * @return The added {@link InventoryItem}.
     */
    public InventoryItem addItem(InventoryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add a null item to inventory.");
        }
        if (item.getId() <= 0) {
            item.setId(idCounter.incrementAndGet());
        } else if (item.getId() > idCounter.get()) {
            idCounter.set(item.getId());
        }
        items.add(item);
        return item;
    }

    /**
     * Retrieves an unmodifiable view of all inventory items.
     *
     * @return List of {@link InventoryItem} in the repository.
     */
    public List<InventoryItem> getAllItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    /**
     * Checks if the inventory is currently empty.
     *
     * @return {@code true} if no items exist, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Retrieves the total count of distinct item types in the inventory.
     *
     * @return Number of registered items.
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Finds an inventory item by its unique identifier.
     *
     * @param id The ID to look up.
     * @return An {@link Optional} containing the item if found, or empty if not present.
     */
    public Optional<InventoryItem> findById(int id) {
        return items.stream()
                .filter(item -> item.getId() == id)
                .findFirst();
    }

    /**
     * Searches for items whose names contain the given keyword (case-insensitive).
     *
     * @param keyword The search substring.
     * @return List of matching {@link InventoryItem} instances.
     */
    public List<InventoryItem> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllItems();
        }
        String lower = keyword.trim().toLowerCase();
        return items.stream()
                .filter(item -> item.getName().toLowerCase().contains(lower) ||
                                item.getCategory().toLowerCase().contains(lower) ||
                                String.valueOf(item.getId()).contains(lower))
                .collect(Collectors.toList());
    }

    /**
     * Filters inventory items by their current stock status.
     *
     * @param status The target {@link StockStatus}.
     * @return List of items matching the given status.
     */
    public List<InventoryItem> filterByStatus(StockStatus status) {
        if (status == null) {
            return getAllItems();
        }
        return items.stream()
                .filter(item -> item.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Filters inventory items by their category.
     *
     * @param category The category to filter by.
     * @return List of items matching category.
     */
    public List<InventoryItem> filterByCategory(String category) {
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("All Categories")) {
            return getAllItems();
        }
        return items.stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing item's name and quantity.
     *
     * @param id          Unique identifier of the target item.
     * @param newName     Updated product name. Cannot be empty.
     * @param newQuantity Updated stock quantity. Must be non-negative.
     * @return {@code true} if updated; {@code false} if not found.
     */
    public boolean updateItem(int id, String newName, int newQuantity) {
        Optional<InventoryItem> optionalItem = findById(id);
        if (optionalItem.isPresent()) {
            InventoryItem item = optionalItem.get();
            item.setName(newName);
            item.setQuantity(newQuantity);
            return true;
        }
        return false;
    }

    /**
     * Updates an existing item's name, quantity, and status.
     *
     * @param id          Target item ID.
     * @param newName     Updated name.
     * @param newQuantity Updated quantity.
     * @param newStatus   Status override.
     * @return {@code true} if updated; {@code false} otherwise.
     */
    public boolean updateItem(int id, String newName, int newQuantity, StockStatus newStatus) {
        Optional<InventoryItem> optionalItem = findById(id);
        if (optionalItem.isPresent()) {
            InventoryItem item = optionalItem.get();
            item.setName(newName);
            item.setQuantity(newQuantity);
            item.setStatus(newStatus);
            return true;
        }
        return false;
    }

    /**
     * Updates an existing item with full details: name, category, unit price, quantity, and status.
     *
     * @param id          Target item ID.
     * @param newName     Updated name.
     * @param newCategory Updated category.
     * @param newPrice    Updated unit price.
     * @param newQuantity Updated quantity.
     * @param newStatus   Status override (or null for auto).
     * @return {@code true} if updated; {@code false} if not found.
     */
    public boolean updateItem(int id, String newName, String newCategory, double newPrice, int newQuantity, StockStatus newStatus) {
        InventoryItem.validateName(newName);
        InventoryItem.validatePrice(newPrice);
        InventoryItem.validateQuantity(newQuantity);

        Optional<InventoryItem> optionalItem = findById(id);
        if (optionalItem.isPresent()) {
            InventoryItem item = optionalItem.get();
            item.setName(newName);
            item.setCategory(newCategory);
            item.setUnitPrice(newPrice);
            item.setQuantity(newQuantity);
            item.setStatus(newStatus);
            return true;
        }
        return false;
    }

    /**
     * Removes an item from the inventory by its unique identifier.
     *
     * @param id Identifier of the item to delete.
     * @return {@code true} if deleted; {@code false} otherwise.
     */
    public boolean deleteItem(int id) {
        return items.removeIf(item -> item.getId() == id);
    }

    /**
     * Clears all items from the inventory repository.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Calculates the cumulative stock units across all inventory items.
     *
     * @return Total integer units in inventory.
     */
    public int getTotalUnits() {
        return items.stream().mapToInt(InventoryItem::getQuantity).sum();
    }

    /**
     * Calculates the cumulative total net worth (monetary value) of the entire inventory.
     *
     * @return Sum of (unitPrice * quantity) across all items.
     */
    public double getTotalInventoryValue() {
        return items.stream().mapToDouble(InventoryItem::getTotalValue).sum();
    }

    /**
     * Counts the number of items that are currently Low Stock.
     *
     * @return Number of items needing replenishment.
     */
    public long getLowStockCount() {
        return items.stream().filter(i -> i.getStatus() == StockStatus.LOW_STOCK).count();
    }

    /**
     * Counts the number of items that are completely Out of Stock.
     *
     * @return Number of depleted items.
     */
    public long getOutOfStockCount() {
        return items.stream().filter(i -> i.getStatus() == StockStatus.OUT_OF_STOCK).count();
    }

    /**
     * Exports the current inventory to a standard Comma-Separated Values (CSV) file.
     *
     * @param file Target output file.
     * @throws IOException if writing fails.
     */
    public void exportToCSV(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("ID,Item Name,Category,Unit Price (PKR),Quantity,Total Value (PKR),Stock Status,Last Modified");
            writer.newLine();

            for (InventoryItem item : items) {
                String safeName = escapeCSV(item.getName());
                String safeCategory = escapeCSV(item.getCategory());
                writer.write(String.format("%d,%s,%s,%.2f,%d,%.2f,%s,%s",
                        item.getId(),
                        safeName,
                        safeCategory,
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getTotalValue(),
                        item.getStatus().getDisplayName(),
                        item.getLastUpdated()));
                writer.newLine();
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
