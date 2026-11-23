@echo off
echo ===================================================
echo  Compiling Store Inventory Management System
echo ===================================================

if not exist bin mkdir bin

javac -d bin src\com\inventory\model\*.java src\com\inventory\service\*.java src\com\inventory\ui\*.java src\com\inventory\Main.java test\com\inventory\InventoryTest.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation finished successfully. Class files generated in \bin.
) else (
    echo [ERROR] Compilation failed. Please check error logs above.
)
pause
