#!/bin/bash
# YouGlish Reels - Quick Start Script for Unix/Linux/Mac

echo "================================"
echo " YouGlish Reels - Quick Start"
echo "================================"
echo ""

# Check Java version
echo "[1/5] Checking Java version..."
if ! java -version 2>&1 | grep -q "version \"21"; then
    echo "ERROR: Java 21 or higher is required"
    echo "Please install Java 21+ and try again"
    exit 1
fi
echo "Java OK"

echo ""
echo "[2/5] Checking PostgreSQL..."
if ! command -v psql &> /dev/null; then
    echo "WARNING: PostgreSQL command line tools not found"
    echo "Make sure PostgreSQL is installed and running"
else
    echo "PostgreSQL OK"
fi

echo ""
echo "[3/5] Setting up database..."
echo "Please ensure PostgreSQL is running and you have created the database:"
echo "  CREATE DATABASE youglish_reels;"
echo ""
read -p "Press Enter to continue (or Ctrl+C to cancel)..."

echo ""
echo "[4/5] Building application..."
./gradlew clean build -x test
if [ $? -ne 0 ]; then
    echo "ERROR: Build failed"
    exit 1
fi

echo ""
echo "[5/5] Starting application..."
echo ""
echo "================================"
echo " Backend will start on:"
echo " http://localhost:8080"
echo "================================"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

./gradlew bootRun

