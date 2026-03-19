# MarketLens

**AI-powered investment analytics platform** — analyze stocks and portfolios, model risk, simulate market scenarios, and receive LLM-generated investment insights through a single, unified interface.

🔗 **Live Demo:** [investment-analytics-nu.vercel.app](https://investment-analytics-nu.vercel.app)

---

## What It Does

MarketLens transforms raw market data into institutional-grade investment intelligence. Enter a ticker or build a portfolio, and the platform runs a full quantitative pipeline — computing risk metrics, scoring diversification, and generating a plain-language AI insight that tells you not just *what* the numbers are, but *what to do about them*.

```
Market Data → Financial Metrics → Risk Modeling → AI Insights → Decision
```

**Example output:**
```
Risk Level:       High
Diversification:  Low
Suggested Action: Reduce exposure / hedge

"This stock shows elevated downside risk with weak sentiment and bearish trend,
indicating caution for short-term investors."
```

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

## Current Features

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

## Application Structure

The platform is organized into five analytical tabs:

| Tab | Purpose | Key Outputs |
|---|---|---|
| **Overview** | Stock snapshot | Price, period returns, summary |
| **Risk** | Deep risk analysis | Volatility, drawdown, VaR, Beta vs S&P 500 |
| **AI Insights** | Human-readable intelligence | LLM narrative, risk level, recommendation |
| **Portfolio** | Multi-stock analytics | Combined return, risk, diversification score |
| **Simulation** | Scenario modeling | Portfolio impact under market crash / rate shock |

---

## Roadmap

The platform is actively being extended. Each item below is a discrete engineering feature with a defined input/output contract.

### Portfolio Builder
Add multiple tickers with custom weights, store portfolios, and compute aggregated metrics — combined return, portfolio-level risk, and weighted exposure breakdown.

### Hidden Risk Score
A composite 0–100 risk indicator combining volatility, cross-asset correlation, and sentiment signal strength into a single actionable number with color-coded severity.

```
Risk Score = 0.4 × Volatility + 0.3 × Correlation + 0.3 × Sentiment
Output: Risk Score 78/100 → High Risk
```

### Drawdown Story
Transform raw drawdown numbers into a ranked narrative of historical crashes — depth, start date, trough date, and recovery time in trading days.

```
Max Drawdown:   -32%
Recovery Time:  210 trading days
```

### Smart Alerts Engine
Event-driven alerts that fire on behavioral signals, not price levels. Triggers include volatility spikes above rolling threshold, trend deviation beyond a sigma band, and portfolio correlation breaching 0.80.

### Correlation Heatmap
Visual pairwise correlation matrix across all portfolio holdings. Pairs with |correlation| > 0.80 are flagged automatically — surfaces hidden concentration risk that return data alone cannot reveal.

```
AAPL ↔ MSFT = 0.85 → Low diversification benefit
```

### Simulation Engine
Stress-test portfolios against historical and hypothetical scenarios — market crash (−10%), sector-specific drops, interest rate shocks. Output: projected portfolio impact as a percentage loss.

### Signature Scoring Metrics
Four proprietary scores that differentiate the platform from standard dashboards:

| Score | Definition |
|---|---|
| **Stability Score** | Low volatility + low drawdown = high stability |
| **Momentum Strength** | Trend consistency across 20/50/200-day windows |
| **Sentiment Confidence** | Reliability of sentiment signals based on volume + source agreement |
| **Diversification Score** | Derived from pairwise correlation matrix across portfolio assets |

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
