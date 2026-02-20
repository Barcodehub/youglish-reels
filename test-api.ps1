# YouGlish Reels - API Testing with PowerShell

$BASE_URL = "http://localhost:8080/api"
$TOKEN = ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " YouGlish Reels - API Test Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Register user
Write-Host "[1] Registering new user..." -ForegroundColor Yellow
$registerBody = @{
    username = "testuser"
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody

    $TOKEN = $registerResponse.token
    Write-Host "Registration successful!" -ForegroundColor Green
    Write-Host "Token: $TOKEN" -ForegroundColor Gray
} catch {
    Write-Host "Registration failed (user may exist), trying login..." -ForegroundColor Yellow

    # Try login
    $loginBody = @{
        username = "testuser"
        password = "password123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody

    $TOKEN = $loginResponse.token
    Write-Host "Login successful!" -ForegroundColor Green
    Write-Host "Token: $TOKEN" -ForegroundColor Gray
}

Write-Host ""

# 2. Create phrases
Write-Host "[2] Creating test phrases..." -ForegroundColor Yellow
$phrases = @("great power", "common sense", "time management", "critical thinking")

foreach ($phrase in $phrases) {
    Write-Host "Creating phrase: $phrase" -ForegroundColor Gray

    $phraseBody = @{
        text = $phrase
        language = "english"
    } | ConvertTo-Json

    try {
        $result = Invoke-RestMethod -Uri "$BASE_URL/phrases" `
            -Method Post `
            -ContentType "application/json" `
            -Headers @{ Authorization = "Bearer $TOKEN" } `
            -Body $phraseBody

        Write-Host "  Created: $($result.text) (ID: $($result.id))" -ForegroundColor Green
    } catch {
        Write-Host "  Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# 3. Get user phrases
Write-Host "[3] Getting user phrases..." -ForegroundColor Yellow
try {
    $userPhrases = Invoke-RestMethod -Uri "$BASE_URL/phrases" `
        -Method Get `
        -Headers @{ Authorization = "Bearer $TOKEN" }

    Write-Host "Total phrases: $($userPhrases.Count)" -ForegroundColor Green
    $userPhrases | ForEach-Object {
        Write-Host "  - $($_.text) (Active: $($_.isActive))" -ForegroundColor Gray
    }
} catch {
    Write-Host "Failed to get phrases: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 4. Get next video (multiple times to test anti-repetition)
Write-Host "[4] Testing video feed (5 requests)..." -ForegroundColor Yellow
for ($i = 1; $i -le 5; $i++) {
    Write-Host "Request #$i:" -ForegroundColor Cyan

    try {
        $video = Invoke-RestMethod -Uri "$BASE_URL/feed/next" `
            -Method Get `
            -Headers @{ Authorization = "Bearer $TOKEN" }

        Write-Host "  Video ID: $($video.videoId)" -ForegroundColor Green
        Write-Host "  Phrase: $($video.phrase.text)" -ForegroundColor Green
        Write-Host "  Track: $($video.trackNumber) / $($video.totalResults)" -ForegroundColor Gray
        Write-Host "  Caption: $($video.captionText)" -ForegroundColor Gray
    } catch {
        Write-Host "  Failed: $($_.Exception.Message)" -ForegroundColor Red
    }

    Write-Host ""
    Start-Sleep -Seconds 1
}

# 5. Get stats
Write-Host "[5] Getting user statistics..." -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri "$BASE_URL/feed/stats" `
        -Method Get `
        -Headers @{ Authorization = "Bearer $TOKEN" }

    Write-Host "Statistics:" -ForegroundColor Green
    Write-Host "  Total Phrases: $($stats.totalPhrases)" -ForegroundColor Gray
    Write-Host "  Active Phrases: $($stats.activePhrases)" -ForegroundColor Gray
    Write-Host "  Videos Watched: $($stats.totalVideosWatched)" -ForegroundColor Gray
    Write-Host "  Phrases Used Today: $($stats.phrasesUsedToday)" -ForegroundColor Gray
} catch {
    Write-Host "Failed to get stats: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Testing complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

