@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1"
if errorlevel 1 (
  echo.
  echo Build did not complete. Read the message above.
  pause
  exit /b 1
)
echo.
echo Build finished. Open build\libs to find the mod JAR.
pause
