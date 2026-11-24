package com.inventory.ui;

import com.inventory.model.InventoryItem;
import com.inventory.model.StockStatus;
import com.inventory.service.InventoryManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleFunction;

/**
 * High-contrast SaaS GUI for the Inventory Management System (Java Swing).
 * Supports 5 KPI cards, categories, unit pricing, total value and 1-click CSV export.
 *
 * @author Muhammad Hassan
 * @version 4.1
 */
public class InventoryGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // ---------- High-contrast palette ----------
    private static final Color CANVAS_BG = new Color(0x1E, 0x29, 0x3B);        // #1E293B dark slate canvas
    private static final Color HEADER_BG = new Color(0x0F, 0x17, 0x2A);        // #0F172A navy header
    private static final Color HEADER_BORDER = new Color(0x33, 0x41, 0x55);    // #334155
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xCB, 0xD5, 0xE1);     // #CBD5E1
    private static final Color INPUT_BORDER = new Color(0x94, 0xA3, 0xB8);     // #94A3B8
    private static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);     // #0F172A
    private static final Color TEXT_LABEL = new Color(0x1E, 0x29, 0x3B);       // #1E293B
    private static final Color TEXT_MUTED = new Color(0x47, 0x55, 0x69);       // #475569
    private static final Color TEXT_ON_DARK = new Color(0xF8, 0xFA, 0xFC);     // #F8FAFC
    private static final Color TEXT_ON_DARK_MUTED = new Color(0x94, 0xA3, 0xB8);
    private static final Color FOCUS_COLOR = new Color(0x3B, 0x82, 0xF6);      // #3B82F6
    private static final Color TABLE_HEADER_BG = new Color(0x1E, 0x3A, 0x8A);  // #1E3A8A deep blue
    private static final Color ROW_ALT = new Color(0xF1, 0xF5, 0xF9);          // zebra
    private static final Color ROW_HOVER = new Color(0xE0, 0xF2, 0xFE);        // #E0F2FE
    private static final Color SELECTION_BG = new Color(0xBF, 0xDB, 0xFE);     // #BFDBFE
    private static final Color DIVIDER_COLOR = new Color(0xE2, 0xE8, 0xF0);    // #E2E8F0
    private static final Color READONLY_BG = new Color(0xE2, 0xE8, 0xF0);

    private static final Color COLOR_TOTAL_ITEMS = new Color(0x25, 0x63, 0xEB); // blue
    private static final Color COLOR_TOTAL_UNITS = new Color(0x05, 0x96, 0x69); // green
    private static final Color COLOR_NET_WORTH = new Color(0x7C, 0x3A, 0xED);   // purple
    private static final Color COLOR_LOW_STOCK = new Color(0xD9, 0x77, 0x06);   // amber
    private static final Color COLOR_OUT_STOCK = new Color(0xDC, 0x26, 0x26);   // red

    // ---------- Shared fonts / borders (created once) ----------
    private static final String FONT_NAME = "Segoe UI";
    private static final Font F_REGULAR = new Font(FONT_NAME, Font.PLAIN, 13);
    private static final Font F_BOLD = new Font(FONT_NAME, Font.BOLD, 13);
    private static final Font F_SMALL = new Font(FONT_NAME, Font.PLAIN, 12);
    private static final Font F_SMALL_BOLD = new Font(FONT_NAME, Font.BOLD, 12);

    private static final Border INPUT_NORMAL = new CompoundBorder(
            BorderFactory.createLineBorder(INPUT_BORDER, 1, true), new EmptyBorder(7, 8, 7, 8));
    private static final Border INPUT_FOCUSED = new CompoundBorder(
            BorderFactory.createLineBorder(FOCUS_COLOR, 2, true), new EmptyBorder(6, 7, 6, 7));

    private static final String[] CATEGORY_OPTIONS = {
            "Electronics", "Accessories", "Office Supplies", "Groceries", "Hardware", "General"
    };

    private static final String ALL_CATEGORIES = "All Categories";
    private static final String ALL_STATUSES = "All Statuses";
    private static final String KPI_ITEMS = "Total Items";
    private static final String KPI_UNITS = "Total Units";
    private static final String KPI_WORTH = "Net Worth";
    private static final String KPI_LOW = "Low Stock";
    private static final String KPI_OUT = "Out of Stock";

    private final InventoryManager manager;

    // Form inputs
    private JTextField txtId;
    private JTextField txtName;
    private JComboBox<String> comboCategory;
    private JTextField txtPrice;
    private JTextField txtQuantity;
    private JComboBox<String> comboStatus;

    // Search and filters
    private JTextField txtSearch;
    private JComboBox<String> comboCategoryFilter;
    private JComboBox<String> comboStatusFilter;
    private javax.swing.Timer searchDebounce;

    // Dashboard
    private JLabel lblTotalItems;
    private JLabel lblTotalUnits;
    private JLabel lblNetWorth;
    private JLabel lblLowStock;
    private JLabel lblOutOfStock;
    private JLabel lblStatusMsg;
    private final Map<String, RoundedPanel> kpiCards = new LinkedHashMap<>();

    // Table
    private JTable itemTable;
    private DefaultTableModel tableModel;

    private int selectedItemId = -1;

    /** Validated form values. */
    private record FormData(String name, String category, double price, int quantity, StockStatus status) { }

    public InventoryGUI(InventoryManager manager) {
        this.manager = manager;
        initUI();
        populateSampleData();
        refreshTable();
        updateDashboard();
    }

    // =====================================================================
    // UI construction
    // =====================================================================

    private void initUI() {
        setTitle("HR Traders - Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1120, 680));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(14, 14));
        mainPanel.setBorder(new EmptyBorder(16, 16, 10, 16));
        mainPanel.setBackground(CANVAS_BG);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(14, 14));
        contentPanel.setOpaque(false);
        contentPanel.add(createFormPanel(), BorderLayout.WEST);
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);
        setContentPane(mainPanel);

        installDynamics();
    }

    private JPanel createHeaderPanel() {
        RoundedPanel headerCard = new RoundedPanel(12, HEADER_BG, HEADER_BORDER);
        headerCard.setLayout(new BorderLayout(16, 16));
        headerCard.setBorder(new EmptyBorder(14, 20, 14, 16));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("HR Traders");
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 24));
        titleLabel.setForeground(TEXT_ON_DARK);

        JLabel subLabel = new JLabel("Inventory Management System");
        subLabel.setFont(F_SMALL);
        subLabel.setForeground(TEXT_ON_DARK_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);
        headerCard.add(titlePanel, BorderLayout.WEST);

        JPanel kpiContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        kpiContainer.setOpaque(false);

        RoundedPanel cItems = createKpiCard(KPI_ITEMS, COLOR_TOTAL_ITEMS, 112, 22, "All Statuses");
        RoundedPanel cUnits = createKpiCard(KPI_UNITS, COLOR_TOTAL_UNITS, 112, 22, "All Statuses");
        RoundedPanel cWorth = createKpiCard(KPI_WORTH, COLOR_NET_WORTH, 190, 17, "All Statuses");
        RoundedPanel cLow = createKpiCard(KPI_LOW, COLOR_LOW_STOCK, 112, 22, "Low Stock");
        RoundedPanel cOut = createKpiCard(KPI_OUT, COLOR_OUT_STOCK, 112, 22, "Out of Stock");

        lblTotalItems = valueLabelOf(cItems);
        lblTotalUnits = valueLabelOf(cUnits);
        lblNetWorth = valueLabelOf(cWorth);
        lblLowStock = valueLabelOf(cLow);
        lblOutOfStock = valueLabelOf(cOut);

        kpiContainer.add(cItems);
        kpiContainer.add(cUnits);
        kpiContainer.add(cWorth);
        kpiContainer.add(cLow);
        kpiContainer.add(cOut);
        headerCard.add(kpiContainer, BorderLayout.EAST);
        return headerCard;
    }

    private static JLabel valueLabelOf(RoundedPanel card) {
        return (JLabel) card.getClientProperty("valueLabel");
    }

    /** Solid-color clickable KPI card. targetStatus is the status filter applied on click. */
    private RoundedPanel createKpiCard(String title, Color color, int width, int valueSize, String targetStatus) {
        RoundedPanel card = new RoundedPanel(12, color, color, color, 4, true);
        card.setLayout(new GridLayout(2, 1, 2, 2));
        card.setBorder(new EmptyBorder(10, 18, 10, 12));
        card.setPreferredSize(new Dimension(width, 64));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setToolTipText("Click to filter table (click again to reset)");

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        titleLbl.setForeground(new Color(0xF1, 0xF5, 0xF9));

        JLabel valLbl = new JLabel("0");
        valLbl.setFont(new Font(FONT_NAME, Font.BOLD, valueSize));
        valLbl.setForeground(Color.WHITE);

        card.add(titleLbl);
        card.add(valLbl);
        card.putClientProperty("valueLabel", valLbl);
        kpiCards.put(title, card);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setHovered(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setHovered(false);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (comboStatusFilter == null) {
                    return;
                }
                boolean toggleOff = !"All Statuses".equals(targetStatus)
                        && targetStatus.equals(comboStatusFilter.getSelectedItem());
                if (toggleOff || "All Statuses".equals(targetStatus)) {
                    comboStatusFilter.setSelectedIndex(0);
                } else {
                    comboStatusFilter.setSelectedItem(targetStatus);
                }
                // combo listener triggers applySearchAndFilter()
            }
        });
        return card;
    }

    private JPanel createFormPanel() {
        RoundedPanel formContainer = new RoundedPanel(12, CARD_BG, BORDER_COLOR);
        formContainer.setLayout(new BorderLayout(10, 10));
        formContainer.setPreferredSize(new Dimension(330, 0));
        formContainer.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setOpaque(false);

        JLabel heading = new JLabel("Product Details");
        heading.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        heading.setForeground(COLOR_TOTAL_ITEMS);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        fields.add(heading);
        fields.add(Box.createVerticalStrut(12));

        txtId = createStyledTextField();
        txtId.setEditable(false);
        txtId.setBackground(READONLY_BG);
        addField(fields, "Item ID (Auto-Generated):", txtId);

        txtName = createStyledTextField();
        addField(fields, "Item Name: *", txtName);

        comboCategory = new JComboBox<>(CATEGORY_OPTIONS);
        styleCombo(comboCategory);
        addField(fields, "Category / Department: *", comboCategory);

        txtPrice = createStyledTextField();
        addField(fields, "Unit Price (PKR): * (e.g. 25000)", txtPrice);

        txtQuantity = createStyledTextField();
        addField(fields, "Quantity: * (0 or greater)", txtQuantity);

        comboStatus = new JComboBox<>(new String[]{"Auto (Calculated from Quantity)", "In Stock", "Low Stock", "Out of Stock"});
        styleCombo(comboStatus);
        addField(fields, "Stock Status:", comboStatus);

        JLabel note = new JLabel("<html><small style='color:#475569; font-family: Segoe UI;'>"
                + "<b>Classification:</b><br>"
                + "&bull; Qty = 0 &rarr; <font color='#991B1B'><b>Out of Stock</b></font><br>"
                + "&bull; Qty 1 - 5 &rarr; <font color='#92400E'><b>Low Stock</b></font><br>"
                + "&bull; Qty &gt; 5 &rarr; <font color='#166534'><b>In Stock</b></font>"
                + "</small></html>");
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        fields.add(note);

        formContainer.add(fields, BorderLayout.CENTER);

        JPanel buttonsGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsGrid.setOpaque(false);
        buttonsGrid.setBorder(new EmptyBorder(10, 0, 0, 0));

        RoundedButton btnAdd = new RoundedButton("Add Item", new Color(0x16, 0xA3, 0x4A), Color.WHITE, 8);
        RoundedButton btnUpdate = new RoundedButton("Update Item", new Color(0x25, 0x63, 0xEB), Color.WHITE, 8);
        RoundedButton btnDelete = new RoundedButton("Delete Item", new Color(0xDC, 0x26, 0x26), Color.WHITE, 8);
        RoundedButton btnClear = new RoundedButton("Clear Form", new Color(0x33, 0x41, 0x55), Color.WHITE, 8);

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

    /** Adds a label + input pair to the vertical form with consistent sizing. */
    private void addField(JPanel panel, String label, JComponent input) {
        JLabel l = new JLabel(label);
        l.setFont(F_SMALL_BOLD);
        l.setForeground(TEXT_LABEL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        panel.add(l);

        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(input);
        panel.add(Box.createVerticalStrut(9));
    }

    private JPanel createTablePanel() {
        RoundedPanel tableContainer = new RoundedPanel(12, CARD_BG, BORDER_COLOR);
        tableContainer.setLayout(new BorderLayout(12, 12));
        tableContainer.setBorder(new EmptyBorder(16, 16, 16, 16));

        // ----- Toolbar -----
        JPanel toolbar = new JPanel(new BorderLayout(8, 8));
        toolbar.setOpaque(false);

        JPanel searchBox = new JPanel(new BorderLayout(6, 6));
        searchBox.setOpaque(false);
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(F_SMALL_BOLD);
        lblSearch.setForeground(TEXT_PRIMARY);

        txtSearch = createStyledTextField();
        txtSearch.setToolTipText("Filter by name, ID, or category...");
        searchBox.add(lblSearch, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);
        toolbar.add(searchBox, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(F_SMALL_BOLD);
        lblCat.setForeground(TEXT_PRIMARY);

        String[] catItems = new String[CATEGORY_OPTIONS.length + 1];
        catItems[0] = ALL_CATEGORIES;
        System.arraycopy(CATEGORY_OPTIONS, 0, catItems, 1, CATEGORY_OPTIONS.length);
        comboCategoryFilter = new JComboBox<>(catItems);
        styleCombo(comboCategoryFilter);
        comboCategoryFilter.addActionListener(e -> applySearchAndFilter());

        JLabel lblStat = new JLabel("Status:");
        lblStat.setFont(F_SMALL_BOLD);
        lblStat.setForeground(TEXT_PRIMARY);

        comboStatusFilter = new JComboBox<>(new String[]{ALL_STATUSES, "In Stock", "Low Stock", "Out of Stock"});
        styleCombo(comboStatusFilter);
        comboStatusFilter.addActionListener(e -> applySearchAndFilter());

        RoundedButton btnRefresh = new RoundedButton("View All", new Color(0x1E, 0x3A, 0x8A), Color.WHITE, 8);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            comboCategoryFilter.setSelectedIndex(0);
            comboStatusFilter.setSelectedIndex(0);
            refreshTable();
            applySearchAndFilter();
            if (manager.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "The inventory is currently empty. Use the left form to add your first product.",
                        "Inventory Empty", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        RoundedButton btnExport = new RoundedButton("Export CSV", new Color(0x05, 0x96, 0x69), Color.WHITE, 8);
        btnExport.setToolTipText("Export inventory report as CSV for Excel / printing");
        btnExport.addActionListener(e -> handleExportCSV());

        actions.add(lblCat);
        actions.add(comboCategoryFilter);
        actions.add(lblStat);
        actions.add(comboStatusFilter);
        actions.add(btnRefresh);
        actions.add(btnExport);

        toolbar.add(actions, BorderLayout.EAST);
        tableContainer.add(toolbar, BorderLayout.NORTH);

        // ----- Table -----
        String[] columnNames = {"ID", "Item Name", "Category", "Unit Price (PKR)", "Quantity",
                "Total Value (PKR)", "Stock Status", "Last Modified"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(42);
        itemTable.setFont(F_REGULAR);
        itemTable.setForeground(TEXT_PRIMARY);
        itemTable.setBackground(Color.WHITE);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setFillsViewportHeight(true);

        itemTable.setShowGrid(false);
        itemTable.setShowHorizontalLines(true);
        itemTable.setShowVerticalLines(false);
        itemTable.setGridColor(DIVIDER_COLOR);
        itemTable.setIntercellSpacing(new Dimension(0, 1));

        itemTable.setSelectionBackground(SELECTION_BG);
        itemTable.setSelectionForeground(TEXT_PRIMARY);

        // Per-column alignment: ID, Category, Qty, Status, Modified centered; name left; money right
        int[] align = {SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.RIGHT,
                SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.CENTER, SwingConstants.CENTER};

        JTableHeader header = itemTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setDefaultRenderer(new HeaderRenderer(align));

        CellRenderer cellRenderer = new CellRenderer(align);
        for (int c = 0; c < columnNames.length; c++) {
            if (c != 6) {
                itemTable.getColumnModel().getColumn(c).setCellRenderer(cellRenderer);
            }
        }
        itemTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());

        itemTable.getColumnModel().getColumn(0).setMaxWidth(70);
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        itemTable.getColumnModel().getColumn(3).setPreferredWidth(135);
        itemTable.getColumnModel().getColumn(4).setMaxWidth(80);
        itemTable.getColumnModel().getColumn(5).setPreferredWidth(145);
        itemTable.getColumnModel().getColumn(6).setPreferredWidth(125);

        // Row hover tracking (repaints only when the hovered row changes)
        itemTable.putClientProperty("hoverRow", -1);
        itemTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setHoverRow(itemTable.rowAtPoint(e.getPoint()));
            }
        });
        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                setHoverRow(-1);
            }
        });

        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = itemTable.getSelectedRow();
                if (row >= 0) {
                    populateFormFromSelectedRow(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE); // eliminate gray bottom block

        tableContainer.add(scrollPane, BorderLayout.CENTER);
        return tableContainer;
    }

    private void setHoverRow(int row) {
        Object cur = itemTable.getClientProperty("hoverRow");
        if (!(cur instanceof Integer c) || c != row) {
            itemTable.putClientProperty("hoverRow", row);
            itemTable.repaint();
        }
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        statusBar.setBorder(new EmptyBorder(2, 6, 0, 6));

        lblStatusMsg = new JLabel("Ready");
        lblStatusMsg.setFont(F_SMALL);
        lblStatusMsg.setForeground(new Color(0xE2, 0xE8, 0xF0));

        JLabel lblDesigner = new JLabel("Designed by Muhammad Hassan");
        lblDesigner.setFont(F_SMALL_BOLD);
        lblDesigner.setForeground(TEXT_ON_DARK_MUTED);

        statusBar.add(lblStatusMsg, BorderLayout.WEST);
        statusBar.add(lblDesigner, BorderLayout.EAST);
        return statusBar;
    }

    private JTextField createStyledTextField() {
        final JTextField field = new JTextField() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Object hint = getClientProperty("hint");
                if (hint != null && getText().isEmpty() && isEditable()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(INPUT_BORDER);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(hint.toString(), getInsets().left,
                            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        field.setFont(F_REGULAR);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setCaretColor(FOCUS_COLOR);
        field.setBorder(INPUT_NORMAL);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.isEditable()) {
                    field.setBorder(INPUT_FOCUSED);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(INPUT_NORMAL);
            }
        });
        return field;
    }

    private static void styleCombo(JComboBox<String> combo) {
        combo.setFont(F_SMALL);
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1, true));
        combo.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, sel, false);
                l.setFont(F_SMALL);
                l.setBorder(new EmptyBorder(6, 10, 6, 10));
                l.setBackground(sel ? SELECTION_BG : Color.WHITE);
                l.setForeground(TEXT_PRIMARY);
                return l;
            }
        });
    }

    /** Hints, keyboard shortcuts, debounced search and status messages. */
    private void installDynamics() {
        txtName.putClientProperty("hint", "e.g. Wireless Mouse");
        txtPrice.putClientProperty("hint", "e.g. 25000");
        txtQuantity.putClientProperty("hint", "0 or greater");
        txtSearch.putClientProperty("hint", "Search by name, ID or category...");

        // Enter moves forward; Enter on Quantity saves (update when editing, add otherwise)
        txtName.addActionListener(e -> txtPrice.requestFocusInWindow());
        txtPrice.addActionListener(e -> txtQuantity.requestFocusInWindow());
        txtQuantity.addActionListener(e -> {
            if (selectedItemId > 0) {
                handleUpdateItem();
            } else {
                handleAddItem();
            }
        });

        // Debounced live search (also reacts to paste / cut)
        searchDebounce = new javax.swing.Timer(140, e -> applySearchAndFilter());
        searchDebounce.setRepeats(false);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchDebounce.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchDebounce.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchDebounce.restart();
            }
        });

        itemTable.getSelectionModel().addListSelectionListener(e -> {
            int r = itemTable.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) {
                setStatus("Editing item #" + tableModel.getValueAt(r, 0)
                        + "  |  Change the fields, then press Update Item");
            }
        });
        setStatus("Ready  |  Select a row to edit or delete. Click a KPI card to filter.");
    }

    // =====================================================================
    // Data / dashboard / filtering
    // =====================================================================

    private void populateSampleData() {
        manager.addItem(new InventoryItem(0, "Apple MacBook Pro 16\" M3 Max", "Electronics", 980000.00, 6));
        manager.addItem(new InventoryItem(0, "Dell XPS 15 OLED Laptop", "Electronics", 520000.00, 4));
        manager.addItem(new InventoryItem(0, "ASUS ROG Zephyrus G14", "Electronics", 440000.00, 3));
        manager.addItem(new InventoryItem(0, "NVIDIA GeForce RTX 4090 24GB", "Electronics", 590000.00, 2));
        manager.addItem(new InventoryItem(0, "AMD Ryzen 9 7950X Processor", "Electronics", 155000.00, 8));
        manager.addItem(new InventoryItem(0, "Intel Core i9-14900K Desktop CPU", "Electronics", 165000.00, 5));
        manager.addItem(new InventoryItem(0, "Samsung 990 PRO 2TB NVMe SSD", "Electronics", 52000.00, 18));
        manager.addItem(new InventoryItem(0, "Corsair Vengeance 32GB DDR5 RAM", "Electronics", 35000.00, 14));
        manager.addItem(new InventoryItem(0, "Dell UltraSharp 27\" 4K Monitor", "Electronics", 125000.00, 7));
        manager.addItem(new InventoryItem(0, "LG UltraGear 34\" Curved Gaming Monitor", "Electronics", 220000.00, 3));
        manager.addItem(new InventoryItem(0, "Sony WH-1000XM5 Wireless Headphones", "Electronics", 110000.00, 9));
        manager.addItem(new InventoryItem(0, "Apple iPad Air 11\" M2 128GB", "Electronics", 175000.00, 11));
        manager.addItem(new InventoryItem(0, "Samsung Galaxy Tab S9 Ultra", "Electronics", 330000.00, 2));
        manager.addItem(new InventoryItem(0, "Logitech MX Master 3S Wireless Mouse", "Accessories", 28500.00, 24));
        manager.addItem(new InventoryItem(0, "Keychron Q1 Pro Mechanical Keyboard", "Accessories", 58000.00, 8));
        manager.addItem(new InventoryItem(0, "Logitech Brio 4K Ultra HD Webcam", "Electronics", 55000.00, 12));
        manager.addItem(new InventoryItem(0, "Shure SM7B Cardioid Dynamic Mic", "Electronics", 115000.00, 4));
        manager.addItem(new InventoryItem(0, "Elgato Stream Deck MK.2", "Accessories", 42000.00, 6));
        manager.addItem(new InventoryItem(0, "ASUS ROG Rapture GT-AXE16000 Router", "Electronics", 170000.00, 3));
        manager.addItem(new InventoryItem(0, "Anker 737 Power Bank 24,000mAh", "Electronics", 38000.00, 15));
        manager.addItem(new InventoryItem(0, "Belkin MagSafe 3-in-1 Wireless Charger", "Accessories", 36000.00, 10));
        manager.addItem(new InventoryItem(0, "Seagate IronWolf 8TB NAS HDD", "Electronics", 56000.00, 5));
        manager.addItem(new InventoryItem(0, "Corsair RM1000x 1000W Power Supply", "Electronics", 54000.00, 7));
        manager.addItem(new InventoryItem(0, "NZXT Kraken Elite 360 RGB Liquid Cooler", "Electronics", 78000.00, 4));
        manager.addItem(new InventoryItem(0, "Apple Watch Ultra 2 GPS + Cellular", "Electronics", 240000.00, 6));
        manager.addItem(new InventoryItem(0, "Samsung Galaxy S24 Ultra 512GB", "Electronics", 380000.00, 8));
        manager.addItem(new InventoryItem(0, "Sony PlayStation 5 Slim Digital", "Electronics", 155000.00, 0));
        manager.addItem(new InventoryItem(0, "Nintendo Switch OLED Model", "Electronics", 110000.00, 0));
        manager.addItem(new InventoryItem(0, "Meta Quest 3 128GB VR Headset", "Electronics", 165000.00, 5));
        manager.addItem(new InventoryItem(0, "Anker USB-C 100W Fast Charging Cable", "Accessories", 4500.00, 35));
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
                    String.format("PKR %,.2f", item.getUnitPrice()),
                    item.getQuantity(),
                    String.format("PKR %,.2f", item.getTotalValue()),
                    item.getStatus(),
                    item.getLastUpdated()
            });
        }
    }

    public void updateDashboard() {
        DoubleFunction<String> asInt = v -> String.valueOf(Math.round(v));
        animateValue(lblTotalItems, manager.getItemCount(), asInt);
        animateValue(lblTotalUnits, manager.getTotalUnits(), asInt);
        animateValue(lblNetWorth, manager.getTotalInventoryValue(), v -> String.format("PKR %,.2f", v));
        animateValue(lblLowStock, manager.getLowStockCount(), asInt);
        animateValue(lblOutOfStock, manager.getOutOfStockCount(), asInt);
        syncKpiHighlight();
    }

    /** Smoothly counts a KPI label from its previous value to the new target. */
    private void animateValue(JLabel label, double target, DoubleFunction<String> format) {
        if (label.getClientProperty("animTimer") instanceof javax.swing.Timer old) {
            old.stop();
        }
        final double start = label.getClientProperty("animValue") instanceof Double d ? d : 0.0;
        label.putClientProperty("animValue", target);
        if (start == target) {
            label.setText(format.apply(target));
            return;
        }
        final int steps = 14;
        final int[] tick = {0};
        final javax.swing.Timer timer = new javax.swing.Timer(18, null);
        timer.addActionListener(ev -> {
            tick[0]++;
            double p = Math.min(1.0, tick[0] / (double) steps);
            double eased = 1 - Math.pow(1 - p, 3);
            label.setText(format.apply(start + (target - start) * eased));
            if (tick[0] >= steps) {
                label.setText(format.apply(target));
                timer.stop();
            }
        });
        label.putClientProperty("animTimer", timer);
        timer.start();
    }

    private void syncKpiHighlight() {
        if (comboStatusFilter == null) {
            return;
        }
        String status = (String) comboStatusFilter.getSelectedItem();
        String active = switch (status == null ? ALL_STATUSES : status) {
            case "Low Stock" -> KPI_LOW;
            case "Out of Stock" -> KPI_OUT;
            case "In Stock" -> "";
            default -> KPI_ITEMS;
        };
        kpiCards.forEach((name, card) -> card.setHighlighted(name.equals(active)));
    }

    private void setStatus(String msg) {
        if (lblStatusMsg != null) {
            lblStatusMsg.setText(msg);
        }
    }

    private void applySearchAndFilter() {
        String query = txtSearch.getText().trim();
        String selectedCat = (String) comboCategoryFilter.getSelectedItem();
        String selectedStatus = (String) comboStatusFilter.getSelectedItem();

        List<InventoryItem> filtered = manager.searchByName(query);

        if (selectedCat != null && !selectedCat.equals(ALL_CATEGORIES)) {
            filtered = filtered.stream()
                    .filter(i -> i.getCategory().equalsIgnoreCase(selectedCat))
                    .toList();
        }

        StockStatus target = statusFromLabel(selectedStatus);
        if (target != null) {
            filtered = filtered.stream().filter(i -> i.getStatus() == target).toList();
        }

        renderItems(filtered);
        syncKpiHighlight();
        setStatus("Showing " + filtered.size() + " of " + manager.getItemCount() + " items"
                + (target != null ? "  |  Status: " + selectedStatus : ""));
    }

    private static StockStatus statusFromLabel(String label) {
        if (label == null) {
            return null;
        }
        return switch (label) {
            case "In Stock" -> StockStatus.IN_STOCK;
            case "Low Stock" -> StockStatus.LOW_STOCK;
            case "Out of Stock" -> StockStatus.OUT_OF_STOCK;
            default -> null;
        };
    }

    private static StockStatus statusFromIndex(int index) {
        return switch (index) {
            case 1 -> StockStatus.IN_STOCK;
            case 2 -> StockStatus.LOW_STOCK;
            case 3 -> StockStatus.OUT_OF_STOCK;
            default -> null; // Auto
        };
    }

    // =====================================================================
    // CRUD handlers
    // =====================================================================

    /** Validates the form. Shows a dialog and returns null on the first error. */
    private FormData readAndValidateForm() {
        String name = txtName.getText() == null ? "" : txtName.getText().trim();
        String category = (String) comboCategory.getSelectedItem();

        if (name.isEmpty()) {
            showError("Validation Error: Item name cannot be empty.", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return null;
        }

        double price;
        try {
            price = parsePrice(txtPrice.getText());
        } catch (NumberFormatException e) {
            showError("Validation Error: Unit price must be a valid number (e.g. 25000).", JOptionPane.ERROR_MESSAGE);
            txtPrice.requestFocus();
            return null;
        }
        if (price < 0.0) {
            showError("Validation Error: Unit price cannot be negative.", JOptionPane.ERROR_MESSAGE);
            txtPrice.requestFocus();
            return null;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(txtQuantity.getText().trim());
        } catch (NumberFormatException e) {
            showError("Validation Error: Quantity must be a valid integer number.", JOptionPane.ERROR_MESSAGE);
            txtQuantity.requestFocus();
            return null;
        }
        if (quantity < 0) {
            showError("Validation Error: Quantity cannot be negative (" + quantity + ").", JOptionPane.ERROR_MESSAGE);
            txtQuantity.requestFocus();
            return null;
        }

        return new FormData(name, category, price, quantity, statusFromIndex(comboStatus.getSelectedIndex()));
    }

    private static double parsePrice(String text) {
        String sanitized = text == null ? "" : text.replaceAll("(?i)pkr|rs\\.?|\\$|,", "").trim();
        return Double.parseDouble(sanitized);
    }

    private void showError(String message, int type) {
        JOptionPane.showMessageDialog(this, message, "Input Validation Error", type);
    }

    private void handleAddItem() {
        FormData f = readAndValidateForm();
        if (f == null) {
            return;
        }

        InventoryItem newItem = new InventoryItem(0, f.name(), f.category(), f.price(), f.quantity(), f.status());
        manager.addItem(newItem);

        JOptionPane.showMessageDialog(this,
                "Success! Item \"" + newItem.getName() + "\" added with ID #" + newItem.getId()
                        + " (" + newItem.getStatus().getDisplayName() + ").",
                "Item Added", JOptionPane.INFORMATION_MESSAGE);

        afterChange();
    }

    private void handleUpdateItem() {
        if (selectedItemId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item from the table or provide an ID to update.",
                    "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FormData f = readAndValidateForm();
        if (f == null) {
            return;
        }

        boolean updated = manager.updateItem(selectedItemId, f.name(), f.category(), f.price(), f.quantity(), f.status());

        if (updated) {
            JOptionPane.showMessageDialog(this,
                    "Item #" + selectedItemId + " successfully updated!",
                    "Item Updated", JOptionPane.INFORMATION_MESSAGE);
            afterChange();
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
            if (manager.deleteItem(selectedItemId)) {
                JOptionPane.showMessageDialog(this,
                        "Item #" + selectedItemId + " removed successfully.",
                        "Item Deleted", JOptionPane.INFORMATION_MESSAGE);
                afterChange();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete item. It may have already been removed.",
                        "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Common post-CRUD refresh: reset form, re-apply active filters, update KPIs. */
    private void afterChange() {
        clearForm();
        applySearchAndFilter();
        updateDashboard();
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

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
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

        // Read raw values from the model layer instead of parsing formatted table strings
        Optional<InventoryItem> opt = manager.findById(selectedItemId);
        if (opt.isEmpty()) {
            return;
        }
        InventoryItem item = opt.get();

        txtId.setText(String.valueOf(item.getId()));
        txtName.setText(item.getName());
        comboCategory.setSelectedItem(item.getCategory());
        txtPrice.setText(String.valueOf(item.getUnitPrice()));
        txtQuantity.setText(String.valueOf(item.getQuantity()));

        switch (item.getStatus()) {
            case IN_STOCK -> comboStatus.setSelectedIndex(1);
            case LOW_STOCK -> comboStatus.setSelectedIndex(2);
            case OUT_OF_STOCK -> comboStatus.setSelectedIndex(3);
            default -> comboStatus.setSelectedIndex(0);
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

    // =====================================================================
    // Row colors + renderers
    // =====================================================================

    private static Color rowBackground(JTable t, boolean selected, int row) {
        if (selected) {
            return SELECTION_BG;
        }
        if (t.getClientProperty("hoverRow") instanceof Integer r && r == row) {
            return ROW_HOVER;
        }
        return row % 2 == 0 ? Color.WHITE : ROW_ALT;
    }

    /** Deep-blue header with white bold text. */
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final int[] align;

        HeaderRenderer(int[] align) {
            this.align = align;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                       boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, value, false, false, r, c);
            setFont(F_SMALL_BOLD);
            setForeground(Color.WHITE);
            setBackground(TABLE_HEADER_BG);
            setHorizontalAlignment(align[c]);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    /** One shared text renderer: zebra, hover tint, soft-blue selection and accent bar on the ID cell. */
    private static class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private static final Border PLAIN = new EmptyBorder(0, 10, 0, 10);
        private static final Border SELECTED_ID = new CompoundBorder(
                new MatteBorder(0, 4, 0, 0, COLOR_TOTAL_ITEMS), new EmptyBorder(0, 6, 0, 10));
        private final int[] align;

        CellRenderer(int[] align) {
            this.align = align;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, sel, false, row, col);
            setFont(col == 1 ? F_BOLD : F_REGULAR);
            setBackground(rowBackground(t, sel, row));
            setForeground(col == 7 && !sel ? TEXT_MUTED : TEXT_PRIMARY);
            setHorizontalAlignment(align[col]);
            setBorder(col == 0 && sel ? SELECTED_ID : PLAIN);
            return this;
        }
    }

    /** Single reusable pill-badge renderer for the Stock Status column. */
    private static class StatusBadgeRenderer extends JComponent implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        private StockStatus status;
        private Color rowBg = Color.WHITE;

        StatusBadgeRenderer() {
            setFont(F_SMALL_BOLD);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            status = value instanceof StockStatus s ? s : null;
            rowBg = rowBackground(table, isSelected, row);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (status != null) {
                Color pillBg;
                Color pillFg;
                switch (status) {
                    case IN_STOCK -> {
                        pillBg = new Color(0xDC, 0xFC, 0xE7); // #DCFCE7
                        pillFg = new Color(0x16, 0x65, 0x34); // #166534
                    }
                    case LOW_STOCK -> {
                        pillBg = new Color(0xFE, 0xF3, 0xC7); // #FEF3C7
                        pillFg = new Color(0x92, 0x40, 0x0E); // #92400E
                    }
                    default -> {
                        pillBg = new Color(0xFE, 0xE2, 0xE2); // #FEE2E2
                        pillFg = new Color(0x99, 0x1B, 0x1B); // #991B1B
                    }
                }

                String text = status.getDisplayName();
                FontMetrics fm = g2.getFontMetrics(getFont());
                int dot = 6;
                int pillW = fm.stringWidth(text) + dot + 34;
                int pillH = 26;
                int pillX = (getWidth() - pillW) / 2;
                int pillY = (getHeight() - pillH) / 2;

                g2.setColor(pillBg);
                g2.fillRoundRect(pillX, pillY, pillW, pillH, pillH, pillH);

                g2.setColor(pillFg);
                g2.fillOval(pillX + 12, pillY + (pillH - dot) / 2, dot, dot);

                g2.setFont(getFont());
                g2.drawString(text, pillX + 12 + dot + 6,
                        pillY + (pillH - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    // =====================================================================
    // Custom components
    // =====================================================================

    /**
     * Rounded panel with border, optional left accent strip and an optional solid-color KPI mode
     * with hover and highlight states.
     */
    public static class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int radius;
        private final Color bgColor;
        private final Color borderColor;
        private final Color accentColor;
        private final int accentWidth;
        private final boolean solid;
        private boolean highlighted;
        private boolean hovered;

        public RoundedPanel(int radius, Color bgColor, Color borderColor) {
            this(radius, bgColor, borderColor, null, 0, false);
        }

        public RoundedPanel(int radius, Color bgColor, Color borderColor, Color accentColor,
                            int accentWidth, boolean solid) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.accentColor = accentColor;
            this.accentWidth = accentWidth;
            this.solid = solid;
            setOpaque(false);
        }

        public void setHighlighted(boolean value) {
            if (highlighted != value) {
                highlighted = value;
                repaint();
            }
        }

        public void setHovered(boolean value) {
            if (hovered != value) {
                hovered = value;
                repaint();
            }
        }

        private static Color mix(Color a, Color b, float ratio) {
            return new Color(
                    Math.round(a.getRed() * (1 - ratio) + b.getRed() * ratio),
                    Math.round(a.getGreen() * (1 - ratio) + b.getGreen() * ratio),
                    Math.round(a.getBlue() * (1 - ratio) + b.getBlue() * ratio));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = bgColor;
            Color edge = borderColor;
            float stroke = 1f;
            if (solid) {
                if (hovered && !highlighted) {
                    fill = mix(bgColor, Color.WHITE, 0.12f);
                }
                if (highlighted) {
                    edge = Color.WHITE;
                    stroke = 2.5f;
                } else {
                    edge = mix(bgColor, Color.BLACK, 0.15f);
                }
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            if (accentColor != null && accentWidth > 0) {
                Shape oldClip = g2.getClip();
                g2.clip(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                g2.setColor(solid ? mix(accentColor, Color.WHITE, 0.65f) : accentColor);
                g2.fillRect(0, 0, accentWidth, getHeight());
                g2.setClip(oldClip);
            }

            if (edge != null) {
                g2.setColor(edge);
                g2.setStroke(new BasicStroke(stroke));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Solid rounded button with hover / pressed states and high-contrast text. */
    public static class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final int radius;

        public RoundedButton(String text, Color bg, Color fg, int radius) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = blend(bg, Color.BLACK, 0.12f);
            this.pressedBg = blend(bg, Color.BLACK, 0.22f);
            this.radius = radius;
            setFont(F_SMALL_BOLD);
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 14, 10, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = !isEnabled() ? blend(normalBg, Color.WHITE, 0.5f)
                    : getModel().isPressed() ? pressedBg
                    : getModel().isRollover() ? hoverBg : normalBg;
            g2.setColor(c);
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
}