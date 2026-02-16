# 🔄 Async Job Polling API - Complete Guide

## API Endpoints

### 1. **POST /api/analysis/run** - Start Analysis Job
Start a new async analysis job.

**Returns:** `201 CREATED` with job ID

### 2. **GET /api/analysis/{id}/status** - Poll Job Status (NEW)
Poll the current status of a job. **ALWAYS returns 200 if job exists.**

**Returns:**
- `200 OK` - Job exists (any status: PROCESSING, COMPLETED, FAILED)
- `404 NOT FOUND` - Job doesn't exist

### 3. **GET /api/analysis/{id}** - Get Analysis Result
Get the full analysis result. HTTP status code indicates job state.

**Returns:**
- `202 ACCEPTED` - Job still processing (poll again later)
- `200 OK` - Job completed successfully (full results)
- `500 INTERNAL SERVER ERROR` - Job failed
- `404 NOT FOUND` - Job doesn't exist

### 4. **GET /api/analysis/ticker/{ticker}/latest** - Get Latest Completed
Get the latest completed analysis for a ticker.

**Returns:**
- `200 OK` - Latest completed analysis found
- `404 NOT FOUND` - No completed analysis for this ticker

---

## Thread Safety

**Storage:** AnalysisRun jobs are persisted in PostgreSQL (JPA entity)

**Concurrency:**
- ✅ Spring Data JPA provides ACID transactions
- ✅ Database-level isolation prevents race conditions
- ✅ Multiple clients can safely poll the same job
- ✅ Async processing updates are atomic

**Future Enhancement:**
For high-traffic scenarios, consider adding Redis cache layer:
```java
// Future: Redis cache for hot jobs (optional)
@Cacheable(value = "analysis-status", key = "#analysisId")
public Optional<AnalysisRun> getAnalysisRun(UUID analysisId) { ... }
```

---

## Complete Testing Workflow

### Step 1: Start Analysis Job

```bash
# Start analysis for AAPL
RESPONSE=$(curl -s -X POST http://localhost:8001/api/analysis/run \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "period": "6mo"
  }')

echo "$RESPONSE" | jq

# Extract analysisId for next steps
ANALYSIS_ID=$(echo "$RESPONSE" | jq -r '.analysisId')
echo "Analysis ID: $ANALYSIS_ID"
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

### Step 2: Poll Job Status (Recommended Method)

Use the **dedicated status endpoint** for polling:

```bash
# Poll status (lightweight endpoint)
curl -s http://localhost:8001/api/analysis/$ANALYSIS_ID/status | jq
```

**Response while PROCESSING (200 OK):**
```json
{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "ticker": "AAPL",
  "period": "6mo",
  "status": "PROCESSING",
  "progress": 45,
  "message": "Analysis in progress (45% complete)",
  "createdAt": "2026-02-11T20:00:00",
  "updatedAt": "2026-02-11T20:00:15",
  "completedAt": null,
  "errorMessage": null
}
```

**Response when COMPLETED (200 OK):**
```json
{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "ticker": "AAPL",
  "period": "6mo",
  "status": "COMPLETED",
  "progress": 100,
  "message": "Analysis completed successfully",
  "createdAt": "2026-02-11T20:00:00",
  "updatedAt": "2026-02-11T20:00:30",
  "completedAt": "2026-02-11T20:00:30",
  "errorMessage": null
}
```

**Response if FAILED (200 OK - check status field):**
```json
{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "ticker": "INVALID",
  "period": "6mo",
  "status": "FAILED",
  "progress": 20,
  "message": "Analysis failed: Stock ticker not found",
  "createdAt": "2026-02-11T20:00:00",
  "updatedAt": "2026-02-11T20:00:10",
  "completedAt": "2026-02-11T20:00:10",
  "errorMessage": "Stock ticker not found"
}
```

---

### Step 3: Get Full Analysis Result

Once status shows `COMPLETED`, fetch the full result:

```bash
# Get full analysis result
curl -i http://localhost:8001/api/analysis/$ANALYSIS_ID
```

**Response if PROCESSING (202 ACCEPTED):**
```http
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "ticker": "AAPL",
  "period": "6mo",
  "status": "PROCESSING",
  "progress": 45,
  "message": "Analysis in progress (45% complete)",
  ...
}
```

**Response if COMPLETED (200 OK):**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "ticker": "AAPL",
  "status": "COMPLETED",
  "period": "6mo",
  "generatedAt": "2026-02-11T20:00:30",
  "analytics": {
    "cumulativeReturn": 0.15,
    "maxDrawdown": -0.08,
    "latestVolatility": 0.25,
    "latestRSI": 65.3
  },
  "charts": {
    "prices": [...],
    "returns": [...],
    "volatility": [...],
    "rsi": [...]
  }
}
```

**Response if FAILED (500 INTERNAL SERVER ERROR):**
```http
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "analysisId": "550e8400-e29b-41d4-a716-446655440000",
  "ticker": "INVALID",
  "status": "FAILED",
  "progress": 20,
  "message": "Analysis failed: Stock ticker not found",
  "errorMessage": "Stock ticker not found",
  ...
}
```

