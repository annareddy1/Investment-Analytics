import React from 'react';
import { useNavigate } from 'react-router-dom';
import { TrendingUp, ArrowLeft, CheckCircle2, Zap, BarChart3, Database, Shield } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';

const CaseStudy = () => {
  const navigate = useNavigate();

  const problemPoints = [
    'AWM teams spend 10-15 minutes per ticker using spreadsheets',
    'Inconsistent calculation methodologies across teams',
    'No standardized visualization for stakeholder presentations',
    'Manual data fetching prone to errors and delays'
  ];

  const solutionFeatures = [
    {
      icon: <Zap className="w-6 h-6" />,
      title: 'One-Click Analysis',
      description: 'Generate complete analytics in under 30 seconds with preset tickers'
    },
    {
      icon: <BarChart3 className="w-6 h-6" />,
      title: 'Standardized Metrics',
      description: 'Consistent calculation of returns, volatility, drawdown, and RSI'
    },
    {
      icon: <Database className="w-6 h-6" />,
      title: 'Async Processing',
      description: 'Background jobs with thread pools for efficient computation'
    },
    {
      icon: <Shield className="w-6 h-6" />,
      title: 'Enterprise Security',
      description: 'Spring Security + OAuth2 with optional authentication'
    }
  ];

  const architectureComponents = [
    {
      layer: 'Frontend',
      tech: 'React + Recharts',
      purpose: 'Interactive dashboard with real-time charts and KPI visualization'
    },
    {
      layer: 'Backend',
      tech: 'Java + Spring Boot',
      purpose: 'RESTful API with @Async thread pool for background analysis'
    },
    {
      layer: 'Data Layer',
      tech: 'PostgreSQL + MongoDB',
      purpose: 'Relational metadata storage + NoSQL for JSON results'
    },
    {
      layer: 'Security',
      tech: 'Spring Security + OAuth2',
      purpose: 'Optional Google authentication for saved history feature'
    },
    {
      layer: 'Data Source',
      tech: 'Yahoo Finance API',
      purpose: 'Real-time market data with Java HTTP client integration'
    }
  ];

  const impact = [
    {
      metric: '95%',
      label: 'Time Reduction',
      description: 'From 15 minutes to 30 seconds per analysis'
    },
    {
      metric: '100%',
      label: 'Consistency',
      description: 'Standardized calculations across all teams'
    },
    {
      metric: '5+',
      label: 'Preset Tickers',
      description: 'Quick access to commonly analyzed stocks'
    },
    {
      metric: '4',
      label: 'Key Metrics',
      description: 'Returns, volatility, drawdown, and RSI'
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="border-b border-gray-200 bg-white">
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
                Back to Home
              </Button>
              <div className="flex items-center gap-2">
                <TrendingUp className="w-6 h-6 text-gray-900" />
                <span className="text-xl font-bold text-gray-900">MarketLens</span>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Case Study Content */}
      <div className="container mx-auto px-6 py-12">
        {/* Header */}
        <div className="max-w-4xl mx-auto mb-16 text-center">
          <h1 className="text-5xl font-bold text-gray-900 mb-6">Case Study: MarketLens</h1>
          <p className="text-xl text-gray-600 leading-relaxed">
            How we built a portfolio-ready investment analytics platform that reduces analysis time by 95%
          </p>
        </div>

        {/* Problem Section */}
        <section className="max-w-4xl mx-auto mb-16">
          <h2 className="text-3xl font-bold text-gray-900 mb-6">The Problem</h2>
          <Card className="border-gray-200">
            <CardContent className="pt-6">
              <p className="text-lg text-gray-700 mb-6 leading-relaxed">
                AWM users and internal teams frequently need to generate consistent investment analytics 
                for various tickers. Traditional methods using spreadsheets and manual scripts are slow, 
                inconsistent, and prone to errors.
              </p>
              <div className="space-y-3">
                {problemPoints.map((point, index) => (
                  <div key={index} className="flex items-start gap-3">
                    <div className="w-6 h-6 rounded-full bg-red-100 flex items-center justify-center flex-shrink-0 mt-0.5">
                      <span className="text-red-600 text-sm font-bold">×</span>
                    </div>
                    <p className="text-gray-700">{point}</p>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </section>

        {/* Solution Section */}
        <section className="max-w-4xl mx-auto mb-16">
          <h2 className="text-3xl font-bold text-gray-900 mb-6">The Solution</h2>
          <p className="text-lg text-gray-700 mb-8 leading-relaxed">
            MarketLens provides a one-click, repeatable dashboard that generates professional 
            investment analytics in under 30 seconds. Built with enterprise-grade technology 
            for reliability and performance.
          </p>
          <div className="grid md:grid-cols-2 gap-6">
            {solutionFeatures.map((feature, index) => (
              <Card key={index} className="border-gray-200">
                <CardHeader>
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-gray-100 rounded-lg text-gray-900">
                      {feature.icon}
                    </div>
                    <CardTitle className="text-lg">{feature.title}</CardTitle>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-gray-600">{feature.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </section>

        {/* Architecture Section */}
        <section className="max-w-4xl mx-auto mb-16">
          <h2 className="text-3xl font-bold text-gray-900 mb-6">Architecture</h2>
          <Card className="border-gray-200">
            <CardContent className="pt-6">
              <div className="space-y-6">
                {architectureComponents.map((component, index) => (
                  <div key={index} className="flex items-start gap-4 pb-6 border-b border-gray-200 last:border-0">
                    <div className="flex-shrink-0">
                      <div className="w-24 h-24 bg-gray-100 rounded-lg flex items-center justify-center">
                        <span className="text-3xl font-bold text-gray-400">{index + 1}</span>
                      </div>
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <h3 className="text-lg font-semibold text-gray-900">{component.layer}</h3>
                        <span className="px-2 py-1 bg-gray-100 rounded text-xs font-medium text-gray-600">
                          {component.tech}
                        </span>
                      </div>
                      <p className="text-gray-600">{component.purpose}</p>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </section>

        {/* Analytics Section */}
        <section className="max-w-4xl mx-auto mb-16">
          <h2 className="text-3xl font-bold text-gray-900 mb-6">Key Analytics</h2>
          <Card className="border-gray-200">
            <CardContent className="pt-6">
              <div className="space-y-4">
                <div className="flex items-start gap-3">
                  <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                  <div>
                    <h4 className="font-semibold text-gray-900">Daily Returns</h4>
                    <p className="text-sm text-gray-600">Calculated as (P_t / P_{t-1}) - 1 for day-over-day changes</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                  <div>
                    <h4 className="font-semibold text-gray-900">Cumulative Return</h4>
                    <p className="text-sm text-gray-600">Total return computed as (P_end / P_start) - 1</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                  <div>
                    <h4 className="font-semibold text-gray-900">Rolling 30-Day Volatility</h4>
                    <p className="text-sm text-gray-600">Annualized standard deviation using rolling window of returns</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                  <div>
                    <h4 className="font-semibold text-gray-900">Maximum Drawdown</h4>
                    <p className="text-sm text-gray-600">Largest peak-to-trough decline calculated as min((P_t / peak_t) - 1)</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                  <div>
                    <h4 className="font-semibold text-gray-900">RSI (14-period)</h4>
                    <p className="text-sm text-gray-600">Relative Strength Index with Wilder smoothing, flagging overbought (RSI &gt;= 70) and oversold (RSI &lt;= 30) conditions</p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </section>

        {/* Impact Section */}
        <section className="max-w-4xl mx-auto mb-16">
          <h2 className="text-3xl font-bold text-gray-900 mb-6">Impact</h2>
          <div className="grid md:grid-cols-4 gap-6">
            {impact.map((item, index) => (
              <Card key={index} className="border-gray-200 text-center">
                <CardContent className="pt-6">
                  <div className="text-4xl font-bold text-gray-900 mb-2">{item.metric}</div>
                  <div className="text-sm font-semibold text-gray-900 mb-1">{item.label}</div>
                  <div className="text-xs text-gray-600">{item.description}</div>
                </CardContent>
              </Card>
            ))}
          </div>
        </section>

        {/* CTA Section */}
        <section className="max-w-4xl mx-auto text-center">
          <Card className="border-gray-900 bg-gray-900 text-white">
            <CardContent className="pt-8 pb-8">
              <h2 className="text-3xl font-bold mb-4">Try MarketLens Now</h2>
              <p className="text-gray-300 mb-6 max-w-2xl mx-auto">
                Experience the power of one-click investment analytics. No login required.
              </p>
              <Button 
                size="lg" 
                className="bg-white text-gray-900 hover:bg-gray-100"
                onClick={() => navigate('/')}
              >
                <Zap className="w-5 h-5 mr-2" />
                Launch Demo
              </Button>
            </CardContent>
          </Card>
        </section>
      </div>

      {/* Footer */}
      <footer className="border-t border-gray-200 bg-white py-8 mt-16">
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

export default CaseStudy;