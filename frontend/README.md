# MarketLens - Investment Analytics Platform

![MarketLens](https://img.shields.io/badge/MarketLens-Investment%20Analytics-blue)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![Tailwind](https://img.shields.io/badge/Tailwind-CSS-38B2AC?logo=tailwind-css)

## 🎯 Overview

MarketLens is a professional investment analytics platform that provides one-click analysis for stocks. Generate comprehensive analytics including returns, volatility, drawdown, and RSI in under 30 seconds.

### ✨ Features

- **30-Second Analysis**: Generate professional investment analytics instantly
- **Daily Updates**: Analytics refresh automatically every day with current data
- **Preset Tickers**: Quick access to AAPL, MSFT, TSLA, SPY, NVDA
- **Comprehensive Metrics**: Returns, volatility, drawdown, RSI with visual charts
- **Interactive Dashboard**: 4 KPI cards + 4 detailed charts
- **Methodology Section**: Transparent formulas and calculations
- **JSON Export**: Download complete analysis results
- **Case Study**: Detailed project documentation

## 🚀 Live Demo

[View Live Demo](#) ← Add your deployed URL here

## 📸 Screenshots

### Landing Page
Clean, professional interface with preset ticker buttons and features overview.

### Analytics Dashboard
Real-time charts showing price movements, returns, volatility, and RSI indicators.

### Case Study
Comprehensive documentation of the problem, solution, architecture, and impact.

## 🛠️ Technology Stack

- **Frontend**: React 19, React Router
- **UI Components**: Shadcn UI, Lucide Icons
- **Styling**: Tailwind CSS
- **Charts**: Recharts
- **HTTP Client**: Axios

## 📦 Installation

### Prerequisites
- Node.js 18+ 
- Yarn or npm

### Local Development

```bash
# Clone the repository
git clone <your-repo-url>
cd marketlens

# Navigate to frontend
cd frontend

# Install dependencies
yarn install

# Start development server
yarn start

# Open http://localhost:3000
```

### Build for Production

```bash
# Create production build
yarn build

# Serve locally to test
npx serve -s build
```

## 🌐 Deployment

### Deploy to Vercel

[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new/clone)

```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
cd frontend
vercel --prod
```

### Deploy to Netlify

[![Deploy to Netlify](https://www.netlify.com/img/deploy/button.svg)](https://app.netlify.com/start)

```bash
# Install Netlify CLI
npm install -g netlify-cli

# Build and deploy
cd frontend
yarn build
netlify deploy --prod --dir=build
```

### Environment Variables

No environment variables required for demo mode. The app uses mock data for analytics.

If integrating with a backend:
```bash
REACT_APP_BACKEND_URL=https://your-backend-api.com
```

## 📊 Analytics Methodology

### Daily Returns
```
return_t = (P_t / P_{t-1}) - 1
```
Percentage change in price from one day to the next.

### Cumulative Return
```
cumulative_return = (P_end / P_start) - 1
```
Total return over the entire period.

### Rolling 30-Day Volatility
```
σ = √(Σ(r_i - μ)² / n) × √252
```
Annualized standard deviation of returns over a 30-day rolling window.

### Maximum Drawdown
```
max_drawdown = min((P_t / peak_t) - 1)
```
Largest peak-to-trough decline in portfolio value.

### RSI (14-period)
```
RS = avgGain / avgLoss
RSI = 100 - (100 / (1 + RS))
```
Relative Strength Index using Wilder smoothing. RSI ≥ 70 indicates overbought, RSI ≤ 30 indicates oversold.

## 🎨 Design Principles

- **Clean & Professional**: Finance-focused aesthetic
- **High Contrast**: Optimal readability
- **Minimal Colors**: Black, gray, and accent colors only
- **Responsive**: Mobile-first design approach
- **Accessible**: WCAG compliant with proper contrast ratios

## 📁 Project Structure

```
frontend/
├── public/
│   ├── index.html
│   └── favicon.ico
├── src/
│   ├── components/
│   │   ├── ui/              # Shadcn UI components
│   │   ├── LandingPage.jsx  # Home page
│   │   ├── Dashboard.jsx    # Analytics dashboard
│   │   └── CaseStudy.jsx    # Case study page
│   ├── data/
│   │   └── mockData.js      # Demo data generator
│   ├── hooks/
│   │   └── use-toast.js     # Toast notifications
│   ├── App.js               # Main app component
│   ├── App.css              # Custom styles
│   └── index.css            # Tailwind + base styles
├── package.json
├── tailwind.config.js
├── vercel.json              # Vercel config
└── netlify.toml             # Netlify config
```

## 🧪 Testing

```bash
# Run tests (if configured)
yarn test

# Run linting
yarn lint
```

## 🔧 Available Scripts

- `yarn start` - Start development server
- `yarn build` - Create production build
- `yarn test` - Run tests
- `yarn eject` - Eject from Create React App (one-way operation)

## 📈 Features in Demo Mode

✅ Preset ticker buttons (AAPL, MSFT, TSLA, SPY, NVDA)
✅ One-click analysis generation
✅ **Daily automatic data refresh** (analytics update every day)
✅ 4 KPI cards with key metrics
✅ Interactive price chart
✅ Daily returns visualization
✅ Rolling volatility chart
✅ RSI with overbought/oversold indicators
✅ Methodology section with formulas
✅ JSON export functionality
✅ Responsive design
✅ Case study documentation

## 🎯 Use Cases

- **AWM Teams**: Quick, consistent analytics for client presentations
- **Retail Investors**: Professional-grade analysis without spreadsheets
- **Financial Analysts**: Standardized metrics and visualizations
- **Portfolio Managers**: Fast comparison across multiple tickers
- **Students**: Learn investment analytics with real formulas

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- Built with [Create React App](https://create-react-app.dev/)
- UI components from [Shadcn UI](https://ui.shadcn.com/)
- Icons from [Lucide](https://lucide.dev/)
- Charts powered by [Recharts](https://recharts.org/)
- Design inspiration from modern fintech platforms

## 📞 Contact

For questions or feedback, please open an issue on GitHub.

---

**Note**: This demo version uses mock data for analytics. All calculations are performed client-side using realistic formulas. For production use with real market data, integrate with the Java Spring Boot backend (see `/backend-java` directory).

## 🚀 Quick Start Guide

1. **Try the Demo**: Visit the live site and click "Try Demo (AAPL)"
2. **Explore Preset Tickers**: Click any preset button (MSFT, TSLA, SPY, NVDA)
3. **View Analytics**: See comprehensive charts and metrics on the dashboard
4. **Read Methodology**: Understand the calculations behind each metric
5. **Export Data**: Download complete analysis as JSON
6. **Learn More**: Check out the case study for project details

---

Built with ❤️ for the investment community