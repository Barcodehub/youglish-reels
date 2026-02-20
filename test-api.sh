# YouGlish Reels - API Testing with cURL

# Base URL
BASE_URL="http://localhost:8080/api"
TOKEN=""

echo "========================================"
echo " YouGlish Reels - API Test Script"
echo "========================================"
echo ""

# 1. Register user
echo "[1] Registering new user..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }')

echo "Response: $REGISTER_RESPONSE"
TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token: $TOKEN"
echo ""

# If registration fails (user exists), try login
if [ -z "$TOKEN" ]; then
    echo "[1b] User exists, trying login..."
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d '{
        "username": "testuser",
        "password": "password123"
      }')

    echo "Response: $LOGIN_RESPONSE"
    TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    echo "Token: $TOKEN"
    echo ""
fi

# 2. Create phrases
echo "[2] Creating test phrases..."
PHRASES=("great power" "common sense" "time management" "critical thinking")

for phrase in "${PHRASES[@]}"; do
    echo "Creating phrase: $phrase"
    curl -s -X POST "$BASE_URL/phrases" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{
        \"text\": \"$phrase\",
        \"language\": \"english\"
      }" | jq '.'
    echo ""
done

# 3. Get user phrases
echo "[3] Getting user phrases..."
curl -s -X GET "$BASE_URL/phrases" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 4. Get next video (multiple times to test anti-repetition)
echo "[4] Testing video feed (5 requests)..."
for i in {1..5}; do
    echo "Request #$i:"
    curl -s -X GET "$BASE_URL/feed/next" \
      -H "Authorization: Bearer $TOKEN" | jq '{videoId, phraseText: .phrase.text, trackNumber}'
    echo ""
    sleep 1
done

# 5. Get stats
echo "[5] Getting user statistics..."
curl -s -X GET "$BASE_URL/feed/stats" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

echo "========================================"
echo " Testing complete!"
echo "========================================"

