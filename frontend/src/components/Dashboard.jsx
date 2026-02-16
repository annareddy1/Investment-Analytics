import React from 'react';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts';
import { TrendingUp, TrendingDown, Activity, AlertTriangle, Download, ArrowLeft } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card';
import { Button } from './ui/button';
import { METHODOLOGY } from '../data/mockData';

const Dashboard = ({ analysisData }) => {
  const navigate = useNavigate();
  const selectedTicker = analysisData || null;

  if (!selectedTicker) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <Activity className="w-16 h-16 mx-auto text-gray-400 mb-4" />
          <h2 className="text-2xl font-bold text-gray-900 mb-2">No Analysis Selected</h2>
          <p className="text-gray-600 mb-6">Please select a ticker from the landing page</p>
          <Button onClick={() => navigate('/')}>Go to Home</Button>
        </div>
      </div>
    );
  }

  const { ticker, analytics, charts, generatedAt } = selectedTicker;

  const downloadJSON = () => {
    const dataStr = JSON.stringify(selectedTicker, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${ticker}_analysis_${new Date().toISOString().split('T')[0]}.json`;
    link.click();
  };

  const formatPercent = (value) => {
    const sign = value >= 0 ? '+' : '';
    return `${sign}${(value * 100).toFixed(2)}%`;
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  };

  const getRSIStatus = (rsi) => {
    if (rsi >= 70) return { status: 'Overbought', color: 'text-orange-600', bg: 'bg-orange-50' };
    if (rsi <= 30) return { status: 'Oversold', color: 'text-blue-600', bg: 'bg-blue-50' };
    return { status: 'Neutral', color: 'text-gray-600', bg: 'bg-gray-50' };
  };

  const rsiStatus = getRSIStatus(analytics.latestRSI);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="border-b border-gray-200 bg-white sticky top-0 z-10">
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button 
                variant="ghost" 
                size="sm"
                onClick={() => navigate('/')}
                className="hover:bg-gray-100"
              >
                <ArrowLeft className="w-4 h-4 mr-2" />
                Back
              </Button>
              <div className="flex items-center gap-2">
                <TrendingUp className="w-6 h-6 text-gray-900" />
                <span className="text-xl font-bold text-gray-900">MarketLens</span>
              </div>
            </div>
            <Button 
              variant="outline" 
              size="sm"
              onClick={downloadJSON}
              className="border-gray-300 hover:bg-gray-50"
            >
              <Download className="w-4 h-4 mr-2" />
              Download JSON
            </Button>
          </div>
        </div>
      </header>

      {/* Dashboard Content */}
      <div className="container mx-auto px-6 py-8">
        {/* Title and Info */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">{ticker} Analysis</h1>
          <div className="flex items-center gap-4 text-gray-600">
            <p>Generated at {new Date(generatedAt).toLocaleString()}</p>
            <span>•</span>
            <p>Period: {selectedTicker.period}</p>
            <span>•</span>
            <p className="text-green-600 font-medium">Updates Daily</p>
          </div>
        </div>

        {/* KPI Cards */}
        <div className="grid md:grid-cols-4 gap-6 mb-8">
          <Card className="border-gray-200">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">Cumulative Return</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                {analytics.cumulativeReturn >= 0 ? (
                  <TrendingUp className="w-5 h-5 text-green-600" />
                ) : (
                  <TrendingDown className="w-5 h-5 text-red-600" />
                )}
                <span className={`text-2xl font-bold ${
                  analytics.cumulativeReturn >= 0 ? 'text-green-600' : 'text-red-600'
                }`}>
                  {formatPercent(analytics.cumulativeReturn)}
                </span>
              </div>
            </CardContent>
          </Card>

          <Card className="border-gray-200">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">Max Drawdown</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <TrendingDown className="w-5 h-5 text-red-600" />
                <span className="text-2xl font-bold text-red-600">
                  {formatPercent(analytics.maxDrawdown)}
                </span>
              </div>
            </CardContent>
          </Card>

          <Card className="border-gray-200">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">Latest Volatility</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Activity className="w-5 h-5 text-gray-600" />
                <span className="text-2xl font-bold text-gray-900">
                  {(analytics.latestVolatility * 100).toFixed(2)}%
                </span>
              </div>
            </CardContent>
          </Card>

          <Card className="border-gray-200">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">RSI (14)</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <AlertTriangle className={`w-5 h-5 ${rsiStatus.color}`} />
                <span className={`text-2xl font-bold ${rsiStatus.color}`}>
                  {analytics.latestRSI.toFixed(2)}
                </span>
              </div>
              <div className={`mt-2 inline-block px-2 py-1 rounded text-xs font-medium ${rsiStatus.bg} ${rsiStatus.color}`}>
                {rsiStatus.status}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Charts Grid */}
        <div className="grid lg:grid-cols-2 gap-6 mb-8">
          {/* Price Chart */}
          <Card className="border-gray-200">
            <CardHeader>
              <CardTitle className="text-lg font-semibold">Price Chart</CardTitle>
              <CardDescription>Historical closing prices over the period</CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={250}>
                <AreaChart data={charts.prices}>
                  <defs>
                    <linearGradient id="colorPrice" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="date" 
                    tickFormatter={formatDate}
                    stroke="#9ca3af"
                    style={{ fontSize: '12px' }}
                  />
                  <YAxis 
                    stroke="#9ca3af"
                    style={{ fontSize: '12px' }}
                    tickFormatter={(value) => `$${value.toFixed(0)}`}
                  />
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '6px' }}
                    formatter={(value) => [`$${value.toFixed(2)}`, 'Price']}
                    labelFormatter={(label) => `Date: ${label}`}
                  />
                  <Area 
                    type="monotone" 
                    dataKey="price" 
                    stroke="#3b82f6" 
                    strokeWidth={2}
                    fillOpacity={1} 
                    fill="url(#colorPrice)" 
                  />
                </AreaChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* Daily Returns Chart */}
          <Card className="border-gray-200">
            <CardHeader>
              <CardTitle className="text-lg font-semibold">Daily Returns</CardTitle>
              <CardDescription>Day-over-day percentage changes</CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={charts.returns}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="date" 
                    tickFormatter={formatDate}
                    stroke="#9ca3af"
                    style={{ fontSize: '12px' }}
                  />
                  <YAxis 
                    stroke="#9ca3af"
                    style={{ fontSize: '12px' }}
                    tickFormatter={(value) => `${value.toFixed(1)}%`}
                  />
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '6px' }}
                    formatter={(value) => [`${value.toFixed(2)}%`, 'Return']}
                    labelFormatter={(label) => `Date: ${label}`}
                  />
                  <ReferenceLine y={0} stroke="#6b7280" strokeDasharray="3 3" />
                  <Line 
                    type="monotone" 
                    dataKey="return" 
                    stroke="#10b981" 
                    strokeWidth={2}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* Volatility Chart */}
          <Card className="border-gray-200">
            <CardHeader>
              <CardTitle className="text-lg font-semibold">Rolling 30-Day Volatility</CardTitle>
              <CardDescription>Annualized standard deviation (30-day window)</CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={250}>
                <AreaChart data={charts.volatility}>
                  <defs>
                    <linearGradient id="colorVol" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#f59e0b" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="date" 
                    tickFormatter={formatDate}
                    stroke="#9ca3af"
                    style={{ fontSize: '12px' }}
                  />
                  <YAxis 
                    stroke="#9ca3af"
                    style={{ fontSize: '12px' }}
                    tickFormatter={(value) => `${value.toFixed(0)}%`}
                  />
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '6px' }}
                    formatter={(value) => [`${value.toFixed(2)}%`, 'Volatility']}
                    labelFormatter={(label) => `Date: ${label}`}
                  />
                  <Area 
                    type="monotone" 
                    dataKey="volatility" 
                    stroke="#f59e0b" 
                    strokeWidth={2}
                    fillOpacity={1} 
                    fill="url(#colorVol)" 
                  />
                </AreaChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* RSI Chart */}
          <Card className="border-gray-200">
            <CardHeader>
              <CardTitle className="text-lg font-semibold">RSI (14-period)</CardTitle>
              <CardDescription>Relative Strength Index with overbought/oversold thresholds</CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={charts.rsi}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="date" 
                    tickFormatter={formatDate}
                    stroke="#9ca3af"
                    style={{ fontSize: '12px' }}
                  />
                  <YAxis 
                    domain={[0, 100]}
                    stroke="#9ca3af"
                    style={{ fontSize: '12px' }}
                  />
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '6px' }}
                    formatter={(value) => [value.toFixed(2), 'RSI']}
                    labelFormatter={(label) => `Date: ${label}`}
                  />
                  <ReferenceLine y={70} stroke="#ef4444" strokeDasharray="3 3" label={{ value: 'Overbought (70)', position: 'right', fill: '#ef4444', fontSize: 10 }} />
                  <ReferenceLine y={30} stroke="#3b82f6" strokeDasharray="3 3" label={{ value: 'Oversold (30)', position: 'right', fill: '#3b82f6', fontSize: 10 }} />
                  <Line 
                    type="monotone" 
                    dataKey="rsi" 
                    stroke="#8b5cf6" 
                    strokeWidth={2}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </div>

        {/* Methodology Section */}
        <Card className="border-gray-200">
          <CardHeader>
            <CardTitle className="text-lg font-semibold">Methodology</CardTitle>
            <CardDescription>Formulas and calculation methods used in this analysis</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-2 gap-6">
              {Object.values(METHODOLOGY).map((method, index) => (
                <div key={index} className="space-y-2">
                  <h4 className="font-semibold text-gray-900">{method.title}</h4>
                  <code className="block px-3 py-2 bg-gray-100 rounded text-sm font-mono text-gray-800">
                    {method.formula}
                  </code>
                  <p className="text-sm text-gray-600">{method.description}</p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;