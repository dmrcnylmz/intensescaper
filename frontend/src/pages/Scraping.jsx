import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { scrapingAPI } from '../services/api';

export default function Scraping() {
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    setResult(null);

    try {
      const response = await scrapingAPI.start({ url });
      setResult(response.data);
      
      // İşlem durumunu kontrol et
      checkStatus(response.data.islemId);
    } catch (err) {
      setError(err.response?.data?.message || 'Scraping başlatılamadı');
    } finally {
      setLoading(false);
    }
  };

  const checkStatus = async (islemId) => {
    try {
      const response = await scrapingAPI.getStatus(islemId);
      setResult(response.data);
      
      if (response.data.durum === 'DEVAM_EDIYOR' || response.data.durum === 'BASLADI') {
        setTimeout(() => checkStatus(islemId), 3000);
      }
    } catch (err) {
      console.error('Durum kontrol edilemedi:', err);
    }
  };

  const getDurumColor = (durum) => {
    switch(durum) {
      case 'TAMAMLANDI': return 'text-green-400';
      case 'HATA': return 'text-red-400';
      case 'DEVAM_EDIYOR': return 'text-yellow-400';
      case 'BASLADI': return 'text-blue-400';
      default: return 'text-gray-400';
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <nav className="bg-white/10 backdrop-blur-xl border-b border-white/10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-20 items-center">
            <button
              onClick={() => navigate('/dashboard')}
              className="flex items-center gap-2 text-white hover:text-purple-300 transition-colors font-medium"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
              Dashboard'a Dön
            </button>
            <h1 className="text-2xl font-bold text-white">🔍 Veri Çekme</h1>
            <div className="w-40"></div>
          </div>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="bg-white/10 backdrop-blur-xl border border-white/10 rounded-3xl p-8 shadow-2xl">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-2xl mb-4 shadow-lg">
              <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h2 className="text-3xl font-bold text-white mb-2">Web Scraping</h2>
            <p className="text-purple-200">İlan sitelerinden otomatik veri çekimi</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-semibold text-white mb-3">
                Site URL'sini Yapıştırın
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <svg className="h-5 w-5 text-purple-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                  </svg>
                </div>
                <input
                  type="url"
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  placeholder="https://www.sahibinden.com/..."
                  className="w-full pl-12 pr-4 py-4 bg-white/10 border-2 border-white/20 rounded-xl text-white placeholder-purple-300 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all outline-none"
                  required
                />
              </div>
              <div className="mt-3 flex items-start gap-2 text-sm text-purple-200">
                <svg className="w-5 h-5 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                </svg>
                <div>
                  <p className="font-medium">Desteklenen siteler:</p>
                  <p>• Sahibinden.com • Emlakjet.com • Arabam.com</p>
                </div>
              </div>
            </div>

            {error && (
              <div className="bg-red-500/20 border-2 border-red-500/50 text-red-200 px-4 py-3 rounded-xl flex items-center gap-2">
                <svg className="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                <span className="text-sm">{error}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-bold py-4 rounded-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-blue-500/50 transform hover:scale-[1.02] active:scale-[0.98]"
            >
              {loading ? (
                <div className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <span>İşlem Başlatılıyor...</span>
                </div>
              ) : (
                <span>🚀 Scraping Başlat</span>
              )}
            </button>
          </form>

          {result && (
            <div className="mt-8 p-6 bg-white/5 border border-white/10 rounded-2xl">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-bold text-white">İşlem Durumu</h3>
                <span className={`px-4 py-2 rounded-full text-sm font-semibold ${getDurumColor(result.durum)} bg-white/10`}>
                  {result.durum}
                </span>
              </div>

              {/* Progress Bar */}
              <div className="mb-6">
                <div className="flex justify-between text-sm text-purple-200 mb-2">
                  <span>İlerleme</span>
                  <span className="font-bold">{result.ilerlemeYuzdesi?.toFixed(1) || 0}%</span>
                </div>
                <div className="w-full bg-white/10 rounded-full h-3 overflow-hidden">
                  <div 
                    className="bg-gradient-to-r from-blue-500 to-cyan-500 h-3 rounded-full transition-all duration-300"
                    style={{ width: `${result.ilerlemeYuzdesi || 0}%` }}
                  ></div>
                </div>
              </div>

              {/* Stats Grid */}
              <div className="grid grid-cols-2 gap-4 mb-6">
                <div className="bg-white/5 rounded-xl p-4">
                  <p className="text-sm text-purple-200 mb-1">Toplam İlan</p>
                  <p className="text-3xl font-bold text-white">{result.toplamIlan || 0}</p>
                </div>
                <div className="bg-white/5 rounded-xl p-4">
                  <p className="text-sm text-purple-200 mb-1">Tamamlanan</p>
                  <p className="text-3xl font-bold text-white">{result.tamamlananIlan || 0}</p>
                </div>
              </div>
              
              {result.durum === 'TAMAMLANDI' && (
                <button
                  onClick={() => navigate('/ilanlar')}
                  className="w-full bg-gradient-to-r from-green-500 to-emerald-500 hover:from-green-600 hover:to-emerald-600 text-white font-bold py-3 rounded-xl transition-all shadow-lg hover:shadow-green-500/50"
                >
                  ✨ İlanları Görüntüle →
                </button>
              )}

              {(result.durum === 'DEVAM_EDIYOR' || result.durum === 'BASLADI') && (
                <div className="flex items-center justify-center gap-2 text-purple-200">
                  <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <span className="text-sm">İlanlar çekiliyor...</span>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

