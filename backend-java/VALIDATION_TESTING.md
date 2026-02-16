# 🔒 Validation & Error Handling - Testing Guide

## Validation Rules

### **POST /api/analysis/run**

#### **ticker** field:
- ✅ **Required** (cannot be null or blank)
- ✅ **Length**: 1-10 characters
- ✅ **Format**: Must start with uppercase letter, followed by uppercase letters, numbers, dots, or dashes
- ✅ **Examples**: 
  - Valid: `AAPL`, `MSFT`, `BRK.B`, `BRK-A`, `T`, `META`
  - Invalid: `aapl` (lowercase), `VERYLONGTICKER` (>10 chars), `123ABC` (starts with number), `AA PL` (space)

#### **period** field:
- ✅ **Required** (cannot be null or blank)
- ✅ **Allowed values** (case-insensitive): `1M`, `3M`, `6M`, `1Y`, `5Y`
- ✅ **Examples**:
  - Valid: `1M`, `3m`, `6M`, `1y`, `5Y`
  - Invalid: `2M`, `10Y`, `1W`, `year`, `month`

---

## Error Response Format

All errors follow this consistent format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable description",
  "details": [
    {
      "field": "fieldName",
      "issue": "what's wrong"
    }
  ],
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/run"
}
```

### Error Codes:
- `VALIDATION_ERROR` - Request validation failed (400)
- `BAD_REQUEST` - Malformed request or invalid data (400)
- `NOT_FOUND` - Resource not found (404)
- `METHOD_NOT_ALLOWED` - HTTP method not supported (405)
- `UNAUTHORIZED` - Authentication required (401)
- `FORBIDDEN` - Access denied (403)
- `INTERNAL_ERROR` - Server error (500)

---

## Test Scenarios

### ✅ **1. Valid Request**

```bash
curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "period": "6M"
  }' | jq
```

**Expected Response (201 CREATED):**
```json
{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "ticker": "AAPL",
  "status": "PROCESSING",
  "progress": 0,
  "message": "Analysis job started. Use /api/analysis/550e8400-e29b-41d4-a716-446655440000/status to poll."
}
```

---

### ❌ **2. Invalid Ticker - Lowercase**

```bash
curl -i -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "aapl",
    "period": "6M"
  }'
```

**Expected Response (400 BAD REQUEST):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "ticker",
      "issue": "Ticker must be uppercase letters, dots, or dashes (e.g., AAPL, BRK.B)"
    }
  ],
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/run"
}
```

---

### ❌ **3. Invalid Ticker - Too Long**

```bash
curl -i -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "VERYLONGTICKER",
    "period": "6M"
  }'
```

**Expected Response (400 BAD REQUEST):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "ticker",
      "issue": "Ticker must be between 1 and 10 characters"
    }
  ],
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/run"
}
```

---

### ❌ **4. Invalid Period**

```bash
curl -i -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "period": "2M"
  }'
```

**Expected Response (400 BAD REQUEST):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "period",
      "issue": "Period must be one of: 1M, 3M, 6M, 1Y, 5Y"
    }
  ],
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/run"
}
```

---

### ❌ **5. Missing Ticker**

```bash
curl -i -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{
    "period": "6M"
  }'
```

**Expected Response (400 BAD REQUEST):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "ticker",
      "issue": "Ticker is required"
    }
  ],
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/run"
}
```

---

### ❌ **6. Multiple Validation Errors**

```bash
curl -i -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "aapl",
    "period": "invalid"
  }'
```

**Expected Response (400 BAD REQUEST):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "ticker",
      "issue": "Ticker must be uppercase letters, dots, or dashes (e.g., AAPL, BRK.B)"
    },
    {
      "field": "period",
      "issue": "Period must be one of: 1M, 3M, 6M, 1Y, 5Y"
    }
  ],
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/run"
}
```

---

### ❌ **7. Malformed JSON**

```bash
curl -i -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL"
    "period": "6M"
  }'
```

**Expected Response (400 BAD REQUEST):**
```json
{
  "error": "BAD_REQUEST",
  "message": "Malformed request body. Please check your JSON syntax.",
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/run"
}
```

---

### ❌ **8. Empty Request Body**

```bash
curl -i -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Expected Response (400 BAD REQUEST):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "ticker",
      "issue": "Ticker is required"
    },
    {
      "field": "period",
      "issue": "Period is required"
    }
  ],
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/run"
}
```

---

### ❌ **9. Endpoint Not Found (404)**

```bash
curl -i http://localhost:8001/api/analysis/invalid-endpoint
```

**Expected Response (404 NOT FOUND):**
```json
{
  "error": "NOT_FOUND",
  "message": "Endpoint not found: GET /api/analysis/invalid-endpoint",
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/invalid-endpoint"
}
```

---

