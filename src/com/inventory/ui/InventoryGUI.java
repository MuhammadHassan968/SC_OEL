package com.inventory.ui;

import com.inventory.model.InventoryItem;
import com.inventory.model.StockStatus;
import com.inventory.service.InventoryManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Modern Light SaaS Graphical User Interface (GUI) for the Inventory Management System using Java Swing.
 * Supports:
 * - 5 KPI Cards (including Total Net Worth)
 * - Category / Tag management and filtering
 * - Item unit pricing and total value calculations
 * - 1-Click CSV Inventory Report Export
 *
 * @author Muhammad Hassan
 * @version 2.1
 */
public class InventoryGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Primary color palette constants */
    private static final Color CANVAS_BG = new Color(0xF1, 0xF5, 0xF9);       // #F1F5F9
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xE2, 0xE8, 0xF0);    // #E2E8F0
    private static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);    // #0F172A
    private static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);      // #64748B
    private static final Color FOCUS_COLOR = new Color(0x3B, 0x82, 0xF6);     // #3B82F6
    private static final Color SELECTION_BG = new Color(0xEF, 0xF6, 0xFF);    // #EFF6FF
    private static final Color DIVIDER_COLOR = new Color(0xF1, 0xF5, 0xF9);   // #F1F5F9

    private static final Color COLOR_TOTAL_ITEMS = new Color(0x3B, 0x82, 0xF6); // #3B82F6 Blue
    private static final Color COLOR_TOTAL_UNITS = new Color(0x10, 0xB9, 0x81); // #10B981 Green
    private static final Color COLOR_NET_WORTH   = new Color(0x8B, 0x5C, 0xF6); // #8B5CF6 Purple
    private static final Color COLOR_LOW_STOCK   = new Color(0xF5, 0x9E, 0x0B); // #F59E0B Amber
    private static final Color COLOR_OUT_STOCK   = new Color(0xEF, 0x44, 0x44); // #EF4444 Red

    private static final String[] CATEGORY_OPTIONS = {
            "Electronics", "Accessories", "Office Supplies", "Groceries", "Hardware", "General"
    };

    private final InventoryManager manager;

    // Form inputs
    private JTextField txtId;
    private JTextField txtName;
    private JComboBox<String> comboCategory;
    private JTextField txtPrice;
    private JTextField txtQuantity;
    private JComboBox<String> comboStatus;

    // Search and Filters
    private JTextField txtSearch;
    private JComboBox<String> comboCategoryFilter;
    private JComboBox<String> comboStatusFilter;

    // Dashboard Metric Labels
    private JLabel lblTotalItems;
    private JLabel lblTotalUnits;
    private JLabel lblNetWorth;
    private JLabel lblLowStock;
    private JLabel lblOutOfStock;

    // Table
    private JTable itemTable;
    private DefaultTableModel tableModel;

    private int selectedItemId = -1;

    public InventoryGUI(InventoryManager manager) {
        this.manager = manager;
        initUI();
        populateSampleData();
        refreshTable();
        updateDashboard();
    }

    private void initUI() {
        setTitle("HR Traders - Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(14, 14));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(CANVAS_BG);

        // Header Panel with title and 5 clickable KPI metric cards
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Split: Form on Left, Table on Center
        JPanel contentPanel = new JPanel(new BorderLayout(14, 14));
        contentPanel.setOpaque(false);
        contentPanel.add(createFormPanel(), BorderLayout.WEST);
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerCard = new RoundedPanel(12, CARD_BG, BORDER_COLOR);
        headerCard.setLayout(new BorderLayout(14, 14));
        headerCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Title and subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("HR Traders");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Inventory Management System | Real-time stock tracking & valuation analytics");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);
        headerCard.add(titlePanel, BorderLayout.WEST);

        // 5 KPI Cards in a FlowLayout
        JPanel kpiContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        kpiContainer.setOpaque(false);

        RoundedPanel cardTotalItems = createClickableKpiCard("Total Items", "0", COLOR_TOTAL_ITEMS, "ALL_STATUS", null);
        RoundedPanel cardTotalUnits = createClickableKpiCard("Total Units", "0", COLOR_TOTAL_UNITS, "ALL_STATUS", null);
        RoundedPanel cardNetWorth   = createClickableKpiCard("Net Worth", "$0.00", COLOR_NET_WORTH, "ALL_STATUS", null);
        RoundedPanel cardLowStock   = createClickableKpiCard("Low Stock", "0", COLOR_LOW_STOCK, "Low Stock", null);
        RoundedPanel cardOutOfStock = createClickableKpiCard("Out of Stock", "0", COLOR_OUT_STOCK, "Out of Stock", null);

        lblTotalItems = (JLabel) cardTotalItems.getClientProperty("valueLabel");
        lblTotalUnits = (JLabel) cardTotalUnits.getClientProperty("valueLabel");
        lblNetWorth   = (JLabel) cardNetWorth.getClientProperty("valueLabel");
        lblLowStock   = (JLabel) cardLowStock.getClientProperty("valueLabel");
        lblOutOfStock = (JLabel) cardOutOfStock.getClientProperty("valueLabel");

        kpiContainer.add(cardTotalItems);
        kpiContainer.add(cardTotalUnits);
        kpiContainer.add(cardNetWorth);
        kpiContainer.add(cardLowStock);
        kpiContainer.add(cardOutOfStock);

        headerCard.add(kpiContainer, BorderLayout.EAST);
        return headerCard;
    }

    private RoundedPanel createClickableKpiCard(String title, String value, Color accentColor, String targetStatus, String targetCat) {
        RoundedPanel card = new RoundedPanel(12, CARD_BG, BORDER_COLOR, accentColor, 4);
        card.setLayout(new GridLayout(2, 1, 2, 2));
        card.setBorder(new EmptyBorder(8, 14, 8, 14));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setToolTipText("Click to filter table");

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valLbl.setForeground(accentColor);

        card.add(titleLbl);
        card.add(valLbl);
        card.putClientProperty("valueLabel", valLbl);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (targetStatus != null && comboStatusFilter != null) {
                    if ("ALL_STATUS".equals(targetStatus)) {
                        comboStatusFilter.setSelectedIndex(0);
                    } else {
                        comboStatusFilter.setSelectedItem(targetStatus);
                    }
                    applySearchAndFilter();
                }
            }
        });

        return card;
    }

    private JPanel createFormPanel() {
        RoundedPanel formContainer = new RoundedPanel(12, CARD_BG, BORDER_COLOR);
        formContainer.setLayout(new BorderLayout(10, 10));
        formContainer.setPreferredSize(new Dimension(330, 0));
        formContainer.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        JLabel formHeading = new JLabel("Product Details");
        formHeading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        formHeading.setForeground(TEXT_PRIMARY);
        formHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(formHeading);
        fieldsPanel.add(Box.createVerticalStrut(12));

        // ID (Read-only)
        fieldsPanel.add(createFieldLabel("Item ID (Auto-Generated):"));
        txtId = createStyledTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(0xF8, 0xFA, 0xFC));
        txtId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtId.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(txtId);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Name
        fieldsPanel.add(createFieldLabel("Item Name: *"));
        txtName = createStyledTextField();
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(txtName);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Category / Tag
        fieldsPanel.add(createFieldLabel("Category / Department: *"));
        comboCategory = new JComboBox<>(CATEGORY_OPTIONS);
        comboCategory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboCategory.setBackground(Color.WHITE);
        comboCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        comboCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(comboCategory);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Unit Price ($)
        fieldsPanel.add(createFieldLabel("Unit Price ($): * (e.g. 19.99)"));
        txtPrice = createStyledTextField();
        txtPrice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtPrice.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(txtPrice);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Quantity
        fieldsPanel.add(createFieldLabel("Quantity: * (0 or greater)"));
        txtQuantity = createStyledTextField();
        txtQuantity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtQuantity.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(txtQuantity);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Status Override / Auto
        fieldsPanel.add(createFieldLabel("Stock Status:"));
        comboStatus = new JComboBox<>(new String[]{"Auto (Calculated from Quantity)", "In Stock", "Low Stock", "Out of Stock"});
        comboStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboStatus.setBackground(Color.WHITE);
        comboStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        comboStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(comboStatus);
        fieldsPanel.add(Box.createVerticalStrut(10));

        JLabel noteLabel = new JLabel("<html><small style='color:#64748b; font-family: Segoe UI;'>"
                + "<b>Classification:</b><br>"
                + "&bull; Qty = 0 &rarr; <font color='#991B1B'>Out of Stock</font><br>"
                + "&bull; Qty 1 - 5 &rarr; <font color='#92400E'>Low Stock</font><br>"
                + "&bull; Qty &gt; 5 &rarr; <font color='#166534'>In Stock</font>"
                + "</small></html>");
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(noteLabel);

        formContainer.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons in 2x2 grid with exact solid colors and high-contrast visible text
        JPanel buttonsGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsGrid.setOpaque(false);
        buttonsGrid.setBorder(new EmptyBorder(10, 0, 0, 0));

        RoundedButton btnAdd = new RoundedButton("Add Item", new Color(0x16, 0xA3, 0x4A), Color.WHITE, 8);
        RoundedButton btnUpdate = new RoundedButton("Update Item", new Color(0x25, 0x63, 0xEB), Color.WHITE, 8);
        RoundedButton btnDelete = new RoundedButton("Delete Item", new Color(0xDC, 0x26, 0x26), Color.WHITE, 8);
        RoundedButton btnClear = new RoundedButton("Clear Form", new Color(0xE2, 0xE8, 0xF0), new Color(0x0F, 0x17, 0x2A), 8);

        btnAdd.addActionListener(e -> handleAddItem());
        btnUpdate.addActionListener(e -> handleUpdateItem());
        btnDelete.addActionListener(e -> handleDeleteItem());
        btnClear.addActionListener(e -> clearForm());

        buttonsGrid.add(btnAdd);
        buttonsGrid.add(btnUpdate);
        buttonsGrid.add(btnDelete);
        buttonsGrid.add(btnClear);

        formContainer.add(buttonsGrid, BorderLayout.SOUTH);
        return formContainer;
    }

    private JPanel createTablePanel() {
        RoundedPanel tableContainer = new RoundedPanel(12, CARD_BG, BORDER_COLOR);
        tableContainer.setLayout(new BorderLayout(12, 12));
        tableContainer.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Toolbar: Search, Category Filter, Status Filter, Export CSV, and View All
        JPanel toolbar = new JPanel(new BorderLayout(8, 8));
        toolbar.setOpaque(false);

        // Search Box
        JPanel searchBox = new JPanel(new BorderLayout(6, 6));
        searchBox.setOpaque(false);
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearch.setForeground(TEXT_PRIMARY);

        txtSearch = createStyledTextField();
        txtSearch.setToolTipText("Filter by name, ID, or category...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applySearchAndFilter();
            }
        });
        searchBox.add(lblSearch, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);
        toolbar.add(searchBox, BorderLayout.CENTER);

        // Right Toolbar Filters & Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionsPanel.setOpaque(false);

        // Category Filter
        JLabel lblCatFilter = new JLabel("Category:");
        lblCatFilter.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCatFilter.setForeground(TEXT_PRIMARY);

        String[] catFilterItems = new String[CATEGORY_OPTIONS.length + 1];
        catFilterItems[0] = "All Categories";
        System.arraycopy(CATEGORY_OPTIONS, 0, catFilterItems, 1, CATEGORY_OPTIONS.length);
        comboCategoryFilter = new JComboBox<>(catFilterItems);
        comboCategoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboCategoryFilter.setBackground(Color.WHITE);
        comboCategoryFilter.addActionListener(e -> applySearchAndFilter());

        // Status Filter
        JLabel lblStatFilter = new JLabel("Status:");
        lblStatFilter.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatFilter.setForeground(TEXT_PRIMARY);

        comboStatusFilter = new JComboBox<>(new String[]{"All Statuses", "In Stock", "Low Stock", "Out of Stock"});
        comboStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboStatusFilter.setBackground(Color.WHITE);
        comboStatusFilter.addActionListener(e -> applySearchAndFilter());

        // View All Button
        RoundedButton btnRefresh = new RoundedButton("View All", new Color(0xF1, 0xF5, 0xF9), TEXT_PRIMARY, 6);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            comboCategoryFilter.setSelectedIndex(0);
            comboStatusFilter.setSelectedIndex(0);
            refreshTable();
            if (manager.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "The inventory is currently empty. Use the left form to add your first product.",
                        "Inventory Empty", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // 1-Click Export CSV Button
        RoundedButton btnExport = new RoundedButton("Export CSV", new Color(0x05, 0x96, 0x69), Color.WHITE, 6);
        btnExport.setToolTipText("Export inventory report as CSV for Excel / printing");
        btnExport.addActionListener(e -> handleExportCSV());

        actionsPanel.add(lblCatFilter);
        actionsPanel.add(comboCategoryFilter);
        actionsPanel.add(lblStatFilter);
        actionsPanel.add(comboStatusFilter);
        actionsPanel.add(btnRefresh);
        actionsPanel.add(btnExport);

        toolbar.add(actionsPanel, BorderLayout.EAST);
        tableContainer.add(toolbar, BorderLayout.NORTH);

        // JTable Configuration with 8 columns (ID, Name, Category, Price, Qty, Total, Status, Modified)
        String[] columnNames = {"ID", "Item Name", "Category", "Unit Price", "Quantity", "Total Value", "Stock Status", "Last Modified"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(42);
        itemTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemTable.setForeground(TEXT_PRIMARY);
        itemTable.setBackground(Color.WHITE);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Horizontal dividers only (#F1F5F9)
        itemTable.setShowGrid(false);
        itemTable.setShowHorizontalLines(true);
        itemTable.setShowVerticalLines(false);
        itemTable.setGridColor(DIVIDER_COLOR);

        // Soft blue row selection (#EFF6FF with #0F172A text)
        itemTable.setSelectionBackground(SELECTION_BG);
        itemTable.setSelectionForeground(TEXT_PRIMARY);

        // Header Styling: #F8FAFC bold header
        JTableHeader header = itemTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(0xF8, 0xFA, 0xFC));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Column renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        rightRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        itemTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        itemTable.getColumnModel().getColumn(0).setMaxWidth(70);
        itemTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);   // Name
        itemTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Category
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        itemTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);  // Unit Price
        itemTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        itemTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Quantity
        itemTable.getColumnModel().getColumn(4).setMaxWidth(80);
        itemTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);  // Total Value
        itemTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        itemTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer()); // Status
        itemTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        itemTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Last Modified

        // Row Selection Listener
        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = itemTable.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromSelectedRow(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE); // Eliminate gray background

        tableContainer.add(scrollPane, BorderLayout.CENTER);
        return tableContainer;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        statusBar.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel lblStatus = new JLabel("Ready | Select any row to edit or delete. Click KPI cards to filter.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(TEXT_MUTED);

        JLabel lblDesigner = new JLabel("Designed by Muhammad Hassan");
        lblDesigner.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDesigner.setForeground(TEXT_PRIMARY);

        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblDesigner, BorderLayout.EAST);
        return statusBar;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setCaretColor(TEXT_PRIMARY);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.isEditable()) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(FOCUS_COLOR, 1, true),
                            new EmptyBorder(6, 8, 6, 8)
                    ));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(6, 8, 6, 8)
                ));
            }
        });

        return field;
    }

    private void populateSampleData() {
        manager.addItem(new InventoryItem(0, "Apple MacBook Pro 16\" M3 Max", "Electronics", 3499.00, 6));
        manager.addItem(new InventoryItem(0, "Dell XPS 15 OLED Laptop", "Electronics", 1899.00, 4));
        manager.addItem(new InventoryItem(0, "ASUS ROG Zephyrus G14", "Electronics", 1599.99, 3));
        manager.addItem(new InventoryItem(0, "NVIDIA GeForce RTX 4090 24GB", "Electronics", 1799.00, 2));
        manager.addItem(new InventoryItem(0, "AMD Ryzen 9 7950X Processor", "Electronics", 549.00, 8));
        manager.addItem(new InventoryItem(0, "Intel Core i9-14900K Desktop CPU", "Electronics", 589.00, 5));
        manager.addItem(new InventoryItem(0, "Samsung 990 PRO 2TB NVMe SSD", "Electronics", 179.99, 18));
        manager.addItem(new InventoryItem(0, "Corsair Vengeance 32GB DDR5 RAM", "Electronics", 119.99, 14));
        manager.addItem(new InventoryItem(0, "Dell UltraSharp 27\" 4K Monitor", "Electronics", 449.00, 7));
        manager.addItem(new InventoryItem(0, "LG UltraGear 34\" Curved Gaming Monitor", "Electronics", 799.99, 3));
        manager.addItem(new InventoryItem(0, "Sony WH-1000XM5 Wireless Headphones", "Electronics", 398.00, 9));
        manager.addItem(new InventoryItem(0, "Apple iPad Air 11\" M2 128GB", "Electronics", 599.00, 11));
        manager.addItem(new InventoryItem(0, "Samsung Galaxy Tab S9 Ultra", "Electronics", 1199.99, 2));
        manager.addItem(new InventoryItem(0, "Logitech MX Master 3S Wireless Mouse", "Accessories", 99.99, 24));
        manager.addItem(new InventoryItem(0, "Keychron Q1 Pro Mechanical Keyboard", "Accessories", 199.00, 8));
        manager.addItem(new InventoryItem(0, "Logitech Brio 4K Ultra HD Webcam", "Electronics", 199.99, 12));
        manager.addItem(new InventoryItem(0, "Shure SM7B Cardioid Dynamic Mic", "Electronics", 399.00, 4));
        manager.addItem(new InventoryItem(0, "Elgato Stream Deck MK.2", "Accessories", 149.99, 6));
        manager.addItem(new InventoryItem(0, "ASUS ROG Rapture GT-AXE16000 Router", "Electronics", 599.99, 3));
        manager.addItem(new InventoryItem(0, "Anker 737 Power Bank 24,000mAh", "Electronics", 149.99, 15));
        manager.addItem(new InventoryItem(0, "Belkin MagSafe 3-in-1 Wireless Charger", "Accessories", 129.99, 10));
        manager.addItem(new InventoryItem(0, "Seagate IronWolf 8TB NAS HDD", "Electronics", 199.99, 5));
        manager.addItem(new InventoryItem(0, "Corsair RM1000x 1000W Power Supply", "Electronics", 189.99, 7));
        manager.addItem(new InventoryItem(0, "NZXT Kraken Elite 360 RGB Liquid Cooler", "Electronics", 279.99, 4));
        manager.addItem(new InventoryItem(0, "Apple Watch Ultra 2 GPS + Cellular", "Electronics", 799.00, 6));
        manager.addItem(new InventoryItem(0, "Samsung Galaxy S24 Ultra 512GB", "Electronics", 1299.99, 8));
        manager.addItem(new InventoryItem(0, "Sony PlayStation 5 Slim Digital", "Electronics", 449.99, 0));
        manager.addItem(new InventoryItem(0, "Nintendo Switch OLED Model", "Electronics", 349.99, 0));
        manager.addItem(new InventoryItem(0, "Meta Quest 3 128GB VR Headset", "Electronics", 499.99, 5));
        manager.addItem(new InventoryItem(0, "Anker USB-C 100W Fast Charging Cable", "Accessories", 19.99, 35));
    }

    public void refreshTable() {
        renderItems(manager.getAllItems());
    }

    private void renderItems(List<InventoryItem> items) {
        tableModel.setRowCount(0);
        for (InventoryItem item : items) {
            tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    String.format("$%.2f", item.getUnitPrice()),
                    item.getQuantity(),
                    String.format("$%.2f", item.getTotalValue()),
                    item.getStatus(),
                    item.getLastUpdated()
            });
        }
    }

    public void updateDashboard() {
        lblTotalItems.setText(String.valueOf(manager.getItemCount()));
        lblTotalUnits.setText(String.valueOf(manager.getTotalUnits()));
        lblNetWorth.setText(String.format("$%,.2f", manager.getTotalInventoryValue()));
        lblLowStock.setText(String.valueOf(manager.getLowStockCount()));
        lblOutOfStock.setText(String.valueOf(manager.getOutOfStockCount()));
    }

    private void applySearchAndFilter() {
        String query = txtSearch.getText().trim();
        String selectedCat = (String) comboCategoryFilter.getSelectedItem();
        String selectedStatus = (String) comboStatusFilter.getSelectedItem();

        List<InventoryItem> filtered = manager.searchByName(query);

        if (selectedCat != null && !selectedCat.equals("All Categories")) {
            filtered = filtered.stream()
                    .filter(i -> i.getCategory().equalsIgnoreCase(selectedCat))
                    .toList();
        }

        if (selectedStatus != null && !selectedStatus.equals("All Statuses")) {
            StockStatus target = null;
            if (selectedStatus.equals("In Stock")) target = StockStatus.IN_STOCK;
            else if (selectedStatus.equals("Low Stock")) target = StockStatus.LOW_STOCK;
            else if (selectedStatus.equals("Out of Stock")) target = StockStatus.OUT_OF_STOCK;

            if (target != null) {
                final StockStatus fTarget = target;
                filtered = filtered.stream().filter(i -> i.getStatus() == fTarget).toList();
            }
        }

        renderItems(filtered);
    }

    private void handleAddItem() {
        String name = txtName.getText();
        String category = (String) comboCategory.getSelectedItem();
        String priceText = txtPrice.getText();
        String qtyText = txtQuantity.getText();

        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Item name cannot be empty.",
                    "Input Validation Error", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText.trim().replace("$", ""));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Unit price must be a valid number (e.g. 19.99).",
                    "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPrice.requestFocus();
            return;
        }

        if (price < 0.0) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Unit price cannot be negative.",
                    "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPrice.requestFocus();
            return;
        }

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

        StockStatus status = null;
        int statusIndex = comboStatus.getSelectedIndex();
        if (statusIndex == 1) status = StockStatus.IN_STOCK;
        else if (statusIndex == 2) status = StockStatus.LOW_STOCK;
        else if (statusIndex == 3) status = StockStatus.OUT_OF_STOCK;

        InventoryItem newItem = new InventoryItem(0, name.trim(), category, price, quantity, status);
        manager.addItem(newItem);

        JOptionPane.showMessageDialog(this,
                "Success! Item \"" + newItem.getName() + "\" added with ID #" + newItem.getId() + " (" + newItem.getStatus().getDisplayName() + ").",
                "Item Added", JOptionPane.INFORMATION_MESSAGE);

        clearForm();
        refreshTable();
        updateDashboard();
    }

    private void handleUpdateItem() {
        if (selectedItemId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item from the table or provide an ID to update.",
                    "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText();
        String category = (String) comboCategory.getSelectedItem();
        String priceText = txtPrice.getText();
        String qtyText = txtQuantity.getText();

        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Item name cannot be empty.",
                    "Input Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText.trim().replace("$", ""));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Unit price must be a valid number.",
                    "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (price < 0.0) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: Unit price cannot be negative.",
                    "Input Validation Error", JOptionPane.ERROR_MESSAGE);
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

        boolean updated = manager.updateItem(selectedItemId, name.trim(), category, price, quantity, status);

        if (updated) {
            JOptionPane.showMessageDialog(this,
                    "Item #" + selectedItemId + " successfully updated!",
                    "Item Updated", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            updateDashboard();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Item with ID #" + selectedItemId + " does not exist in the inventory.",
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

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

    private void handleExportCSV() {
        if (manager.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot export an empty inventory. Please add items first.",
                    "Export Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Inventory Report (CSV)");
        fileChooser.setSelectedFile(new File("inventory_report.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Comma-Separated Values (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try {
                manager.exportToCSV(fileToSave);
                JOptionPane.showMessageDialog(this,
                        "Report exported successfully!\nLocation: " + fileToSave.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting report: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateFormFromSelectedRow(int row) {
        selectedItemId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String category = (String) tableModel.getValueAt(row, 2);
        String priceStr = ((String) tableModel.getValueAt(row, 3)).replace("$", "").trim();
        int quantity = (int) tableModel.getValueAt(row, 4);
        StockStatus status = (StockStatus) tableModel.getValueAt(row, 6);

        txtId.setText(String.valueOf(selectedItemId));
        txtName.setText(name);
        comboCategory.setSelectedItem(category);
        txtPrice.setText(priceStr);
        txtQuantity.setText(String.valueOf(quantity));

        if (status == StockStatus.IN_STOCK) {
            comboStatus.setSelectedIndex(1);
        } else if (status == StockStatus.LOW_STOCK) {
            comboStatus.setSelectedIndex(2);
        } else if (status == StockStatus.OUT_OF_STOCK) {
            comboStatus.setSelectedIndex(3);
        }
    }

    private void clearForm() {
        selectedItemId = -1;
        txtId.setText("");
        txtName.setText("");
        comboCategory.setSelectedIndex(0);
        txtPrice.setText("");
        txtQuantity.setText("");
        comboStatus.setSelectedIndex(0);
        itemTable.clearSelection();
    }

    public static class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int radius;
        private final Color bgColor;
        private final Color borderColor;
        private final Color leftAccentColor;
        private final int leftAccentWidth;

        public RoundedPanel(int radius, Color bgColor, Color borderColor) {
            this(radius, bgColor, borderColor, null, 0);
        }

        public RoundedPanel(int radius, Color bgColor, Color borderColor, Color leftAccentColor, int leftAccentWidth) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.leftAccentColor = leftAccentColor;
            this.leftAccentWidth = leftAccentWidth;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            if (leftAccentColor != null && leftAccentWidth > 0) {
                Shape oldClip = g2.getClip();
                g2.clip(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                g2.setColor(leftAccentColor);
                g2.fillRect(0, 0, leftAccentWidth, getHeight());
                g2.setClip(oldClip);
            }

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final Color normalBg;
        private final Color hoverBg;
        private final int radius;

        public RoundedButton(String text, Color bg, Color fg, int radius) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = blend(bg, Color.BLACK, 0.12f);
            this.radius = radius;
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 12, 8, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? hoverBg : normalBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }

        private static Color blend(Color c1, Color c2, float ratio) {
            float r = c1.getRed() * (1 - ratio) + c2.getRed() * ratio;
            float g = c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio;
            float b = c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio;
            return new Color((int) r, (int) g, (int) b);
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return new PillBadgeComponent(value instanceof StockStatus ? (StockStatus) value : null,
                    isSelected, table.getSelectionBackground());
        }

        private static class PillBadgeComponent extends JComponent {
            private static final long serialVersionUID = 1L;
            private final StockStatus status;
            private final boolean isSelected;
            private final Color selectionBg;

            PillBadgeComponent(StockStatus status, boolean isSelected, Color selectionBg) {
                this.status = status;
                this.isSelected = isSelected;
                this.selectionBg = selectionBg;
                setFont(new Font("Segoe UI", Font.BOLD, 12));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2.setColor(selectionBg);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                if (status != null) {
                    Color pillBg;
                    Color pillFg;

                    switch (status) {
                        case IN_STOCK:
                            pillBg = new Color(0xDC, 0xFC, 0xE7); // #DCFCE7
                            pillFg = new Color(0x16, 0x65, 0x34); // #166534
                            break;
                        case LOW_STOCK:
                            pillBg = new Color(0xFE, 0xF3, 0xC7); // #FEF3C7
                            pillFg = new Color(0x92, 0x40, 0x0E); // #92400E
                            break;
                        case OUT_OF_STOCK:
                        default:
                            pillBg = new Color(0xFE, 0xE2, 0xE2); // #FEE2E2
                            pillFg = new Color(0x99, 0x1B, 0x1B); // #991B1B
                            break;
                    }

                    String text = status.getDisplayName();
                    FontMetrics fm = g2.getFontMetrics(getFont());
                    int textW = fm.stringWidth(text);
                    int textH = fm.getAscent();

                    int pillW = textW + 24;
                    int pillH = 26;
                    int pillX = (getWidth() - pillW) / 2;
                    int pillY = (getHeight() - pillH) / 2;

                    g2.setColor(pillBg);
                    g2.fillRoundRect(pillX, pillY, pillW, pillH, 14, 14);

                    g2.setColor(pillFg);
                    g2.setFont(getFont());
                    g2.drawString(text, pillX + (pillW - textW) / 2, pillY + (pillH + textH) / 2 - 2);
                }
                g2.dispose();
            }
        }
    }
}
