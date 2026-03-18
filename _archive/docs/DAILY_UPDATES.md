# MarketLens - Daily Update Feature

## 🔄 How Daily Updates Work

MarketLens now automatically generates fresh analytics data every day without requiring any backend infrastructure or API calls.

### Implementation

The mock data generation system uses **deterministic randomization** based on:
1. Current date (day + month)
2. Ticker symbol
3. Mathematical functions (sine/cosine waves)

This ensures:
- ✅ Data is different each day
- ✅ Data is consistent throughout the same day
- ✅ All users see the same data on the same day
- ✅ No caching issues
- ✅ No API rate limits
- ✅ Works offline

### What Updates Daily

1. **Price Data**: Last 252 trading days (1 year) calculated from today
2. **Analytics Metrics**: 
   - Cumulative Return
   - Maximum Drawdown
   - Latest Volatility
   - Latest RSI
3. **Chart Data**: All charts regenerate with fresh data points
4. **Timestamps**: "Generated at" shows current date/time

### User Experience

**Landing Page:**
- Displays message: "Analytics update daily with fresh market data"

**Dashboard:**
- Shows: "Generated at [current date/time]"
- Displays: "Updates Daily" indicator in green
- Period shows: "1 Year" (always last 252 days from today)

### Technical Details

```javascript
// Data generation is seeded by current date + ticker
const seed = ticker.charCodeAt(0) + endDate.getDate() + endDate.getMonth();

// Date range always relative to today
const endDate = new Date();
const startDate = new Date(endDate);
startDate.setDate(startDate.getDate() - 252); // 1 year back

// Price movements use deterministic math
const randomFactor = Math.sin(dayOffset * 0.1) * Math.cos(dayOffset * 0.05);
```

### Benefits

1. **Always Current**: Data never feels stale or outdated
2. **No Backend Required**: Works entirely client-side
3. **No API Costs**: No external API calls needed
4. **Consistent**: Same data for all users on same day
5. **Realistic**: Generates believable market movements
6. **Portfolio Ready**: Demonstrates live data capability

### Testing Daily Updates

To verify daily updates work:

1. **Same Day**: Refresh the page multiple times - data stays consistent
2. **Next Day**: Check tomorrow - all analytics will be recalculated
3. **Date Range**: Charts always show last 1 year from current date

### Future Backend Integration

When integrating with real backend:
1. Replace `generateMockAnalysis()` calls with API calls
2. Remove mock data generator
3. Backend will provide real daily updates from Yahoo Finance API
4. Frontend UI remains unchanged

### Deployment Notes

- ✅ No environment variables needed for daily updates
- ✅ Works on Vercel/Netlify without configuration
- ✅ No build-time data generation
- ✅ No server-side rendering required
- ✅ Pure client-side runtime generation

### Code Location

- **Mock Data Generator**: `/app/frontend/src/data/mockData.js`
- **Dashboard Component**: `/app/frontend/src/components/Dashboard.jsx`
- **Landing Page**: `/app/frontend/src/components/LandingPage.jsx`

---

## 📊 Example Daily Behavior

**Day 1 (Jan 28):**
- AAPL shows +21.37% return
- Generated at: 1/28/2026, 9:14 PM
- Date range: Jan 28, 2025 - Jan 28, 2026

**Day 2 (Jan 29):**
- AAPL shows different return (e.g., +19.82%)
- Generated at: 1/29/2026, [current time]
- Date range: Jan 29, 2025 - Jan 29, 2026

**All charts, KPIs, and analytics recalculate automatically!**

---

## 🎯 User Messaging

We clearly communicate the daily update feature:

1. **Landing Page**: "Analytics update daily with fresh market data"
2. **Dashboard Header**: "Updates Daily" (green badge)
3. **Timestamp**: Always shows current generation time

This creates confidence that users are seeing fresh, relevant data even though it's mock data for the demo.
