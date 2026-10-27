@echo off
setlocal

:: Define the path to your portable Zed executable
set "ZED_PATH=.\editor\zed.exe"

:: Check if zed.exe exists before launching
if not exist "%ZED_PATH%" (
    echo [ERROR] Could not find zed.exe at: "%ZED_PATH%"
    echo.
    pause
    exit /b 1
)

:: Launch Zed with the current directory as the target argument
start "" "%ZED_PATH%" "%CD%"

endlocal
