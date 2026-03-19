# MarketLens

**AI-powered stock analysis platform** — enter any ticker, get institutional-grade risk metrics, interactive charts, and LLM-generated investment insights in seconds.

🔗 **Live Demo:** [investment-analytics-nu.vercel.app](https://investment-analytics-nu.vercel.app)

---

## What It Does

MarketLens pulls real market data from Yahoo Finance, runs a full quantitative analytics pipeline, and delivers a plain-language AI summary of the stock's risk profile — the kind of output a junior analyst would spend hours producing, automated end-to-end.

Built to demonstrate full-stack engineering depth in a finance context: from raw OHLCV data ingestion through statistical computation to LLM orchestration and a production React UI.

---

## Technical Architecture

```
React (CRA)  ──POST /api/analysis/run──►  Spring Boot (Java 17)
     │                                          │
     │  poll /api/analysis/{id}                 ├── YahooFinanceService
     │◄─────────────────────────────────────    │     └── HTTP → Yahoo Finance v8 API
     │                                          │
     └──POST /api/analysis/{id}/insight ──────► ├── AnalyticsCalculationService
                                                │     ├── Cumulative return
                                                │     ├── Max drawdown
                                                │     ├── 30-day rolling volatility
                                                │     └── 14-day RSI
                                                │
                                                ├── AiInsightService
                                                │     └── HTTP → Anthropic Claude API
                                                │           └── structured JSON response
                                                │             (narrative, riskLevel,
                                                │              recommendation, action)
                                                │
                                                ├── MongoDB  ← AnalysisResult (documents)
                                                └── PostgreSQL ← AnalysisRun (job state)
```

---

## Features

**Quantitative Analytics Pipeline**
- Cumulative return over configurable periods (1M · 3M · 6M · 1Y · 5Y)
- Max drawdown with peak-to-trough calculation
- 30-day rolling annualised volatility (252 trading days)
- 14-period RSI with overbought/oversold classification

**AI Insight Engine**
- Calls Anthropic Claude with a structured metric payload
- Returns plain-language risk narrative, risk level (Low → Very High), buy/hold/reduce/hedge recommendation, and a concrete suggested action
- Result cached per analysis — no redundant API calls on tab switch

**Production Infrastructure**
- Async job processing with Spring `@Async` executor and progress tracking
- Polling-based status updates (PROCESSING → COMPLETED → insight fetch)
- Dual-database architecture: PostgreSQL for job state, MongoDB for analysis documents
- Deployed on Render (backend) + Vercel (frontend)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React, Axios, React Router |
| Backend | Java 17, Spring Boot 3.2, Maven |
| AI / LLM | Anthropic Claude (`claude-sonnet-4`) |
| Market Data | Yahoo Finance API (v8 chart endpoint) |
| Databases | PostgreSQL (job state) · MongoDB (results) |
| Deployment | Vercel · Render |

---

## Local Setup

**Prerequisites:** Java 17+, Maven, Node.js, MongoDB, PostgreSQL

### Backend
```bash
cd backend-java
# Set ANTHROPIC_API_KEY, MongoDB URI, and PostgreSQL URL in application-dev.properties
mvn spring-boot:run
```
Runs at `http://localhost:8000` · API docs at `http://localhost:8000/docs`

### Frontend
```bash
cd frontend
npm install
# Set REACT_APP_API_BASE_URL=http://localhost:8000 in .env
npm start
```
Runs at `http://localhost:3000`

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/analysis/run` | Start async analysis for a ticker |
| `GET` | `/api/analysis/{id}` | Poll status / fetch completed result |
| `GET` | `/api/analysis/{id}/status` | Lightweight status check |
| `POST` | `/api/analysis/{id}/insight` | Generate LLM insight for completed analysis |
| `GET` | `/api/analysis/ticker/{ticker}/latest` | Fetch most recent completed analysis |
| `GET` | `/api/tickers/presets` | Get preset ticker list |

---

## Engineering Decisions Worth Noting

**Async job processing over synchronous response** — Yahoo Finance fetch + statistical computation + an LLM call in a single blocking request would time out. The pipeline runs on a dedicated Spring executor thread, exposing a progress percentage the frontend polls against — a pattern common in real-time financial data platforms.

**Dual-database architecture** — Analysis runs (job metadata, status, timestamps) are relational by nature: fixed schema, ACID guarantees needed for state transitions. Analysis results (nested price arrays, chart data, analytics objects) are document-shaped and schema-flexible. Using the right store for each concern is a deliberate design decision.

**Structured LLM output** — The insight endpoint prompts Claude to return a strict JSON schema (`narrative`, `riskLevel`, `recommendation`, `suggestedAction`). This keeps frontend rendering deterministic — badges and color coding are driven by typed enum values, not brittle string parsing.

---

## Author

**Rithika Annareddy**

[GitHub](https://github.com/annareddy1) · [LinkedIn](https://linkedin.com/in/rithika-annareddy)