**Response if NOT FOUND (404 NOT FOUND):**
```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error": "Analysis job not found: invalid-uuid-here"
}
```

---

## Polling Best Practices

### ✅ Recommended: Use Status Endpoint

```bash
# Poll every 2 seconds until completed
while true; do
  STATUS=$(curl -s http://localhost:8001/api/analysis/$ANALYSIS_ID/status | jq -r '.status')
  echo "Status: $STATUS"
  
  if [ "$STATUS" == "COMPLETED" ] || [ "$STATUS" == "FAILED" ]; then
    echo "Job finished!"
    break
  fi
  
  sleep 2
done

# Now fetch full result
curl http://localhost:8001/api/analysis/$ANALYSIS_ID | jq
```

### ⚠️ Alternative: Use Main Endpoint with HTTP Status Codes

```bash
# Poll using main endpoint (checks HTTP status code)
while true; do
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8001/api/analysis/$ANALYSIS_ID)
  echo "HTTP Code: $HTTP_CODE"
  
  if [ "$HTTP_CODE" == "200" ] || [ "$HTTP_CODE" == "500" ]; then
    echo "Job finished!"
    curl http://localhost:8001/api/analysis/$ANALYSIS_ID | jq
    break
  elif [ "$HTTP_CODE" == "202" ]; then
    echo "Still processing..."
  fi
  
  sleep 2
done
```

---

## Error Scenarios

### Invalid UUID Format
```bash
curl -i http://localhost:8001/api/analysis/invalid-uuid/status
```

**Response (400 BAD REQUEST via GlobalExceptionHandler):**
```json
{
  "timestamp": "2026-02-11T20:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid analysisId (must be UUID)",
  "path": "/api/analysis/invalid-uuid/status"
}
```

### Job Not Found
```bash
curl -i http://localhost:8001/api/analysis/550e8400-0000-0000-0000-000000000000/status
```

**Response (404 NOT FOUND):**
```http
HTTP/1.1 404 Not Found
```

---

## Integration with Frontend

### React Example with Polling

```javascript
// Start analysis
const startAnalysis = async (ticker, period) => {
  const response = await fetch('http://localhost:8001/api/analysis/run', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ticker, period })
  });
  
  const data = await response.json();
  return data.analysisId;
};

// Poll status until complete
const pollStatus = async (analysisId) => {
  const poll = async () => {
    const response = await fetch(
      `http://localhost:8001/api/analysis/${analysisId}/status`
    );
    
    const status = await response.json();
    console.log(`Status: ${status.status} (${status.progress}%)`);
    
    if (status.status === 'COMPLETED') {
      return await getFullResult(analysisId);
    } else if (status.status === 'FAILED') {
      throw new Error(status.errorMessage);
    }
    
    // Still processing, poll again in 2 seconds
    await new Promise(resolve => setTimeout(resolve, 2000));
    return poll();
  };
  
  return poll();
};

// Get full result
const getFullResult = async (analysisId) => {
  const response = await fetch(
    `http://localhost:8001/api/analysis/${analysisId}`
  );
  
  if (response.status === 200) {
    return await response.json();
  } else if (response.status === 500) {
    const error = await response.json();
    throw new Error(error.errorMessage);
  }
};

// Usage
const runAnalysis = async () => {
  try {
    const analysisId = await startAnalysis('AAPL', '6mo');
    const result = await pollStatus(analysisId);
    console.log('Analysis complete:', result);
  } catch (error) {
    console.error('Analysis failed:', error);
  }
};
```

---

## HTTP Status Code Summary

| Endpoint | Status Code | Meaning |
|----------|-------------|---------|
| `POST /api/analysis/run` | `201` | Job created successfully |
| `GET /{id}/status` | `200` | Job exists (any state) |
| `GET /{id}/status` | `404` | Job not found |
| `GET /{id}` | `202` | Job processing (poll again) |
| `GET /{id}` | `200` | Job completed (results ready) |
| `GET /{id}` | `500` | Job failed permanently |
| `GET /{id}` | `404` | Job not found |

---

## Performance Notes

**Database Queries:**
- `/status` endpoint: 1 SELECT query (lightweight)
- `/{id}` endpoint (COMPLETED): 2 SELECT queries (AnalysisRun + AnalysisResult)

**Optimization Tips:**
1. Use `/status` for polling (lighter weight)
2. Only fetch full result once status shows COMPLETED
3. Implement exponential backoff for long-running jobs
4. Frontend: Use WebSocket/SSE for real-time updates (future enhancement)

---

## ✅ Ready to Test!

```bash
# Quick test script
cd ~/Downloads/Investment-Analytics/backend-java

# Make sure backend is running
~/maven/bin/mvn spring-boot:run

# In another terminal, run the full workflow above
```

**Your async polling API is now production-ready!** 🎉
