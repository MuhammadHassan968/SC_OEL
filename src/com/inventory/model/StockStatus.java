package com.inventory.model;

/**
 * Enumeration representing the stock status of an inventory item.
 * <p>
 * Status levels are classified as:
 * <ul>
 *   <li>{@link #IN_STOCK}: Quantity is greater than 5.</li>
 *   <li>{@link #LOW_STOCK}: Quantity is between 1 and 5 (inclusive).</li>
 *   <li>{@link #OUT_OF_STOCK}: Quantity is 0.</li>
 * </ul>
 *
 * @author Muhammad Hassan
 * @version 1.0
 */
public enum StockStatus {

    /** Indicates the item is sufficiently stocked (quantity &gt; 5). */
    IN_STOCK("In Stock"),

    /** Indicates the item has low stock and should be replenished (1 &lt;= quantity &lt;= 5). */
    LOW_STOCK("Low Stock"),

    /** Indicates the item is completely depleted (quantity == 0). */
    OUT_OF_STOCK("Out of Stock");

    /** Threshold boundary for low stock (items at or below this are Low Stock). */
    public static final int LOW_STOCK_THRESHOLD = 5;

    /** Human-readable display label for the stock status. */
    private final String displayName;

    /**
     * Constructs a StockStatus enum constant with the specified display name.
     *
     * @param displayName The formatted label for UI display.
     */
    StockStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display label.
     *
     * @return Formatted status string.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Automatically computes and returns the appropriate {@link StockStatus} based on item quantity.
     *
     * @param quantity The current stock quantity of the item.
     * @return {@link #OUT_OF_STOCK} if quantity is 0,
     *         {@link #LOW_STOCK} if quantity is between 1 and 5 (inclusive),
     *         {@link #IN_STOCK} if quantity is greater than 5.
     * @throws IllegalArgumentException if quantity is negative.
     */
    public static StockStatus determineStatus(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        if (quantity == 0) {
            return OUT_OF_STOCK;
        } else if (quantity <= LOW_STOCK_THRESHOLD) {
            return LOW_STOCK;
        } else {
            return IN_STOCK;
        }
    }

    /**
     * Returns the string representation of the stock status.
     *
     * @return Display name of the status.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
