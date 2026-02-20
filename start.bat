@echo off
REM YouGlish Reels - Quick Start Script for Windows
REM This script helps you get started quickly

echo ================================
echo  YouGlish Reels - Quick Start
echo ================================
echo.

REM Check Java version
echo [1/5] Checking Java version...
java -version 2>&1 | findstr /R "version.*21" >nul
if %errorlevel% neq 0 (
    echo ERROR: Java 21 or higher is required
    echo Please install Java 21+ and try again
    pause
    exit /b 1
)
echo Java OK

echo.
echo [2/5] Checking PostgreSQL...
psql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: PostgreSQL command line tools not found
    echo Make sure PostgreSQL is installed and running
    echo.
) else (
    echo PostgreSQL OK
)

echo.
echo [3/5] Setting up database...
echo Please ensure PostgreSQL is running and you have created the database:
echo   CREATE DATABASE youglish_reels;
echo.
echo Press any key to continue (or Ctrl+C to cancel)...
pause >nul

echo.
echo [4/5] Building application...
call gradlew.bat clean build -x test
if %errorlevel% neq 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo [5/5] Starting application...
echo.
echo ================================
echo  Backend will start on:
echo  http://localhost:8080
echo ================================
echo.
echo Press Ctrl+C to stop the server
echo.

call gradlew.bat bootRun

pause

