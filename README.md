# Inventory Management System (IMS)

A modern, robust, and user-friendly Java Swing desktop application designed for small store inventory tracking. Built with modern software construction tools adhering to **CLO-4** and **CLO-5**.

---

## 📌 Project Overview

This repository contains the complete implementation of the **Java-Based Inventory Management System Using Modern Software Construction Tools** developed as part of the Open-Ended Lab (OEL) for Software Construction.

### Learning Outcomes (CLO Mapping)
- **CLO-4 (PLO-5, Level P-3, Weight 25%):** Practice and adapt modern tools (IDE, JDK, Git, GitHub, Javadoc) for software construction.
- **CLO-5 (PLO-5, Level P-4, Weight 75%):** Execute a software construction workflow independently using development, version-control, and documentation tools, producing a working application with recorded revisions and generated code documentation.

---

## 🚀 Key Features

1. **Add Item:** Add products with unique identifiers, names, quantities, and automatic stock status classification.
2. **View Inventory:** Interactive tabular display of all inventory items with real-time status badges and summary analytics.
3. **Update Item:** Edit item details (name, quantity) with immediate state updates and validation.
4. **Delete Item:** Safely remove items with visual confirmation prompts.
5. **Stock Status Automation:**
   - **Out of Stock:** Quantity = 0
   - **Low Stock:** 1 ≤ Quantity ≤ 5
   - **In Stock:** Quantity > 5
6. **Robust Input Validation:**
   - Non-empty, non-whitespace product names.
   - Non-negative integer quantities (0 or greater).
   - Clear and descriptive error messaging via dialog prompts.
7. **Search & Filter:** Instant search by item name or ID and filter by stock status.
8. **Automated Unit Testing:** Full coverage of all edge and error cases.
9. **Standard Javadoc Documentation:** Comprehensive API comments and compiled HTML documentation.

---

## 🛠️ Architecture & Package Structure

```
oel_SC/
├── .gitignore                      # Git ignore configuration
├── README.md                       # Comprehensive project documentation
├── src/                            # Source code directory
│   └── com/
│       └── inventory/
│           ├── model/
│           │   ├── StockStatus.java     # Enum for inventory item stock status
│           │   └── InventoryItem.java   # Data model entity representing an inventory product
│           ├── service/
│           │   └── InventoryManager.java# Business logic controller for CRUD and validation
│           ├── ui/
│           │   └── InventoryGUI.java    # Swing graphical interface with custom rendering
│           └── Main.java                # Application launch entry point
├── test/                           # Automated test suite
│   └── com/
│       └── inventory/
│           └── InventoryTest.java       # Comprehensive test runner covering all lab test cases
├── docs/                           # Generated Javadoc HTML documentation
└── scripts/                        # Automated build, run, and test scripts
    ├── compile.bat
    ├── run.bat
    ├── test.bat
    └── generate_javadoc.bat
```

---

## ⚙️ Compilation & Execution

### Prerequisites
- Java Development Kit (JDK 17 or higher)
- Git (v2.x or higher)

### Quick Commands (Windows PowerShell / CMD)
```powershell
# Compile all source files
javac -d bin src/com/inventory/model/*.java src/com/inventory/service/*.java src/com/inventory/ui/*.java src/com/inventory/Main.java

# Run Application
java -cp bin com.inventory.Main

# Run Unit Tests
javac -d bin -cp bin test/com/inventory/InventoryTest.java
java -cp bin com.inventory.InventoryTest

# Generate Javadoc
javadoc -d docs -sourcepath src -subpackages com.inventory -author -version
```

---

## 🧪 Test Cases & Results Matrix

| Test Case | Description | Input | Expected Result | Status |
|---|---|---|---|---|
| TC-01 | Add a valid item | Name: "Wireless Mouse", Qty: 15 | Item added with "In Stock" status | ✅ PASS |
| TC-02 | Add item with empty name | Name: "   ", Qty: 10 | Validation error message displayed | ✅ PASS |
| TC-03 | Add negative quantity | Name: "Keyboard", Qty: -5 | Error message displayed | ✅ PASS |
| TC-04 | Add non-numeric quantity | Name: "Monitor", Qty: "ten" | Error message displayed | ✅ PASS |
| TC-05 | View all items | Call `getAllItems()` | All items accurately displayed | ✅ PASS |
| TC-06 | View empty inventory | Inventory cleared | Informative empty inventory message | ✅ PASS |
| TC-07 | Update existing item | Update Qty: 15 -> 2 | Details updated, status -> "Low Stock" | ✅ PASS |
| TC-08 | Update non-existing item | ID: 99999 | Gracefully handled with false/warning | ✅ PASS |
| TC-09 | Delete existing item | Delete item by ID | Item removed from repository | ✅ PASS |

---

## 👨‍💻 Author
- **Student Name:** Muhammad Hassan
- **Course:** Software Construction (CS-xxx)
- **Repository:** Inventory Management System
