# PowerShell launch script for Smart Parking Finder 3D
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Smart Parking Finder - 3D Simulation System (JavaFX 3D)   " -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Launching 3D Simulation Environment..." -ForegroundColor Green

& "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe" --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics Main $args

