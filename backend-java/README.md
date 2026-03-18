# MarketLens Backend - Java Spring Boot

## Overview
Investment analytics platform backend built with Java Spring Boot, featuring async processing, PostgreSQL for metadata, and MongoDB for results storage.

## Technology Stack
- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA** (PostgreSQL)
- **Spring Data MongoDB**
- **Spring Async** (@Async, ThreadPool)
- **Yahoo Finance API** (Java HTTP Client)

## Prerequisites
- Java 17 or higher
- Maven 3.8+
- PostgreSQL 15+
- MongoDB 7+

## Local Development Setup

### Option 1: Using Docker Compose (Recommended)

```bash
# Start all services (PostgreSQL, MongoDB, Backend)
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop all services
docker-compose down
```

### Option 2: Manual Setup

1. **Install Dependencies**
   ```bash
   # Install PostgreSQL 15
   # Install MongoDB 7
   ```

2. **Create Databases**
   ```bash
   # PostgreSQL
   createdb marketlens
   
   # MongoDB (auto-created on first connection)
   ```

3. **Configure Application**
   
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/marketlens
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   
   spring.data.mongodb.uri=mongodb://localhost:27017/marketlens
   ```

4. **Build and Run**
   ```bash
   cd backend-java
   
   # Build
   mvn clean package
   
   # Run
   java -jar target/marketlens-backend-1.0.0.jar
   
   # Or use Maven
   mvn spring-boot:run
   ```

## API Endpoints

### Health Check
```bash
GET /api/
Response: {"message": "MarketLens API is running"}
```

### Run Analysis
```bash
POST /api/analysis/run
Content-Type: application/json

{
  "ticker": "AAPL",
  "period": "1Y"
}

Response: {
  "analysisId": "uuid",
  "ticker": "AAPL",
  "status": "PROCESSING",
  "message": "Analysis job started"
}
```

### Get Analysis Status/Result
```bash
GET /api/analysis/{analysisId}

Response (Processing): {
  "analysisId": "uuid",
  "ticker": "AAPL",
  "status": "PROCESSING",
  "progress": 50
}

Response (Completed): {
  "analysisId": "uuid",
  "ticker": "AAPL",
  "status": "COMPLETED",
  "generatedAt": "2025-01-28T...",
  "analytics": {...},
  "charts": {...}
}
```

### Get Latest Analysis for Ticker
```bash
GET /api/analysis/ticker/{ticker}/latest

Response: { /* Full analysis result */ }
```

### Get Preset Tickers
```bash
GET /api/tickers/presets

Response: {
  "tickers": [
    {"symbol": "AAPL", "name": "Apple Inc."},
    ...
  ]
}
```

## Architecture

### Async Processing Flow
1. Client calls `POST /api/analysis/run`
2. Controller creates `AnalysisRun` record (status: PROCESSING)
3. Controller submits async job to thread pool
4. Controller returns immediately with `analysisId`
5. Background worker:
   - Fetches data from Yahoo Finance
   - Calculates analytics (returns, volatility, drawdown, RSI)
   - Stores results in MongoDB
   - Updates status to COMPLETED
6. Client polls `GET /api/analysis/{analysisId}`

### Database Schema

**PostgreSQL (analysis_runs)**
```sql
CREATE TABLE analysis_runs (
  id UUID PRIMARY KEY,
  ticker VARCHAR(10) NOT NULL,
  period VARCHAR(10) NOT NULL,
  status VARCHAR(20) NOT NULL,
  progress INTEGER,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  completed_at TIMESTAMP,
  error_message TEXT
);
```

**MongoDB (analysis_results)**
```json
{
  "_id": "ObjectId",
  "analysisId": "uuid",
  "ticker": "AAPL",
  "period": "1Y",
  "generatedAt": "ISODate",
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

## Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn clean verify
```

## Deployment

### Docker Deployment
```bash
# Build image
docker build -t marketlens-backend .

# Run container
docker run -p 8001:8001 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/marketlens \
  -e SPRING_DATA_MONGODB_URI=mongodb://host:27017/marketlens \
  marketlens-backend
```

### Cloud Deployment (Render/Fly.io)

1. Set environment variables:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `SPRING_DATA_MONGODB_URI`
   - `CORS_ALLOWED_ORIGINS`

2. Deploy using Docker or JAR file

## Configuration

### Thread Pool Settings
```properties
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
spring.task.execution.pool.queue-capacity=25
```

### CORS Settings
```properties
cors.allowed.origins=http://localhost:3000,https://yourapp.vercel.app
```

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL and MongoDB are running
- Check connection strings in `application.properties`
- Ensure databases are created

### Yahoo Finance API Issues
- Check network connectivity
- Verify ticker symbol is valid
- Yahoo Finance API is unofficial - occasional failures expected

### Async Processing Issues
- Check thread pool configuration
- Review logs for exceptions
- Verify database write permissions

## Development

### Project Structure
```
backend-java/
├── src/main/java/com/marketlens/
│   ├── MarketLensApplication.java
│   ├── config/
│   │   ├── AsyncConfig.java
│   │   └── CorsConfig.java
│   ├── controller/
│   │   ├── AnalysisController.java
│   │   └── TickerController.java
│   ├── dto/
│   │   ├── AnalysisRequest.java
│   │   ├── AnalysisResponse.java
│   │   └── TickerInfo.java
│   ├── model/
│   │   ├── AnalysisRun.java
│   │   └── AnalysisResult.java
│   ├── repository/
│   │   ├── AnalysisRunRepository.java
│   │   └── AnalysisResultRepository.java
│   └── service/
│       ├── AnalysisService.java
│       ├── AnalyticsCalculationService.java
│       └── YahooFinanceService.java
└── src/main/resources/
    └── application.properties
```

## License
MIT