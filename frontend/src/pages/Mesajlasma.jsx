import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { mesajAPI, ilanAPI, sablonAPI } from '../services/api';

export default function Mesajlasma() {
  const [ilanlar, setIlanlar] = useState([]);
  const [sablonlar, setSablonlar] = useState([]);
  const [selectedIlanlar, setSelectedIlanlar] = useState([]);
  const [selectedSablon, setSelectedSablon] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [ilanResponse, sablonResponse] = await Promise.all([
        ilanAPI.list({ page: 0, size: 50 }),
        sablonAPI.list(),
      ]);
      setIlanlar(ilanResponse.data.content.filter((i) => !i.mesajGonderildi));
      setSablonlar(sablonResponse.data);
    } catch (error) {
      console.error('Veri yüklenemedi:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = () => {
    if (selectedIlanlar.length === ilanlar.length) {
      setSelectedIlanlar([]);
    } else {
      setSelectedIlanlar(ilanlar.map((i) => i.id));
    }
  };

  const handleSelectIlan = (id) => {
    setSelectedIlanlar((prev) =>
      prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSending(true);
    setResult(null);

    try {
      const response = await mesajAPI.send({
        ilanIdListesi: selectedIlanlar,
        sablonId: parseInt(selectedSablon),
      });
      setResult({ success: true, data: response.data });
      setSelectedIlanlar([]);
      loadData(); // Listeyi yenile
    } catch (error) {
      setResult({ success: false, message: error.response?.data?.message || 'Hata oluştu' });
    } finally {
      setSending(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <button
              onClick={() => navigate('/dashboard')}
              className="text-blue-600 hover:text-blue-800"
            >
              ← Dashboard'a Dön
            </button>
            <h1 className="text-2xl font-bold text-gray-800">Toplu Mesaj Gönder</h1>
            <div className="w-32"></div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {ilanlar.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-xl shadow-md">
            <p className="text-gray-500">Mesaj gönderilebilecek ilan yok.</p>
            <button
              onClick={() => navigate('/scraping')}
              className="mt-4 bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg"
            >
              Veri Çek
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Şablon Seçimi */}
            <div className="bg-white rounded-xl shadow-md p-6">
              <h2 className="text-lg font-semibold mb-4">1. Mesaj Şablonu Seç</h2>
              <select
                value={selectedSablon}
                onChange={(e) => setSelectedSablon(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              >
                <option value="">Şablon seçin...</option>
                {sablonlar.map((sablon) => (
                  <option key={sablon.id} value={sablon.id}>
                    {sablon.baslik}
                  </option>
                ))}
              </select>
              {sablonlar.length === 0 && (
                <button
                  type="button"
                  onClick={() => navigate('/sablonlar')}
                  className="mt-2 text-blue-500 hover:text-blue-700 text-sm"
                >
                  + Yeni şablon oluştur
                </button>
              )}
            </div>

            {/* İlan Seçimi */}
            <div className="bg-white rounded-xl shadow-md p-6">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-lg font-semibold">2. İlanları Seç</h2>
                <button
                  type="button"
                  onClick={handleSelectAll}
                  className="text-blue-500 hover:text-blue-700 text-sm"
                >
                  {selectedIlanlar.length === ilanlar.length
                    ? 'Tümünü Kaldır'
                    : 'Tümünü Seç'}
                </button>
              </div>
              <div className="space-y-2 max-h-96 overflow-y-auto">
                {ilanlar.map((ilan) => (
                  <label
                    key={ilan.id}
                    className="flex items-center p-3 border rounded-lg hover:bg-gray-50 cursor-pointer"
                  >
                    <input
                      type="checkbox"
                      checked={selectedIlanlar.includes(ilan.id)}
                      onChange={() => handleSelectIlan(ilan.id)}
                      className="mr-3 h-4 w-4"
                    />
                    <div className="flex-1">
                      <p className="font-medium text-gray-800">{ilan.baslik}</p>
                      <p className="text-sm text-gray-600">
                        {ilan.telefonNumarasi} - {ilan.site}
                      </p>
                    </div>
                  </label>
                ))}
              </div>
              <p className="mt-4 text-sm text-gray-600">
                Seçilen: <strong>{selectedIlanlar.length}</strong> ilan
              </p>
            </div>

            {/* Gönder Butonu */}
            <button
              type="submit"
              disabled={sending || selectedIlanlar.length === 0 || !selectedSablon}
              className="w-full bg-green-500 hover:bg-green-600 text-white font-semibold py-3 rounded-lg transition disabled:opacity-50"
            >
              {sending
                ? 'Gönderiliyor...'
                : `📤 ${selectedIlanlar.length} İlana Mesaj Gönder`}
            </button>

            {/* Sonuç */}
            {result && (
              <div
                className={`p-4 rounded-lg ${
                  result.success
                    ? 'bg-green-100 text-green-800'
                    : 'bg-red-100 text-red-800'
                }`}
              >
                {result.success ? (
                  <div>
                    <p className="font-semibold">✓ Mesajlar kuyruğa eklendi!</p>
                    <p className="text-sm mt-1">
                      {result.data.toplamIlan} ilan için mesaj gönderimi başlatıldı.
                    </p>
                  </div>
                ) : (
                  <p>{result.message}</p>
                )}
              </div>
            )}
          </form>
        )}
      </div>
    </div>
  );
}

