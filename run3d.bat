@echo off
title Smart Parking Finder 3D Simulation
echo ============================================================
echo   Smart Parking Finder - 3D Simulation System (JavaFX 3D)
echo ============================================================
echo.
echo Launching 3D Simulation Environment...
"C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe" --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics Main %*
if errorlevel 1 (
    echo.
    echo Application exited with an error. Press any key to exit.
    pause >nul
)

