# MarketLens API Contracts & Integration Plan

## Overview
This document outlines the API contracts between React frontend and Java Spring Boot backend, along with the integration strategy.

## Current Mock Data (Frontend)
Location: `/app/frontend/src/data/mockData.js`

### Mocked Data Structure:
```javascript
{
  ticker: "AAPL",
  period: "1 Year",
  generatedAt: "2025-01-28T...",
  priceData: [{ date, close, timestamp }],
  analytics: {
    cumulativeReturn: 0.2038,
    maxDrawdown: -0.0898,
    latestVolatility: 0.1284,
    latestRSI: 20.26
  },
  charts: {
    prices: [{ date, price }],
    returns: [{ date, return }],
    volatility: [{ date, volatility }],
    rsi: [{ date, rsi }]
  }
}
```

## Backend API Endpoints

### 1. POST /api/analysis/run
**Purpose:** Trigger analysis for a ticker (async processing)

**Request:**
```json
{
  "ticker": "AAPL",
  "period": "1Y"
}
```

**Response (Immediate):**
```json
{
  "analysisId": "uuid-string",
  "ticker": "AAPL",
  "status": "PROCESSING",
  "message": "Analysis job started"
}
```

### 2. GET /api/analysis/{analysisId}
**Purpose:** Get analysis results (check status and retrieve data)

**Response (Processing):**
```json
{
  "analysisId": "uuid",
  "ticker": "AAPL",
  "status": "PROCESSING",
  "progress": 50
}
```

**Response (Complete):**
```json
{
  "analysisId": "uuid",
  "ticker": "AAPL",
  "status": "COMPLETED",
  "generatedAt": "2025-01-28T...",
  "analytics": {
    "cumulativeReturn": 0.2038,
    "maxDrawdown": -0.0898,
    "latestVolatility": 0.1284,
    "latestRSI": 20.26
  },
  "charts": {
    "prices": [...],
    "returns": [...],
    "volatility": [...],
    "rsi": [...]
  }
}
```

### 3. GET /api/analysis/ticker/{ticker}/latest
**Purpose:** Get latest analysis for a ticker (quick access for preset buttons)

**Response:** Same as analysis/{analysisId} complete response

### 4. GET /api/tickers/presets
**Purpose:** Get list of preset tickers

**Response:**
```json
{
  "tickers": [
    { "symbol": "AAPL", "name": "Apple Inc." },
    { "symbol": "MSFT", "name": "Microsoft Corp." },
    { "symbol": "TSLA", "name": "Tesla Inc." },
    { "symbol": "SPY", "name": "S&P 500 ETF" },
    { "symbol": "NVDA", "name": "NVIDIA Corp." }
  ]
}
```

## Backend Technology Stack

### Core Framework
- **Java 17+**
- **Spring Boot 3.x**
- **Spring Web** (REST API)
- **Spring Data JPA** (PostgreSQL)
- **Spring Data MongoDB**
- **Spring Async** (@Async, @EnableAsync)

### Databases
1. **PostgreSQL** - Stores analysis metadata
   - Table: `analysis_runs`
   - Columns: id, ticker, status, created_at, updated_at, completed_at

2. **MongoDB** - Stores analysis results (JSON documents)
   - Collection: `analysis_results`
   - Documents: Full analysis results with charts

### External API
- **Yahoo Finance API** via Java HTTP Client
- Endpoint: `https://query1.finance.yahoo.com/v8/finance/chart/{ticker}`
- Parameters: `interval=1d`, `range=1y`

## Analytics Calculations (Backend)

### 1. Daily Returns
```
return_t = (price_t / price_{t-1}) - 1
```

### 2. Cumulative Return
```
cumulative_return = (price_end / price_start) - 1
```

### 3. Rolling 30-Day Volatility
```
volatility = sqrt(sum((return_i - mean)^2) / n) * sqrt(252)
```
- Rolling window: 30 days
- Annualized using sqrt(252)

### 4. Maximum Drawdown
```
drawdown_t = (price_t / peak_t) - 1
max_drawdown = min(all_drawdowns)
```

### 5. RSI (14-period Wilder Smoothing)
```
RS = avgGain / avgLoss
RSI = 100 - (100 / (1 + RS))
```
- Flags: RSI >= 70 (Overbought), RSI <= 30 (Oversold)

## Frontend Integration Steps

### Phase 1: Update API Service
Create `/app/frontend/src/services/api.js`:
```javascript
const API_BASE = `${process.env.REACT_APP_BACKEND_URL}/api`;

export const runAnalysis = async (ticker, period = '1Y') => {
  const response = await axios.post(`${API_BASE}/analysis/run`, { ticker, period });
  return response.data;
};

export const getAnalysis = async (analysisId) => {
  const response = await axios.get(`${API_BASE}/analysis/${analysisId}`);
  return response.data;
};

export const getLatestAnalysis = async (ticker) => {
  const response = await axios.get(`${API_BASE}/analysis/ticker/${ticker}/latest`);
  return response.data;
};
```

### Phase 2: Update Components
1. **LandingPage.jsx**: Call `runAnalysis()` instead of `generateMockAnalysis()`
2. **Dashboard.jsx**: Poll `getAnalysis()` if status is PROCESSING
3. **App.js**: Remove mock data imports, use API service

### Phase 3: Remove Mock Data
Delete or comment out `/app/frontend/src/data/mockData.js`

## Async Processing Architecture

### Backend Flow:
1. Client calls POST /api/analysis/run
2. Controller creates AnalysisRun record in PostgreSQL (status: PROCESSING)
3. Controller submits async job to thread pool (@Async)
4. Controller returns immediately with analysisId
5. Background worker:
   - Fetches data from Yahoo Finance API
   - Calculates analytics
   - Stores results in MongoDB
   - Updates PostgreSQL status to COMPLETED
6. Client polls GET /api/analysis/{analysisId} until status is COMPLETED

### Thread Pool Configuration:
```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("analysis-");
        executor.initialize();
        return executor;
    }
}
```

## Error Handling

### Backend Errors:
- Invalid ticker: Return 400 with error message
- API failure: Return 503 with retry message
- Processing failure: Update status to FAILED in PostgreSQL

### Frontend Handling:
- Show error toast for API failures
- Retry button for failed analyses
- Timeout after 2 minutes of polling

## Testing Strategy

### Backend Testing:
1. Unit tests for analytics calculations
2. Integration tests for API endpoints
3. Mock Yahoo Finance API responses

### Frontend Testing:
1. Test API integration with backend
2. Test polling mechanism
3. Test error scenarios

## Deployment Notes

### Backend:
- Dockerize Spring Boot application
- Environment variables: DB URLs, Yahoo Finance endpoint
- Deploy to Render/Fly.io

### Frontend:
- Build with `yarn build`
- Deploy to Vercel/Netlify
- Configure REACT_APP_BACKEND_URL

### Docker Compose (Local Dev):
```yaml
services:
  postgres:
    image: postgres:15
  mongodb:
    image: mongo:7
  backend:
    build: ./backend
    ports:
      - "8001:8001"
  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
```
