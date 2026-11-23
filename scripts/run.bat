@echo off
echo ===================================================
echo  Launching Store Inventory Management System (GUI)
echo ===================================================

if not exist bin\com\inventory\Main.class (
    echo [INFO] Binaries not found. Triggering automated build first...
    call "%~dp0compile.bat"
)

java -cp bin com.inventory.Main
