@echo off
setlocal enabledelayedexpansion
title Smart Parking Finder 3D Simulation
echo ============================================================
echo   Smart Parking Finder - 3D Simulation System (JavaFX 3D)
echo ============================================================
echo.

:: Detect Java executable
set "JAVA_EXE=java"
if exist "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe" (
    set "JAVA_EXE=C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe"
    set "JAVAC_EXE=C:\Program Files\Java\jdk-26.0.2.1\bin\javac.exe"
) else (
    set "JAVAC_EXE=javac"
)

:: Compile if Parking3DApp.class is missing
if not exist "Parking3DApp.class" (
    echo Compiling Java source files...
    "%JAVAC_EXE%" -cp ".;javafx-sdk\lib\*" *.java
    if errorlevel 1 (
        echo.
        echo [ERROR] Compilation failed. Please check your Java installation.
        pause
        exit /b 1
    )
)

echo Launching 3D Simulation Environment...
"%JAVA_EXE%" --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics --enable-native-access=javafx.graphics Main %*
if errorlevel 1 (
    echo.
    echo Application exited. Press any key to close this window.
    pause >nul
)
