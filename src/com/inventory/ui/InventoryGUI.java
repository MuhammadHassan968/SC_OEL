package com.inventory.ui;

import com.inventory.model.InventoryItem;
import com.inventory.model.StockStatus;
import com.inventory.service.InventoryManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;

/**
 * Graphical User Interface (GUI) for the Inventory Management System using Java Swing.
 * <p>
 * Provides interactive features:
 * <ul>
 *   <li>Product entry form with live input validation.</li>
 *   <li>Dynamic table displaying items with color-coded stock status badges.</li>
 *   <li>Add, View, Update, and Delete operations.</li>
 *   <li>Search by item name or ID and filtering by stock status.</li>
 *   <li>Real-time dashboard metrics (Total Items, Total Units, Low Stock, Out of Stock).</li>
 * </ul>
 *
 * @author Muhammad Hassan
 * @version 1.0
 */
public class InventoryGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Service controller for managing inventory repository. */
    private final InventoryManager manager;

    /** Text input field for Item ID. */
    private JTextField txtId;
    /** Text input field for Item Name. */
    private JTextField txtName;
    /** Text input field for Quantity. */
    private JTextField txtQuantity;
    /** Selection dropdown for Stock Status override. */
    private JComboBox<String> comboStatus;
    /** Text field for real-time search queries. */
    private JTextField txtSearch;
    /** Dropdown for status-based filtering. */
    private JComboBox<String> comboFilter;

    /** Metric label for Total Items. */
    private JLabel lblTotalItems;
    /** Metric label for Total Quantity Units. */
    private JLabel lblTotalUnits;
    /** Metric label for Low Stock count. */
    private JLabel lblLowStock;
    /** Metric label for Out of Stock count. */
    private JLabel lblOutOfStock;

    /** Main inventory display table. */
    private JTable itemTable;
    /** Table data model. */
    private DefaultTableModel tableModel;

    /** Currently selected item ID in the UI. */
    private int selectedItemId = -1;

    /**
     * Constructs and initializes the {@code InventoryGUI} window.
     *
     * @param manager The {@link InventoryManager} service instance.
     */
    public InventoryGUI(InventoryManager manager) {
        this.manager = manager;
        initUI();
        populateSampleData();
        refreshTable();
        updateDashboard();
    }

    /**
     * Sets up the window layout, themes, fonts, and sub-panels.
     */
    private void initUI() {
        setTitle("Store Inventory Management System - OEL Software Construction");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);

        // Main content container with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(14, 14, 14, 14));
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. Header Banner
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content Split: Form on Left/Top, Table on Center
        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setOpaque(false);

        // Left sidebar: Form & Actions
        contentPanel.add(createFormPanel(), BorderLayout.WEST);

        // Center: Search, Filters, and Table
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 3. Status Bar at Bottom
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Builds the top banner and quick dashboard statistics cards.
     *
     * @return Formatted header {@link JPanel}.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);

        // Title and subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("🏪 Store Inventory Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 41, 59));

        JLabel subLabel = new JLabel("Manage products, track stock status levels, and maintain store records");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(100, 116, 139));

        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Dashboard Metrics Cards
        JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        metricsPanel.setOpaque(false);

        lblTotalItems = createMetricCard("Total Items", "0", new Color(59, 130, 246));
        lblTotalUnits = createMetricCard("Total Units", "0", new Color(16, 185, 129));
        lblLowStock = createMetricCard("Low Stock", "0", new Color(245, 158, 11));
        lblOutOfStock = createMetricCard("Out of Stock", "0", new Color(239, 68, 68));

        metricsPanel.add(lblTotalItems.getParent());
        metricsPanel.add(lblTotalUnits.getParent());
        metricsPanel.add(lblLowStock.getParent());
        metricsPanel.add(lblOutOfStock.getParent());

        headerPanel.add(metricsPanel, BorderLayout.EAST);
        return headerPanel;
    }

    /**
     * Helper to create a styled dashboard metric card.
     *
     * @param title Title label for the card.
     * @param value Initial string value.
     * @param accent Accent border/text color.
     * @return The value {@link JLabel} to allow dynamic updates.
     */
    private JLabel createMetricCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new GridLayout(2, 1, 2, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(100, 116, 139));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLbl.setForeground(accent);

        card.add(titleLbl);
        card.add(valLbl);
        return valLbl;
    }

    /**
     * Builds the item management form panel with fields and action buttons.
     *
     * @return Formatted form {@link JPanel}.
     */
    private JPanel createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout(8, 8));
        formContainer.setPreferredSize(new Dimension(320, 0));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Form Fields Area
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        JLabel formHeading = new JLabel("Product Details");
        formHeading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formHeading.setForeground(new Color(30, 41, 59));
        formHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(formHeading);
        fieldsPanel.add(Box.createVerticalStrut(12));

        // ID (Read-only/auto-assigned)
        fieldsPanel.add(createFieldLabel("Item ID (Auto-Generated / Read-Only):"));
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));
        txtId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtId.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(txtId);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Name
        fieldsPanel.add(createFieldLabel("Item Name: *"));
        txtName = new JTextField();
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(txtName);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Quantity
        fieldsPanel.add(createFieldLabel("Quantity: * (0 or greater)"));
        txtQuantity = new JTextField();
        txtQuantity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtQuantity.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(txtQuantity);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Status Override / Auto
        fieldsPanel.add(createFieldLabel("Stock Status (Auto / Override):"));
        comboStatus = new JComboBox<>(new String[]{"Auto (Calculated from Quantity)", "In Stock", "Low Stock", "Out of Stock"});
        comboStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        comboStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(comboStatus);
        fieldsPanel.add(Box.createVerticalStrut(14));

        // Status Legend helper note
        JLabel noteLabel = new JLabel("<html><small style='color:#64748b;'>"
                + "<b>Status Rules:</b><br>"
                + "• Quantity = 0 &rarr; <font color='#dc3545'>Out of Stock</font><br>"
                + "• Quantity 1 - 5 &rarr; <font color='#d97706'>Low Stock</font><br>"
                + "• Quantity &gt; 5 &rarr; <font color='#16a34a'>In Stock</font>"
                + "</small></html>");
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(noteLabel);

        formContainer.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons Area
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnAdd = createStyledButton("➕ Add Item", new Color(16, 185, 129), Color.WHITE);
        JButton btnUpdate = createStyledButton("✏️ Update Selected", new Color(59, 130, 246), Color.WHITE);
        JButton btnDelete = createStyledButton("🗑️ Delete Selected", new Color(239, 68, 68), Color.WHITE);
        JButton btnClear = createStyledButton("🧹 Clear Form", new Color(148, 163, 184), Color.WHITE);

        btnAdd.addActionListener(e -> handleAddItem());
        btnUpdate.addActionListener(e -> handleUpdateItem());
        btnDelete.addActionListener(e -> handleDeleteItem());
        btnClear.addActionListener(e -> clearForm());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        formContainer.add(buttonsPanel, BorderLayout.SOUTH);
        return formContainer;
    }

    /**
     * Builds the right-hand panel containing search, filtering, and the inventory table.
     *
     * @return Formatted table container {@link JPanel}.
     */
    private JPanel createTablePanel() {
        JPanel tableContainer = new JPanel(new BorderLayout(8, 8));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // Top Toolbar: Search & Filter
        JPanel toolbar = new JPanel(new BorderLayout(8, 8));
        toolbar.setOpaque(false);

        // Search Box
        JPanel searchBox = new JPanel(new BorderLayout(6, 6));
        searchBox.setOpaque(false);
        JLabel lblSearchIcon = new JLabel("🔍 Search:");
        lblSearchIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSearch = new JTextField();
        txtSearch.setToolTipText("Type item name or ID to filter in real-time...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applySearchAndFilter();
            }
        });
        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);
        toolbar.add(searchBox, BorderLayout.CENTER);

        // Filter Dropdown & Refresh
        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterBox.setOpaque(false);
        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));

        comboFilter = new JComboBox<>(new String[]{"All Items", "In Stock", "Low Stock", "Out of Stock"});
        comboFilter.addActionListener(e -> applySearchAndFilter());

        JButton btnRefresh = new JButton("🔄 View All");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            comboFilter.setSelectedIndex(0);
            refreshTable();
            if (manager.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "The inventory is currently empty. Use the left form to add your first product.",
                        "Inventory Empty", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        filterBox.add(lblFilter);
        filterBox.add(comboFilter);
        filterBox.add(btnRefresh);
        toolbar.add(filterBox, BorderLayout.EAST);

        tableContainer.add(toolbar, BorderLayout.NORTH);

        // JTable Configuration
        String[] columnNames = {"ID", "Item Name", "Quantity", "Stock Status", "Last Modified"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table cells are read-only; use form to edit
            }
        };

        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(28);
        itemTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setGridColor(new Color(241, 245, 249));

        // Header Styling
        JTableHeader header = itemTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(51, 65, 85));
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        // Center align ID and Quantity columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        itemTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        itemTable.getColumnModel().getColumn(0).setMaxWidth(80);
        itemTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        itemTable.getColumnModel().getColumn(2).setMaxWidth(100);

        // Custom Cell Renderer for Stock Status Badges
        itemTable.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());

        // Row Selection Listener: Populate form when row is selected
        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = itemTable.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromSelectedRow(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        return tableContainer;
    }

    /**
     * Builds the bottom status bar displaying application mode and tips.
     *
     * @return Status bar {@link JPanel}.
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        statusBar.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel lblStatus = new JLabel("Ready | Select any row in the table to edit or delete.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(100, 116, 139));

        JLabel lblVersion = new JLabel("Java Swing • JDK 21 • Modern Software Construction");
        lblVersion.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblVersion.setForeground(new Color(148, 163, 184));

        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblVersion, BorderLayout.EAST);
        return statusBar;
    }

    /**
     * Creates a styled form label.
     */
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(51, 65, 85));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Helper to create custom styled buttons.
     */
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Populates initial sample items to showcase inventory tracking on startup.
     */
    private void populateSampleData() {
        manager.addItem("Logitech MX Master 3S", 12);
        manager.addItem("Mechanical Keyboard TKL", 4);
        manager.addItem("USB-C Fast Charging Cable", 28);
        manager.addItem("Dell UltraSharp 27\" Monitor", 2);
        manager.addItem("Ergonomic Desk Chair Pad", 0);
    }

    /**
     * Refreshes the table view with all items from the manager.
     */
    public void refreshTable() {
        renderItems(manager.getAllItems());
    }

    /**
     * Renders a specific list of items into the table model.
     *
     * @param items List of inventory items to display.
     */
    private void renderItems(List<InventoryItem> items) {
        tableModel.setRowCount(0);
        for (InventoryItem item : items) {
            tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getStatus(),
                    item.getLastUpdated()
            });
        }
    }

    /**
     * Updates top dashboard metrics.
     */
    public void updateDashboard() {
        lblTotalItems.setText(String.valueOf(manager.getItemCount()));
        lblTotalUnits.setText(String.valueOf(manager.getTotalUnits()));
        lblLowStock.setText(String.valueOf(manager.getLowStockCount()));
        lblOutOfStock.setText(String.valueOf(manager.getOutOfStockCount()));
    }

    /**
     * Applies search and status filtering based on input controls.
     */
    private void applySearchAndFilter() {
        String query = txtSearch.getText().trim();
        String selectedFilter = (String) comboFilter.getSelectedItem();

        List<InventoryItem> filtered = manager.searchByName(query);

        if (selectedFilter != null && !selectedFilter.equals("All Items")) {
            StockStatus statusFilter = null;
            if (selectedFilter.equals("In Stock")) statusFilter = StockStatus.IN_STOCK;
            else if (selectedFilter.equals("Low Stock")) statusFilter = StockStatus.LOW_STOCK;
            else if (selectedFilter.equals("Out of Stock")) statusFilter = StockStatus.OUT_OF_STOCK;

            final StockStatus target = statusFilter;
            filtered = filtered.stream().filter(i -> i.getStatus() == target).toList();
        }

        renderItems(filtered);
    }

    /**
     * Handles adding a new item from form inputs with validation.
     */
    private void handleAddItem() {
        String name = txtName.getText();
        String qtyText = txtQuantity.getText();

        // 1. Validate empty name
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Item name cannot be empty.",
                    "Input Validation Error", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        // 2. Validate quantity (must be numeric and non-negative)
        int quantity;
        try {
            quantity = Integer.parseInt(qtyText.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Quantity must be a valid integer number.",
                    "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            txtQuantity.requestFocus();
            return;
        }

        if (quantity < 0) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Quantity cannot be negative (" + quantity + ").",
                    "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            txtQuantity.requestFocus();
            return;
        }

        // 3. Check status override
        StockStatus status = null;
        int statusIndex = comboStatus.getSelectedIndex();
        if (statusIndex == 1) status = StockStatus.IN_STOCK;
        else if (statusIndex == 2) status = StockStatus.LOW_STOCK;
        else if (statusIndex == 3) status = StockStatus.OUT_OF_STOCK;

        // 4. Perform addition
        InventoryItem newItem;
        if (status == null) {
            newItem = manager.addItem(name.trim(), quantity);
        } else {
            newItem = new InventoryItem(0, name.trim(), quantity, status);
            manager.addItem(newItem);
        }

        JOptionPane.showMessageDialog(this,
                "Success! Item \"" + newItem.getName() + "\" added with ID #" + newItem.getId() + " (" + newItem.getStatus().getDisplayName() + ").",
                "Item Added", JOptionPane.INFORMATION_MESSAGE);

        clearForm();
        refreshTable();
        updateDashboard();
    }

    /**
     * Handles updating an existing selected item.
     */
    private void handleUpdateItem() {
        if (selectedItemId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item from the table or provide an ID to update.",
                    "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText();
        String qtyText = txtQuantity.getText();

        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Item name cannot be empty.",
                    "Input Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyText.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Quantity must be a valid integer number.",
                    "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (quantity < 0) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Quantity cannot be negative.",
                    "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StockStatus status = null;
        int statusIndex = comboStatus.getSelectedIndex();
        if (statusIndex == 1) status = StockStatus.IN_STOCK;
        else if (statusIndex == 2) status = StockStatus.LOW_STOCK;
        else if (statusIndex == 3) status = StockStatus.OUT_OF_STOCK;

        boolean updated;
        if (status == null) {
            updated = manager.updateItem(selectedItemId, name.trim(), quantity);
        } else {
            updated = manager.updateItem(selectedItemId, name.trim(), quantity, status);
        }

        if (updated) {
            JOptionPane.showMessageDialog(this,
                    "Item #" + selectedItemId + " successfully updated!",
                    "Item Updated", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            updateDashboard();
        } else {
            // Handled gracefully as required
            JOptionPane.showMessageDialog(this,
                    "Item with ID #" + selectedItemId + " does not exist in the inventory.",
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles deleting the selected item.
     */
    private void handleDeleteItem() {
        if (selectedItemId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item from the table to delete.",
                    "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Optional<InventoryItem> itemOpt = manager.findById(selectedItemId);
        String itemName = itemOpt.map(InventoryItem::getName).orElse("Selected Item");

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete item #" + selectedItemId + " (\"" + itemName + "\")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = manager.deleteItem(selectedItemId);
            if (deleted) {
                JOptionPane.showMessageDialog(this,
                        "Item #" + selectedItemId + " removed successfully.",
                        "Item Deleted", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();
                updateDashboard();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete item. It may have already been removed.",
                        "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Populates the input form from the selected table row.
     */
    private void populateFormFromSelectedRow(int row) {
        selectedItemId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int quantity = (int) tableModel.getValueAt(row, 2);
        StockStatus status = (StockStatus) tableModel.getValueAt(row, 3);

        txtId.setText(String.valueOf(selectedItemId));
        txtName.setText(name);
        txtQuantity.setText(String.valueOf(quantity));

        if (status == StockStatus.IN_STOCK) {
            comboStatus.setSelectedIndex(1);
        } else if (status == StockStatus.LOW_STOCK) {
            comboStatus.setSelectedIndex(2);
        } else if (status == StockStatus.OUT_OF_STOCK) {
            comboStatus.setSelectedIndex(3);
        }
    }

    /**
     * Clears all form fields and resets selection tracking.
     */
    private void clearForm() {
        selectedItemId = -1;
        txtId.setText("");
        txtName.setText("");
        txtQuantity.setText("");
        comboStatus.setSelectedIndex(0);
        itemTable.clearSelection();
    }

    /**
     * Custom cell renderer for displaying colored badges for StockStatus.
     */
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            if (value instanceof StockStatus) {
                StockStatus status = (StockStatus) value;
                label.setText("  " + status.getDisplayName() + "  ");

                if (!isSelected) {
                    switch (status) {
                        case IN_STOCK:
                            label.setForeground(new Color(22, 101, 52));
                            label.setBackground(new Color(220, 252, 231));
                            break;
                        case LOW_STOCK:
                            label.setForeground(new Color(146, 64, 14));
                            label.setBackground(new Color(254, 243, 199));
                            break;
                        case OUT_OF_STOCK:
                            label.setForeground(new Color(153, 27, 27));
                            label.setBackground(new Color(254, 226, 226));
                            break;
                    }
                    label.setOpaque(true);
                } else {
                    label.setOpaque(true);
                }
            }
            return label;
        }
    }
}
