@echo off
echo ==========================================
echo    Mail Chat System - SERVER
echo ==========================================
echo.
echo Starting server...
echo.

cd /d "%~dp0"

REM Run server with all dependencies using shaded JAR
java -cp target\smtp-swing-0.1.0-all.jar smtp.ServerStarter

pause
