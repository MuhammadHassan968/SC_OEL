package com.inventory.service;

import com.inventory.model.InventoryItem;
import com.inventory.model.StockStatus;

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
 *   <li>Adding new inventory items with unique auto-incremented identifiers and validation.</li>
 *   <li>Retrieving all items or querying specific items by ID or name.</li>
 *   <li>Modifying existing items while handling non-existent updates gracefully.</li>
 *   <li>Removing items safely by ID.</li>
 *   <li>Monitoring stock level thresholds (In Stock, Low Stock, Out of Stock).</li>
 * </ul>
 *
 * @author Muhammad Hassan
 * @version 1.0
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
        this.idCounter = new AtomicInteger(100); // Start IDs at 101 for realistic SKU-style IDs
    }

    /**
     * Adds a new item to the inventory after performing rigorous validation.
     *
     * @param name     Name of the product. Cannot be empty or null.
     * @param quantity Initial quantity on hand. Must be non-negative.
     * @return The newly created {@link InventoryItem}.
     * @throws IllegalArgumentException if the name is empty or quantity is negative.
     */
    public InventoryItem addItem(String name, int quantity) {
        InventoryItem.validateName(name);
        InventoryItem.validateQuantity(quantity);

        int newId = idCounter.incrementAndGet();
        InventoryItem item = new InventoryItem(newId, name, quantity);
        items.add(item);
        return item;
    }

    /**
     * Adds an existing item instance directly into the repository.
     *
     * @param item Item to insert. Must not be null.
     * @return The added {@link InventoryItem}.
     * @throws IllegalArgumentException if item is null.
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
                .filter(item -> item.getName().toLowerCase().contains(lower))
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
     * Updates an existing item's name and quantity.
     * Handles non-existing items gracefully by returning {@code false}.
     *
     * @param id          Unique identifier of the target item.
     * @param newName     Updated product name. Cannot be empty.
     * @param newQuantity Updated stock quantity. Must be non-negative.
     * @return {@code true} if the item was located and successfully updated;
     *         {@code false} if no item with the given ID exists.
     * @throws IllegalArgumentException if the updated name is empty or quantity is negative.
     */
    public boolean updateItem(int id, String newName, int newQuantity) {
        InventoryItem.validateName(newName);
        InventoryItem.validateQuantity(newQuantity);

        Optional<InventoryItem> optionalItem = findById(id);
        if (optionalItem.isPresent()) {
            InventoryItem item = optionalItem.get();
            item.setName(newName);
            item.setQuantity(newQuantity);
            return true;
        }
        // Graceful handling for non-existent item
        return false;
    }

    /**
     * Updates an existing item's name, quantity, and explicit status override.
     *
     * @param id          Unique identifier of the target item.
     * @param newName     Updated product name. Cannot be empty.
     * @param newQuantity Updated stock quantity. Must be non-negative.
     * @param newStatus   Explicit status override (or null for automatic).
     * @return {@code true} if updated successfully; {@code false} if item not found.
     * @throws IllegalArgumentException if name is invalid or quantity is negative.
     */
    public boolean updateItem(int id, String newName, int newQuantity, StockStatus newStatus) {
        InventoryItem.validateName(newName);
        InventoryItem.validateQuantity(newQuantity);

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
     * Removes an item from the inventory by its unique identifier.
     *
     * @param id Identifier of the item to delete.
     * @return {@code true} if the item was found and deleted; {@code false} otherwise.
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
}
