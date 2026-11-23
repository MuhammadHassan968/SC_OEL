@echo off
echo ===================================================
echo  Running Inventory System Automated Unit Tests
echo ===================================================

if not exist bin\com\inventory\InventoryTest.class (
    echo [INFO] Binaries not found. Triggering automated build first...
    call "%~dp0compile.bat"
)

java -cp bin com.inventory.InventoryTest
pause
