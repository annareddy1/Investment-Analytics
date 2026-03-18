import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TrendingUp, BarChart3, Activity, Zap, Database, Shield } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';
import { getPresetTickers } from '../services/api';

const PERIOD_OPTIONS = ['1M', '3M', '6M', '1Y', '5Y'];

const LandingPage = ({ onRunDemo, isLoading }) => {
  const navigate = useNavigate();

  const [presetTickers, setPresetTickers] = useState([]);
  const [isLoadingTickers, setIsLoadingTickers] = useState(true);

  const [tickerInput, setTickerInput] = useState('AAPL');
  const [selectedPeriod, setSelectedPeriod] = useState('1Y');

  useEffect(() => {
    const loadPresetTickers = async () => {
      try {
        const tickers = await getPresetTickers();
        setPresetTickers(tickers);
      } catch (error) {
        console.error('Failed to load preset tickers:', error);
        setPresetTickers([
          { symbol: 'AAPL', name: 'Apple Inc.' },
          { symbol: 'MSFT', name: 'Microsoft Corp.' },
          { symbol: 'TSLA', name: 'Tesla Inc.' },
          { symbol: 'SPY', name: 'S&P 500 ETF' },
          { symbol: 'NVDA', name: 'NVIDIA Corp.' }
        ]);
      } finally {
        setIsLoadingTickers(false);
      }
    };

    loadPresetTickers();
  }, []);

  const handleTryDemo = () => {
    onRunDemo('AAPL', '1Y', navigate);
  };

  const handlePresetTicker = (ticker) => {
    setTickerInput(ticker);
    onRunDemo(ticker, selectedPeriod, navigate);
  };

  const handleAnalyze = () => {
    const normalizedTicker = tickerInput.trim().toUpperCase();

    if (!normalizedTicker) {
      return;
    }

    onRunDemo(normalizedTicker, selectedPeriod, navigate);
  };

  const features = [
    {
      icon: <Zap className="w-8 h-8" />,
      title: '30-Second Analysis',
      description: 'Generate professional investment analytics in under 30 seconds with one click'
    },
    {
      icon: <BarChart3 className="w-8 h-8" />,
      title: 'Comprehensive Metrics',
      description: 'Returns, volatility, drawdown, and RSI with visual charts and KPI cards'
    },
    {
      icon: <Activity className="w-8 h-8" />,
      title: 'Real-Time Data',
      description: 'Live market data integration with Yahoo Finance API for accurate analysis'
    },
    {
      icon: <Database className="w-8 h-8" />,
      title: 'Enterprise Stack',
      description: 'Built with Java Spring Boot, PostgreSQL, MongoDB, and React'
    }
  ];

  const techStack = [
    { name: 'React', category: 'Frontend' },
    { name: 'Java Spring Boot', category: 'Backend' },
    { name: 'PostgreSQL', category: 'Database' },
    { name: 'MongoDB Atlas', category: 'Database' },
    { name: 'Recharts', category: 'Visualization' },
    { name: 'Yahoo Finance API', category: 'Data' }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-b from-gray-50 to-white">
      <header className="border-b border-gray-200 bg-white">
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <TrendingUp className="w-8 h-8 text-gray-900" />
              <span className="text-2xl font-bold text-gray-900">MarketLens</span>
            </div>
            <nav className="flex items-center gap-6">
              <a href="#features" className="text-sm font-medium text-gray-600 hover:text-gray-900 transition-colors">Features</a>
              <a href="#case-study" className="text-sm font-medium text-gray-600 hover:text-gray-900 transition-colors">Case Study</a>
              <a href="#stack" className="text-sm font-medium text-gray-600 hover:text-gray-900 transition-colors">Stack</a>
            </nav>
          </div>
        </div>
      </header>

      <section className="py-20">
        <div className="container mx-auto px-6">
          <div className="max-w-4xl mx-auto text-center">
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-gray-100 rounded-full mb-6">
              <Shield className="w-4 h-4 text-gray-600" />
              <span className="text-sm font-medium text-gray-600">Enterprise-Grade Investment Analytics</span>
            </div>

            <h1 className="text-6xl font-bold text-gray-900 mb-6 leading-tight">
              Professional Analytics
              <br />
              <span className="text-gray-600">In 30 Seconds</span>
            </h1>

            <p className="text-xl text-gray-600 mb-12 leading-relaxed max-w-2xl mx-auto">
              MarketLens provides one-click, repeatable investment analytics for any ticker.
              Generate consistent returns, volatility, drawdown, and RSI analysis instantly.
              <span className="block mt-2 text-green-600 font-semibold">Analytics update daily with fresh market data.</span>
            </p>

            <div className="max-w-2xl mx-auto bg-white border border-gray-200 rounded-2xl p-6 shadow-sm mb-10">
              <div className="grid md:grid-cols-3 gap-4 items-end">
                <div className="text-left">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Ticker
                  </label>
                  <input
                    type="text"
                    value={tickerInput}
                    onChange={(e) => setTickerInput(e.target.value.toUpperCase())}
                    placeholder="AAPL"
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-gray-900"
                  />
                </div>

                <div className="text-left">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Period
                  </label>
                  <select
                    value={selectedPeriod}
                    onChange={(e) => setSelectedPeriod(e.target.value)}
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 bg-white focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-gray-900"
                  >
                    {PERIOD_OPTIONS.map((period) => (
                      <option key={period} value={period}>
                        {period}
                      </option>
                    ))}
                  </select>
                </div>

                <Button
                  size="lg"
                  className="w-full bg-gray-900 hover:bg-gray-800"
                  onClick={handleAnalyze}
                  disabled={isLoading || !tickerInput.trim()}
                >
                  <Zap className="w-5 h-5 mr-2" />
                  {isLoading ? 'Analyzing...' : 'Analyze'}
                </Button>
              </div>

              <div className="mt-4 text-sm text-gray-500 text-left">
                Enter any supported ticker symbol and choose a time range for analysis.
              </div>
            </div>

            <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-8">
              <Button
                size="lg"
                className="text-base px-8 py-6 bg-gray-900 hover:bg-gray-800"
                onClick={handleTryDemo}
                disabled={isLoading}
              >
                <Zap className="w-5 h-5 mr-2" />
                {isLoading ? 'Analyzing...' : 'Try Demo (AAPL)'}
              </Button>

              <Button
                size="lg"
                variant="outline"
                className="text-base px-8 py-6 border-gray-300 hover:bg-gray-50"
                onClick={() => navigate('/case-study')}
              >
                View Case Study
              </Button>
            </div>

            <div className="flex flex-wrap items-center justify-center gap-3">
              <span className="text-sm font-medium text-gray-500">Quick analyze:</span>
              {isLoadingTickers ? (
                <span className="text-sm text-gray-500">Loading tickers...</span>
              ) : (
                presetTickers.map((ticker) => (
                  <Button
                    key={ticker.symbol}
                    variant="outline"
                    size="sm"
                    className="border-gray-300 hover:bg-gray-50 hover:border-gray-400"
                    onClick={() => handlePresetTicker(ticker.symbol)}
                  >
                    {ticker.symbol}
                  </Button>
                ))
              )}
            </div>
          </div>
        </div>
      </section>

      <section id="features" className="py-20 bg-gray-50">
        <div className="container mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">Why MarketLens?</h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              Built for users who need fast, consistent, professional analytics
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {features.map((feature, index) => (
              <Card key={index} className="border-gray-200 hover:shadow-lg transition-shadow">
                <CardContent className="pt-6">
                  <div className="flex flex-col items-start gap-4">
                    <div className="p-3 bg-gray-100 rounded-lg text-gray-900">
                      {feature.icon}
                    </div>
                    <h3 className="text-lg font-semibold text-gray-900">{feature.title}</h3>
                    <p className="text-sm text-gray-600 leading-relaxed">{feature.description}</p>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section id="stack" className="py-20">
        <div className="container mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">Technology Stack</h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              Built with modern technologies for reliability and performance
            </p>
          </div>

          <div className="max-w-4xl mx-auto">
            <div className="grid md:grid-cols-3 gap-4">
              {techStack.map((tech, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between p-4 bg-white border border-gray-200 rounded-lg hover:border-gray-300 transition-colors"
                >
                  <span className="font-medium text-gray-900">{tech.name}</span>
                  <span className="text-sm text-gray-500">{tech.category}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="py-20 bg-gray-900 text-white">
        <div className="container mx-auto px-6 text-center">
          <h2 className="text-4xl font-bold mb-6">Ready to try MarketLens?</h2>
          <p className="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
            Run your first analysis in under 30 seconds. No login required.
          </p>
          <Button
            size="lg"
            className="text-base px-8 py-6 bg-white text-gray-900 hover:bg-gray-100"
            onClick={handleTryDemo}
            disabled={isLoading}
          >
            <Zap className="w-5 h-5 mr-2" />
            {isLoading ? 'Analyzing...' : 'Launch Demo'}
          </Button>
        </div>
      </section>

      <footer className="border-t border-gray-200 bg-white py-8">
        <div className="container mx-auto px-6 text-center">
          <div className="flex items-center justify-center gap-2 mb-4">
            <TrendingUp className="w-6 h-6 text-gray-900" />
            <span className="text-lg font-bold text-gray-900">MarketLens</span>
          </div>
          <p className="text-sm text-gray-500">
            © 2025 MarketLens. Professional investment analytics platform.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;