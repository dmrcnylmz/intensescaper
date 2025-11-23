import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authAPI } from '../services/api';

export default function Dashboard() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const response = await authAPI.getProfile();
      setProfile(response.data);
    } catch (error) {
      console.error('Profil yüklenemedi:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-purple-500"></div>
          <p className="mt-4 text-white font-semibold">Yükleniyor...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      {/* Header */}
      <nav className="bg-white/10 backdrop-blur-xl border-b border-white/10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-20 items-center">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-pink-500 rounded-xl flex items-center justify-center shadow-lg">
                <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h1 className="text-2xl font-bold text-white">IntenseScraper</h1>
            </div>
            <div className="flex items-center gap-4">
              <div className="hidden md:flex items-center gap-2 px-4 py-2 bg-white/10 rounded-xl">
                <div className="w-8 h-8 bg-gradient-to-br from-purple-400 to-pink-400 rounded-full flex items-center justify-center">
                  <span className="text-white font-bold text-sm">
                    {profile?.kullaniciAdi?.charAt(0).toUpperCase()}
                  </span>
                </div>
                <span className="text-white font-medium">
                  {profile?.kullaniciAdi}
                </span>
              </div>
              <button
                onClick={handleLogout}
                className="bg-red-500/90 hover:bg-red-600 text-white px-5 py-2.5 rounded-xl font-medium transition-all shadow-lg hover:shadow-red-500/50"
              >
                Çıkış
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Banner */}
        <div className="mb-8 bg-gradient-to-r from-purple-500/20 to-pink-500/20 backdrop-blur-xl border border-white/10 rounded-2xl p-6">
          <h2 className="text-3xl font-bold text-white mb-2">
            Hoş geldin, {profile?.kullaniciAdi}! 👋
          </h2>
          <p className="text-purple-200">
            Web scraping ve mesajlaşma işlemlerinizi buradan yönetebilirsiniz.
          </p>
        </div>

        {/* İstatistik Kartları */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white/10 backdrop-blur-xl border border-white/10 rounded-2xl p-6 hover:bg-white/15 transition-all group">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-purple-300 font-medium mb-1">Toplam İlan</p>
                <p className="text-4xl font-bold text-white">
                  {profile?.toplamIlanSayisi || 0}
                </p>
              </div>
              <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                <svg
                  className="w-8 h-8 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                  />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white/10 backdrop-blur-xl border border-white/10 rounded-2xl p-6 hover:bg-white/15 transition-all group">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-purple-300 font-medium mb-1">Mesaj Gönderilen</p>
                <p className="text-4xl font-bold text-white">
                  {profile?.mesajGonderilenIlanSayisi || 0}
                </p>
              </div>
              <div className="w-16 h-16 bg-gradient-to-br from-green-500 to-emerald-500 rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                <svg
                  className="w-8 h-8 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
                  />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white/10 backdrop-blur-xl border border-white/10 rounded-2xl p-6 hover:bg-white/15 transition-all group">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-purple-300 font-medium mb-1">Hesap Durumu</p>
                <p className="text-4xl font-bold text-white">
                  {profile?.rol?.replace('ROLE_', '') || 'USER'}
                </p>
              </div>
              <div className="w-16 h-16 bg-gradient-to-br from-purple-500 to-pink-500 rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                <svg
                  className="w-8 h-8 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
                  />
                </svg>
              </div>
            </div>
          </div>
        </div>

        {/* Hızlı Eylemler */}
        <h3 className="text-2xl font-bold text-white mb-6">Hızlı İşlemler</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <button
            onClick={() => navigate('/scraping')}
            className="bg-gradient-to-br from-blue-500/20 to-cyan-500/20 backdrop-blur-xl border border-white/10 rounded-2xl p-6 hover:from-blue-500/30 hover:to-cyan-500/30 transition-all group transform hover:scale-105"
          >
            <div className="text-center">
              <div className="inline-block p-4 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-2xl shadow-lg group-hover:shadow-blue-500/50 transition-all">
                <svg
                  className="w-10 h-10 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
              <h3 className="mt-4 text-lg font-bold text-white">
                🚀 Veri Çek
              </h3>
              <p className="mt-2 text-sm text-purple-200">
                Sahibinden'den ilan çek
              </p>
            </div>
          </button>

          <button
            onClick={() => navigate('/ilanlar')}
            className="bg-gradient-to-br from-green-500/20 to-emerald-500/20 backdrop-blur-xl border border-white/10 rounded-2xl p-6 hover:from-green-500/30 hover:to-emerald-500/30 transition-all group transform hover:scale-105"
          >
            <div className="text-center">
              <div className="inline-block p-4 bg-gradient-to-br from-green-500 to-emerald-500 rounded-2xl shadow-lg group-hover:shadow-green-500/50 transition-all">
                <svg
                  className="w-10 h-10 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                  />
                </svg>
              </div>
              <h3 className="mt-4 text-lg font-bold text-white">
                📋 İlanlar
              </h3>
              <p className="mt-2 text-sm text-purple-200">
                Çekilen ilanları gör
              </p>
            </div>
          </button>

        <button
          onClick={() => navigate('/ilanlar')}
          className="bg-gradient-to-br from-purple-500/20 to-pink-500/20 backdrop-blur-xl border border-white/10 rounded-2xl p-6 hover:from-purple-500/30 hover:to-pink-500/30 transition-all group transform hover:scale-105"
        >
          <div className="text-center">
            <div className="inline-block p-4 bg-gradient-to-br from-purple-500 to-pink-500 rounded-2xl shadow-lg group-hover:shadow-purple-500/50 transition-all">
              <svg
                className="w-10 h-10 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
                />
              </svg>
            </div>
            <h3 className="mt-4 text-lg font-bold text-white">
              💬 Mesaj Merkezi
            </h3>
            <p className="mt-2 text-sm text-purple-200">
              İlanlar sayfasından şablon oluştur & mesaj hazırla
            </p>
          </div>
        </button>

          <button
            onClick={() => navigate('/mesajlasma')}
            className="bg-gradient-to-br from-orange-500/20 to-red-500/20 backdrop-blur-xl border border-white/10 rounded-2xl p-6 hover:from-orange-500/30 hover:to-red-500/30 transition-all group transform hover:scale-105"
          >
            <div className="text-center">
              <div className="inline-block p-4 bg-gradient-to-br from-orange-500 to-red-500 rounded-2xl shadow-lg group-hover:shadow-orange-500/50 transition-all">
                <svg
                  className="w-10 h-10 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
                  />
                </svg>
              </div>
              <h3 className="mt-4 text-lg font-bold text-white">
                📱 Mesaj Gönder
              </h3>
              <p className="mt-2 text-sm text-purple-200">
                Toplu WhatsApp mesajı
              </p>
            </div>
          </button>
        </div>
      </div>
    </div>
  );
}