### ❌ **10. Method Not Allowed (405)**

```bash
curl -i -X DELETE http://localhost:8001/api/analysis/run
```

**Expected Response (405 METHOD NOT ALLOWED):**
```json
{
  "error": "METHOD_NOT_ALLOWED",
  "message": "HTTP method DELETE is not supported for this endpoint. Supported methods: POST",
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/run"
}
```

---

### ❌ **11. Invalid UUID Format**

```bash
curl -i http://localhost:8001/api/analysis/invalid-uuid/status
```

**Expected Response (400 BAD REQUEST):**
```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid or unknown analysis ID",
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/invalid-uuid/status"
}
```

---

### ❌ **12. Analysis Not Found**

```bash
curl -i http://localhost:8001/api/analysis/550e8400-0000-0000-0000-000000000000/status
```

**Expected Response (404 NOT FOUND):**
```json
{
  "error": "NOT_FOUND",
  "message": "Analysis job not found: 550e8400-0000-0000-0000-000000000000",
  "timestamp": "2026-02-11T20:00:00",
  "path": "/api/analysis/550e8400-0000-0000-0000-000000000000/status"
}
```

---

## Valid Ticker Examples

### Standard Tickers
```bash
# Single letter
curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "T", "period": "1Y"}'

# Standard
curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "MSFT", "period": "1Y"}'

# With dot (Berkshire Hathaway Class B)
curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "BRK.B", "period": "1Y"}'

# With dash
curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "BRK-A", "period": "1Y"}'
```

---

## Valid Period Examples

```bash
# Case-insensitive - all these work:
curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "AAPL", "period": "1M"}'

curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "AAPL", "period": "1m"}'

curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "AAPL", "period": "3M"}'

curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "AAPL", "period": "6M"}'

curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "AAPL", "period": "1Y"}'

curl -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker": "AAPL", "period": "5Y"}'
```

---

## Automated Test Script

```bash
#!/bin/bash

echo "Testing MarketLens API Validation..."

# Test 1: Valid request
echo -e "\n1. Valid request (AAPL, 6M):"
curl -s -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker":"AAPL","period":"6M"}' | jq -r '.status // .error'

# Test 2: Invalid ticker (lowercase)
echo -e "\n2. Invalid ticker (lowercase):"
curl -s -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker":"aapl","period":"6M"}' | jq -r '.error'

# Test 3: Invalid period
echo -e "\n3. Invalid period (2M):"
curl -s -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker":"AAPL","period":"2M"}' | jq -r '.error'

# Test 4: Missing ticker
echo -e "\n4. Missing ticker:"
curl -s -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"period":"6M"}' | jq -r '.error'

# Test 5: Multiple errors
echo -e "\n5. Multiple validation errors:"
curl -s -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{"ticker":"aapl","period":"invalid"}' | jq '.details | length' 2>/dev/null || echo "N/A"

# Test 6: Method not allowed
echo -e "\n6. Method not allowed (DELETE):"
curl -s -X DELETE http://localhost:8001/api/analysis/run | jq -r '.error'

# Test 7: Endpoint not found
echo -e "\n7. Endpoint not found:"
curl -s http://localhost:8001/api/invalid | jq -r '.error'

echo -e "\n✅ All tests complete!"
```

Save this as `test-validation.sh`, make it executable with `chmod +x test-validation.sh`, and run it!

---

## Frontend Integration Example

### React with Error Handling

```typescript
interface ApiErrorResponse {
  error: string;
  message: string;
  details?: Array<{ field: string; issue: string }>;
  timestamp: string;
  path: string;
}

const startAnalysis = async (ticker: string, period: string) => {
  try {
    const response = await fetch('http://localhost:8001/api/analysis/run', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ticker, period })
    });

    if (!response.ok) {
      const error: ApiErrorResponse = await response.json();
      
      if (error.error === 'VALIDATION_ERROR' && error.details) {
        // Show field-level errors
        error.details.forEach(detail => {
          console.error(`${detail.field}: ${detail.issue}`);
          // Show in UI: setFieldError(detail.field, detail.issue)
        });
      } else {
        // Show general error message
        console.error(error.message);
      }
      
      throw new Error(error.message);
    }

    const data = await response.json();
    return data.analysisId;
    
  } catch (error) {
    console.error('Failed to start analysis:', error);
    throw error;
  }
};
```

---

## Summary

✅ **Validation enforced** on POST /api/analysis/run  
✅ **Consistent error format** across all endpoints  
✅ **Field-level details** for validation errors  
✅ **Machine-readable error codes** (VALIDATION_ERROR, NOT_FOUND, etc.)  
✅ **Handles all HTTP error scenarios** (400, 404, 405, 500)  
✅ **Production-ready** error handling  

**Your API now has enterprise-grade validation!** 🎉
