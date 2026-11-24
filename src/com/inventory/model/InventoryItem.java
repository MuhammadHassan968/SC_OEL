package com.inventory.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Entity class representing an individual item in the inventory.
 * <p>
 * Encapsulates the item's unique identifier, name, category, unit price, current quantity,
 * automated stock status, and timestamp of the most recent modification.
 * Adheres to strict validation constraints:
 * <ul>
 *   <li>ID must be positive.</li>
 *   <li>Name must not be null or blank.</li>
 *   <li>Price must be non-negative.</li>
 *   <li>Quantity must be non-negative.</li>
 * </ul>
 *
 * @author Muhammad Hassan
 * @version 2.0
 */
public class InventoryItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Unique identification number for the inventory item. */
    private int id;
    /** Commercial name of the product. */
    private String name;
    /** Product category / department tag. */
    private String category;
    /** Unit price of the item. */
    private double unitPrice;
    /** Current quantity available in stock. */
    private int quantity;
    /** Calculated or assigned stock level status. */
    private StockStatus status;
    /** Timestamp string representing the most recent modification. */
    private String lastUpdated;

    /**
     * Constructs a new {@code InventoryItem} with default category and zero price.
     *
     * @param id       Unique identifier for the item. Must be greater than 0.
     * @param name     Name of the inventory product. Cannot be null or blank.
     * @param quantity Available stock quantity. Must be non-negative.
     * @throws IllegalArgumentException if name is null/empty or quantity is negative.
     */
    public InventoryItem(int id, String name, int quantity) {
        this(id, name, "General", 0.0, quantity, null);
    }

    /**
     * Constructs a new {@code InventoryItem} with explicitly specified status and default category/price.
     *
     * @param id       Unique identifier for the item. Must be greater than 0.
     * @param name     Name of the inventory product. Cannot be null or blank.
     * @param quantity Available stock quantity. Must be non-negative.
     * @param status   Explicit {@link StockStatus} override. If null, automatically determined.
     * @throws IllegalArgumentException if name is null/empty or quantity is negative.
     */
    public InventoryItem(int id, String name, int quantity, StockStatus status) {
        this(id, name, "General", 0.0, quantity, status);
    }

    /**
     * Constructs a new {@code InventoryItem} with category and price.
     *
     * @param id        Unique identifier for the item. Must be greater than 0.
     * @param name      Name of the inventory product. Cannot be null or blank.
     * @param category  Category or department tag.
     * @param unitPrice Price per unit. Must be non-negative.
     * @param quantity  Available stock quantity. Must be non-negative.
     * @throws IllegalArgumentException if name is null/empty, price is negative, or quantity is negative.
     */
    public InventoryItem(int id, String name, String category, double unitPrice, int quantity) {
        this(id, name, category, unitPrice, quantity, null);
    }

    /**
     * Full constructor for {@code InventoryItem}.
     *
     * @param id        Unique identifier for the item. Must be greater than 0.
     * @param name      Name of the inventory product. Cannot be null or blank.
     * @param category  Category or department tag.
     * @param unitPrice Price per unit. Must be non-negative.
     * @param quantity  Available stock quantity. Must be non-negative.
     * @param status    Explicit {@link StockStatus} override.
     * @throws IllegalArgumentException if validation constraints fail.
     */
    public InventoryItem(int id, String name, String category, double unitPrice, int quantity, StockStatus status) {
        validateName(name);
        validatePrice(unitPrice);
        validateQuantity(quantity);
        this.id = id;
        this.name = name.trim();
        this.category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();
        this.unitPrice = unitPrice;
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
     * Validates that the unit price is non-negative.
     *
     * @param price Price value to validate.
     * @throws IllegalArgumentException if price is less than 0.
     */
    public static void validatePrice(double price) {
        if (price < 0.0) {
            throw new IllegalArgumentException("Unit price cannot be negative: " + price);
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
        updateTimestamp();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();
        updateTimestamp();
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        validatePrice(unitPrice);
        this.unitPrice = unitPrice;
        updateTimestamp();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
        this.status = StockStatus.determineStatus(quantity);
        updateTimestamp();
    }

    /**
     * Calculates the total inventory value for this item (Unit Price * Quantity).
     *
     * @return Total value in currency units.
     */
    public double getTotalValue() {
        return unitPrice * quantity;
    }

    public StockStatus getStatus() {
        return status;
    }

    public void setStatus(StockStatus status) {
        this.status = (status != null) ? status : StockStatus.determineStatus(this.quantity);
        updateTimestamp();
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

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
        return String.format("Item #%d: %s [%s] | Price: $%.2f | Qty: %d | Total: $%.2f | Status: %s | Updated: %s",
                id, name, category, unitPrice, quantity, getTotalValue(), status.getDisplayName(), lastUpdated);
    }
}
