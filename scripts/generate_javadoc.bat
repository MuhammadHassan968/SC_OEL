@echo off
echo ===================================================
echo  Generating Javadoc HTML Documentation
echo ===================================================

if not exist docs mkdir docs

javadoc -d docs -sourcepath src -subpackages com.inventory -author -version -windowtitle "Store Inventory Management System API" -doctitle "Store Inventory Management System (IMS) - Code Documentation"

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Javadoc successfully generated in \docs directory.
    echo Open docs\index.html in any web browser to view the documentation.
) else (
    echo [ERROR] Javadoc generation encountered issues.
)
pause
