// Mock data for MarketLens demo
// This will be replaced with real backend data later

export const PRESET_TICKERS = [
  { symbol: 'AAPL', name: 'Apple Inc.' },
  { symbol: 'MSFT', name: 'Microsoft Corp.' },
  { symbol: 'TSLA', name: 'Tesla Inc.' },
  { symbol: 'SPY', name: 'S&P 500 ETF' },
  { symbol: 'NVDA', name: 'NVIDIA Corp.' }
];

// Generate mock historical data that updates daily
const generateMockData = (ticker, days = 252) => {
  const data = [];
  const endDate = new Date();
  endDate.setHours(0, 0, 0, 0); // Normalize to start of day
  
  const startDate = new Date(endDate);
  startDate.setDate(startDate.getDate() - days);
  
  // Use ticker and current date to seed random variations (deterministic per day)
  const seed = ticker.charCodeAt(0) + endDate.getDate() + endDate.getMonth();
  let basePrice = ticker === 'AAPL' ? 180 : ticker === 'MSFT' ? 420 : ticker === 'TSLA' ? 280 : ticker === 'SPY' ? 475 : 520;
  let price = basePrice * 0.85; // Start from 85% of current price
  
  for (let i = 0; i < days; i++) {
    const date = new Date(startDate);
    date.setDate(date.getDate() + i);
    
    // Deterministic but realistic price movement
    const dayOffset = i + seed;
    const trend = 0.0008; // Slight upward trend
    const volatility = 0.015;
    const randomFactor = Math.sin(dayOffset * 0.1) * Math.cos(dayOffset * 0.05);
    const change = price * (trend + (randomFactor * volatility));
    
    price = price + change;
    
    // Ensure price doesn't go negative
    if (price < basePrice * 0.5) price = basePrice * 0.5;
    
    data.push({
      date: date.toISOString().split('T')[0],
      close: parseFloat(price.toFixed(2)),
      timestamp: date.getTime()
    });
  }
  
  return data;
};

// Calculate analytics from price data
const calculateAnalytics = (priceData) => {
  const prices = priceData.map(d => d.close);
  
  // Daily returns
  const returns = [];
  for (let i = 1; i < prices.length; i++) {
    returns.push((prices[i] / prices[i - 1]) - 1);
  }
  
  // Cumulative return
  const cumulativeReturn = (prices[prices.length - 1] / prices[0]) - 1;
  
  // Rolling 30-day volatility
  const volatility = [];
  for (let i = 30; i < returns.length; i++) {
    const slice = returns.slice(i - 30, i);
    const mean = slice.reduce((a, b) => a + b, 0) / slice.length;
    const variance = slice.reduce((sum, r) => sum + Math.pow(r - mean, 2), 0) / slice.length;
    const stdDev = Math.sqrt(variance);
    volatility.push(stdDev * Math.sqrt(252)); // Annualized
  }
  
  // Max drawdown
  let maxDrawdown = 0;
  let peak = prices[0];
  for (let i = 1; i < prices.length; i++) {
    if (prices[i] > peak) peak = prices[i];
    const drawdown = (prices[i] / peak) - 1;
    if (drawdown < maxDrawdown) maxDrawdown = drawdown;
  }
  
  // RSI calculation (14-period Wilder smoothing)
  const calculateRSI = (prices, period = 14) => {
    const rsi = [];
    const gains = [];
    const losses = [];
    
    // Calculate price changes
    for (let i = 1; i < prices.length; i++) {
      const change = prices[i] - prices[i - 1];
      gains.push(change > 0 ? change : 0);
      losses.push(change < 0 ? Math.abs(change) : 0);
    }
    
    // Calculate RSI
    for (let i = period; i < gains.length; i++) {
      const avgGain = gains.slice(i - period, i).reduce((a, b) => a + b, 0) / period;
      const avgLoss = losses.slice(i - period, i).reduce((a, b) => a + b, 0) / period;
      
      const rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
      const rsiValue = 100 - (100 / (1 + rs));
      rsi.push(rsiValue);
    }
    
    return rsi;
  };
  
  const rsi = calculateRSI(prices);
  
  return {
    returns,
    volatility,
    cumulativeReturn,
    maxDrawdown,
    rsi,
    latestVolatility: volatility[volatility.length - 1],
    latestRSI: rsi[rsi.length - 1]
  };
};

// Generate mock analysis results
export const generateMockAnalysis = (ticker) => {
  const priceData = generateMockData(ticker);
  const analytics = calculateAnalytics(priceData);
  
  return {
    ticker,
    period: '1 Year',
    generatedAt: new Date().toISOString(),
    priceData: priceData,
    analytics: {
      cumulativeReturn: analytics.cumulativeReturn,
      maxDrawdown: analytics.maxDrawdown,
      latestVolatility: analytics.latestVolatility,
      latestRSI: analytics.latestRSI
    },
    charts: {
      prices: priceData.map((d, i) => ({
        date: d.date,
        price: d.close
      })),
      returns: analytics.returns.map((r, i) => ({
        date: priceData[i + 1].date,
        return: r * 100 // Convert to percentage
      })),
      volatility: analytics.volatility.map((v, i) => ({
        date: priceData[i + 30].date,
        volatility: v * 100 // Convert to percentage
      })),
      rsi: analytics.rsi.map((r, i) => ({
        date: priceData[i + 14].date,
        rsi: r
      }))
    }
  };
};

// Default analysis for "Try Demo" button
export const DEFAULT_ANALYSIS = generateMockAnalysis('AAPL');

// Methodology content
export const METHODOLOGY = {
  dailyReturns: {
    title: 'Daily Returns',
    formula: '(P_t / P_{t-1}) - 1',
    description: 'Percentage change in price from one day to the next'
  },
  cumulativeReturn: {
    title: 'Cumulative Return',
    formula: '(P_end / P_start) - 1',
    description: 'Total return over the entire period'
  },
  volatility: {
    title: 'Rolling 30-Day Volatility',
    formula: 'sigma = sqrt(sum((r_i - mu)^2) / n) * sqrt(252)',
    description: 'Annualized standard deviation of returns over a 30-day rolling window'
  },
  maxDrawdown: {
    title: 'Maximum Drawdown',
    formula: 'min((P_t / peak_t) - 1)',
    description: 'Largest peak-to-trough decline in portfolio value'
  },
  rsi: {
    title: 'RSI (14-period)',
    formula: 'RSI = 100 - (100 / (1 + RS))',
    description: 'Relative Strength Index using Wilder smoothing. RSI >= 70 indicates overbought, RSI <= 30 indicates oversold'
  }
};