# PowerShell launch script for Smart Parking Finder 3D
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Smart Parking Finder - 3D Simulation System (JavaFX 3D)   " -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$javaExe = "java"
$javacExe = "javac"
if (Test-Path "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe") {
    $javaExe = "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe"
    $javacExe = "C:\Program Files\Java\jdk-26.0.2.1\bin\javac.exe"
}

# Auto-compile if classes are missing
if (-not (Test-Path "Parking3DApp.class")) {
    Write-Host "Compiling Java source files..." -ForegroundColor Gray
    & $javacExe -cp ".;javafx-sdk\lib\*" *.java
}

Write-Host "Launching 3D Simulation Environment..." -ForegroundColor Green
& $javaExe --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics --enable-native-access=javafx.graphics Main $args
