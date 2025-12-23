@echo off
echo ==========================================
echo    Mail Chat System - CLIENT
echo ==========================================
echo.
echo Starting client...
echo.

cd /d "%~dp0"

REM Run client with all dependencies using shaded JAR
java -jar target\smtp-swing-0.1.0-all.jar

pause
