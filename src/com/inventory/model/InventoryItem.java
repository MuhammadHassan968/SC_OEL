package com.inventory.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Entity class representing an individual item in the inventory.
 * <p>
 * Encapsulates the item's unique identifier, name, current quantity,
 * automated stock status, and timestamp of the most recent modification.
 * Adheres to strict validation constraints:
 * <ul>
 *   <li>ID must be positive.</li>
 *   <li>Name must not be null or blank.</li>
 *   <li>Quantity must be non-negative.</li>
 * </ul>
 *
 * @author Muhammad Hassan
 * @version 1.0
 */
public class InventoryItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Unique identification number for the inventory item. */
    private int id;
    /** Commercial name of the product. */
    private String name;
    /** Current quantity available in stock. */
    private int quantity;
    /** Calculated or assigned stock level status. */
    private StockStatus status;
    /** Timestamp string representing the most recent modification. */
    private String lastUpdated;

    /**
     * Constructs a new {@code InventoryItem} with an automatic status evaluation.
     *
     * @param id       Unique identifier for the item. Must be greater than 0.
     * @param name     Name of the inventory product. Cannot be null or blank.
     * @param quantity Available stock quantity. Must be non-negative.
     * @throws IllegalArgumentException if name is null/empty or quantity is negative.
     */
    public InventoryItem(int id, String name, int quantity) {
        validateName(name);
        validateQuantity(quantity);
        this.id = id;
        this.name = name.trim();
        this.quantity = quantity;
        this.status = StockStatus.determineStatus(quantity);
        updateTimestamp();
    }

    /**
     * Constructs a new {@code InventoryItem} with explicitly specified status.
     *
     * @param id       Unique identifier for the item. Must be greater than 0.
     * @param name     Name of the inventory product. Cannot be null or blank.
     * @param quantity Available stock quantity. Must be non-negative.
     * @param status   Explicit {@link StockStatus} override. If null, automatically determined.
     * @throws IllegalArgumentException if name is null/empty or quantity is negative.
     */
    public InventoryItem(int id, String name, int quantity, StockStatus status) {
        validateName(name);
        validateQuantity(quantity);
        this.id = id;
        this.name = name.trim();
        this.quantity = quantity;
        this.status = (status != null) ? status : StockStatus.determineStatus(quantity);
        updateTimestamp();
    }

    /**
     * Validates that the product name is not null, empty, or consisting solely of whitespace.
     *
     * @param name Name string to validate.
     * @throws IllegalArgumentException if name is invalid.
     */
    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty or whitespace.");
        }
    }

    /**
     * Validates that the quantity is a non-negative integer.
     *
     * @param quantity Quantity to validate.
     * @throws IllegalArgumentException if quantity is less than 0.
     */
    public static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
    }

    /**
     * Updates the last modified timestamp to current date and time.
     */
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now().format(DATE_FORMATTER);
    }

    /**
     * Retrieves the item's unique identifier.
     *
     * @return Integer ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the item's unique identifier.
     *
     * @param id New unique identifier.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retrieves the product name.
     *
     * @return Item name.
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the product name after validating constraints.
     *
     * @param name New valid product name.
     * @throws IllegalArgumentException if name is empty or null.
     */
    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
        updateTimestamp();
    }

    /**
     * Retrieves the current stock quantity.
     *
     * @return Current quantity.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Updates the stock quantity, automatically recalibrates {@link StockStatus},
     * and refreshes the modification timestamp.
     *
     * @param quantity New quantity (must be non-negative).
     * @throws IllegalArgumentException if quantity is negative.
     */
    public void setQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
        this.status = StockStatus.determineStatus(quantity);
        updateTimestamp();
    }

    /**
     * Retrieves the current stock status.
     *
     * @return {@link StockStatus} indicating In Stock, Low Stock, or Out of Stock.
     */
    public StockStatus getStatus() {
        return status;
    }

    /**
     * Explicitly sets the stock status.
     *
     * @param status New {@link StockStatus}.
     */
    public void setStatus(StockStatus status) {
        this.status = (status != null) ? status : StockStatus.determineStatus(this.quantity);
        updateTimestamp();
    }

    /**
     * Retrieves the formatted timestamp of the most recent modification.
     *
     * @return Formatted date-time string.
     */
    public String getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Sets the last updated timestamp.
     *
     * @param lastUpdated Timestamp string.
     */
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Item #%d: %s | Qty: %d | Status: %s | Updated: %s",
                id, name, quantity, status.getDisplayName(), lastUpdated);
    }
}
