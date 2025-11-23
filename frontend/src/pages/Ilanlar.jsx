import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { ilanAPI, sablonAPI } from '../services/api';

const PLACEHOLDERS = [
  { key: 'telefonNumarasi', label: 'Telefon' },
  { key: 'baslik', label: 'İlan Başlığı' },
  { key: 'konum', label: 'Konum' },
  { key: 'fiyat', label: 'Fiyat' },
  { key: 'ilanSahibi', label: 'İlan Sahibi' },
];

export default function Ilanlar() {
  const [ilanlar, setIlanlar] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [sablonlar, setSablonlar] = useState([]);
  const [sablonLoading, setSablonLoading] = useState(true);
  const [selectedTemplateId, setSelectedTemplateId] = useState(null);
  const [showTemplateForm, setShowTemplateForm] = useState(false);
  const [templateForm, setTemplateForm] = useState({
    baslik: '',
    icerik:
      'Merhaba {{ilanSahibi}}, {{baslik}} ilanınız hakkında bilgi almak istiyorum. Uygunsanız dönüş yapabilir misiniz?',
    aciklama: '',
  });
  const [savingTemplate, setSavingTemplate] = useState(false);

  const [composerOpen, setComposerOpen] = useState(false);
  const [activeIlan, setActiveIlan] = useState(null);
  const [messageDraft, setMessageDraft] = useState('');
  const [copyState, setCopyState] = useState('idle');
  const [phoneCopyState, setPhoneCopyState] = useState('idle');

  const navigate = useNavigate();

  useEffect(() => {
    loadIlanlar();
  }, [page]);

  useEffect(() => {
    loadSablonlar();
  }, []);

  const loadIlanlar = async () => {
    try {
      const response = await ilanAPI.list({ page, size: 10 });
      setIlanlar(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('İlanlar yüklenemedi:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadSablonlar = async () => {
    try {
      setSablonLoading(true);
      const response = await sablonAPI.list();
      setSablonlar(response.data);
      if (!selectedTemplateId && response.data.length > 0) {
        setSelectedTemplateId(response.data[0].id);
      }
    } catch (error) {
      console.error('Şablonlar yüklenemedi:', error);
    } finally {
      setSablonLoading(false);
    }
  };

  const applyTemplate = (text, ilan) => {
    if (!text) return '';
    if (!ilan) return text;

    const map = {
      telefonNumarasi: ilan.telefonNumarasi || '',
      baslik: ilan.baslik || '',
      konum: ilan.konum || '',
      fiyat: ilan.fiyat ? `${ilan.fiyat.toLocaleString('tr-TR')} ₺` : '',
      ilanSahibi: ilan.aciklama || 'Değerli ilan sahibi',
    };

    return text.replace(/{{\s*(\w+)\s*}}/g, (_, key) => map[key] ?? '');
  };

  const defaultMessage = (ilan) =>
    `Merhaba ${ilan?.aciklama || 'değerli ilan sahibi'}, ${ilan?.baslik || 'ilanınız'} hakkında bilgi almak istiyorum. Uygunsanız dönüş yapabilir misiniz?`;

  const currentTemplateBody = useMemo(() => {
    const match = sablonlar.find((s) => s.id === selectedTemplateId);
    return match?.icerik || '';
  }, [sablonlar, selectedTemplateId]);

  const openComposer = (ilan) => {
    setActiveIlan(ilan);
    if (currentTemplateBody) {
      setMessageDraft(applyTemplate(currentTemplateBody, ilan));
    } else {
      setMessageDraft(defaultMessage(ilan));
    }
    setComposerOpen(true);
    setCopyState('idle');
  };

  const closeComposer = () => {
    setComposerOpen(false);
    setActiveIlan(null);
    setMessageDraft('');
  };

  const handleTemplateSelect = (id) => {
    const parsedId = id ? Number(id) : null;
    setSelectedTemplateId(parsedId);
    if (activeIlan) {
      const template = sablonlar.find((s) => s.id === parsedId);
      setMessageDraft(applyTemplate(template?.icerik, activeIlan));
    }
  };

  const getWhatsappLink = (telefon) => {
    const digits = (telefon || '').replace(/\D/g, '');
    if (!digits) return null;
    let normalized = digits;
    if (normalized.startsWith('0')) {
      normalized = normalized.substring(1);
    }
    if (!normalized.startsWith('90')) {
      normalized = `90${normalized}`;
    }
    if (normalized.length < 12) return null;
    return `https://wa.me/${normalized}`;
  };

  const handleInsertTokenToDraft = (key) => {
    setMessageDraft((prev) => `${prev.trimEnd()} {{${key}}}`.trimStart());
  };

  const handleCopyPhone = async () => {
    if (!activeIlan?.telefonNumarasi) return;
    try {
      await navigator.clipboard.writeText(activeIlan.telefonNumarasi);
      setPhoneCopyState('copied');
      setTimeout(() => setPhoneCopyState('idle'), 2000);
    } catch (error) {
      console.error('Telefon kopyalanamadı', error);
      setPhoneCopyState('error');
    }
  };

  const handleWhatsappRedirect = () => {
    const link = getWhatsappLink(activeIlan?.telefonNumarasi);
    if (!link) {
      alert('Geçerli bir telefon numarası bulunamadı.');
      return;
    }
    window.open(link, '_blank', 'noopener,noreferrer');
  };

  const handleTemplateSubmit = async (e) => {
    e.preventDefault();
    try {
      setSavingTemplate(true);
      const response = await sablonAPI.create(templateForm);
      setTemplateForm((prev) => ({ ...prev, baslik: '', aciklama: '' }));
      await loadSablonlar();
      setSelectedTemplateId(response.data.id);
      setShowTemplateForm(false);
    } catch (error) {
      console.error('Şablon oluşturulamadı:', error);
    } finally {
      setSavingTemplate(false);
    }
  };

  const handleInsertPlaceholder = (key) => {
    setTemplateForm((prev) => ({
      ...prev,
      icerik: `${prev.icerik.trimEnd()} {{${key}}}`,
    }));
  };

  const handleCopyMessage = async () => {
    if (!messageDraft) return;
    try {
      await navigator.clipboard.writeText(messageDraft);
      setCopyState('copied');
      setTimeout(() => setCopyState('idle'), 2000);
    } catch (error) {
      console.error('Mesaj kopyalanamadı', error);
      setCopyState('error');
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
            <h1 className="text-2xl font-bold text-gray-800">İlanlar & Mesaj Merkezi</h1>
            <div className="hidden md:block text-sm text-gray-500">
              Toplam {ilanlar.length} kayıt
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 py-8 space-y-8">
        {/* Şablon Paneli */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <div className="flex items-center justify-between mb-4">
              <div>
                <p className="text-sm text-gray-500">Hazır Mesajlar</p>
                <h2 className="text-xl font-semibold text-gray-800">Şablon Seç</h2>
              </div>
              <button
                onClick={() => setShowTemplateForm((prev) => !prev)}
                className="text-sm text-blue-600 hover:text-blue-800 font-medium"
              >
                {showTemplateForm ? 'Formu Gizle' : '+ Yeni Şablon'}
              </button>
            </div>

            {sablonLoading ? (
              <div className="py-8 text-center text-gray-500">Şablonlar yükleniyor...</div>
            ) : sablonlar.length === 0 ? (
              <div className="text-center py-10 border-2 border-dashed border-gray-200 rounded-xl">
                <p className="text-sm text-gray-500">
                  Henüz kaydedilmiş şablon yok. Hemen oluştur!
                </p>
              </div>
            ) : (
              <div className="space-y-3 max-h-60 overflow-y-auto pr-2">
                {sablonlar.map((sablon) => (
                  <button
                    key={sablon.id}
                    onClick={() => handleTemplateSelect(sablon.id)}
                    className={`w-full text-left rounded-xl border px-4 py-3 transition ${
                      selectedTemplateId === sablon.id
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-200 hover:border-blue-200'
                    }`}
                  >
                    <div className="flex items-center justify-between gap-4">
                      <div>
                        <p className="font-semibold text-gray-800">{sablon.baslik}</p>
                        <p className="text-xs text-gray-500 line-clamp-2">{sablon.icerik}</p>
                      </div>
                      {selectedTemplateId === sablon.id && (
                        <span className="text-xs text-blue-600 font-medium">Seçildi</span>
                      )}
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          {showTemplateForm && (
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h2 className="text-xl font-semibold text-gray-800 mb-4">Hızlı Şablon Oluştur</h2>
              <form className="space-y-4" onSubmit={handleTemplateSubmit}>
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1">Başlık</label>
                  <input
                    type="text"
                    value={templateForm.baslik}
                    onChange={(e) => setTemplateForm({ ...templateForm, baslik: e.target.value })}
                    className="w-full rounded-lg border border-gray-200 px-3 py-2 focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1">
                    Mesaj İçeriği
                  </label>
                  <textarea
                    rows={5}
                    value={templateForm.icerik}
                    onChange={(e) => setTemplateForm({ ...templateForm, icerik: e.target.value })}
                    className="w-full rounded-lg border border-gray-200 px-3 py-2 focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                    required
                  />
                  <div className="flex flex-wrap gap-2 mt-3">
                    {PLACEHOLDERS.map((p) => (
                      <button
                        type="button"
                        key={p.key}
                        onClick={() => handleInsertPlaceholder(p.key)}
                        className="px-3 py-1 text-xs rounded-full bg-gray-100 text-gray-700 hover:bg-gray-200"
                      >
                        {`{{${p.key}}}`}
                      </button>
                    ))}
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1">
                    Açıklama (opsiyonel)
                  </label>
                  <input
                    type="text"
                    value={templateForm.aciklama}
                    onChange={(e) => setTemplateForm({ ...templateForm, aciklama: e.target.value })}
                    className="w-full rounded-lg border border-gray-200 px-3 py-2 focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                  />
                </div>
                <button
                  type="submit"
                  disabled={savingTemplate}
                  className="w-full bg-blue-500 hover:bg-blue-600 text-white rounded-lg py-2 font-medium disabled:opacity-60"
                >
                  {savingTemplate ? 'Kaydediliyor...' : 'Şablonu Kaydet'}
                </button>
              </form>
            </div>
          )}
        </div>

        {/* İlan Listesi */}
        {ilanlar.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-2xl shadow-sm border border-gray-100">
            <h3 className="text-lg font-semibold text-gray-800 mb-2">Henüz ilan yok</h3>
            <p className="text-sm text-gray-500">Veri çekerek ilanları listeleyebilirsiniz.</p>
            <button
              onClick={() => navigate('/scraping')}
              className="mt-4 bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg"
            >
              Veri Çek
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {ilanlar.map((ilan) => (
              <div
                key={ilan.id}
                className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-lg transition"
              >
                <div className="flex justify-between items-start gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded">
                        {ilan.site}
                      </span>
                      {ilan.mesajGonderildi && (
                        <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded">
                          ✓ Mesaj Gönderildi
                        </span>
                      )}
                    </div>
                    <h3 className="text-lg font-semibold text-gray-800 mb-2 line-clamp-2">
                      {ilan.baslik}
                    </h3>
                    <div className="bg-gray-50 rounded-lg p-4 mb-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <span className="text-2xl">📞</span>
                          <div>
                            <p className="text-xs text-gray-500">Telefon</p>
                            <p className="text-lg font-bold text-gray-900">{ilan.telefonNumarasi}</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="text-xs text-gray-500">{ilan.konum}</p>
                          <p className="text-xs text-gray-400">
                            {new Date(ilan.cekilmeTarihi).toLocaleDateString('tr-TR')}
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      <button
                        onClick={() => openComposer(ilan)}
                        className="w-full py-2.5 rounded-lg font-medium bg-blue-500 hover:bg-blue-600 text-white transition"
                      >
                        ✍️ Mesaj Oluştur
                      </button>
                      <button
                        onClick={() => navigate('/mesajlasma')}
                        className="w-full py-2.5 rounded-lg font-medium border border-gray-200 hover:border-blue-300 text-gray-700 transition"
                      >
                        📤 Toplu Gönderim
                      </button>
                    </div>
                  </div>
                  <a
                    href={ilan.ilanUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-500 hover:text-blue-700"
                  >
                    <svg
                      className="w-6 h-6"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
                      />
                    </svg>
                  </a>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="mt-8 flex justify-center gap-2">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="px-4 py-2 border rounded-lg disabled:opacity-50"
            >
              ← Önceki
            </button>
            <span className="px-4 py-2">
              Sayfa {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              className="px-4 py-2 border rounded-lg disabled:opacity-50"
            >
              Sonraki →
            </button>
          </div>
        )}
      </div>

      {/* Composer */}
      {composerOpen && activeIlan && (
        <div className="fixed inset-0 z-50">
          <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={closeComposer}></div>
          <div className="relative z-10 max-w-5xl mx-auto my-10 px-4">
            <div className="bg-white rounded-2xl shadow-2xl border border-gray-100 p-6 space-y-6">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-xs uppercase tracking-widest text-gray-400">Mesaj Oluştur</p>
                  <h3 className="text-2xl font-semibold text-gray-900">{activeIlan.baslik}</h3>
                  <p className="text-sm text-gray-500">{activeIlan.konum}</p>
                </div>
                <button onClick={closeComposer} className="text-gray-500 hover:text-gray-800 text-sm">
                  Kapat ✕
                </button>
              </div>

              <div className="grid md:grid-cols-3 gap-5">
                <div className="space-y-4">
                  <div className="border border-gray-200 rounded-xl p-4 bg-gray-50">
                    <p className="text-xs uppercase text-gray-400 mb-2">İlan Özeti</p>
                    <div className="space-y-2 text-sm text-gray-700">
                      <p>👤 {activeIlan.aciklama || 'İlan Sahibi Bilinmiyor'}</p>
                      <p>📞 {activeIlan.telefonNumarasi}</p>
                      <p className="truncate">
                        🔗{' '}
                        <a href={activeIlan.ilanUrl} target="_blank" rel="noopener noreferrer" className="text-blue-600">
                          İlan linkini aç
                        </a>
                      </p>
                    </div>
                    <div className="mt-3 flex flex-wrap gap-2">
                      <button
                        onClick={handleCopyPhone}
                        className="px-3 py-1 rounded-lg border text-xs text-gray-700 hover:bg-gray-100"
                      >
                        {phoneCopyState === 'copied' ? '✓ Telefon Kopyalandı' : '📋 Telefonu Kopyala'}
                      </button>
                      <button
                        onClick={handleWhatsappRedirect}
                        className="px-3 py-1 rounded-lg bg-green-500 text-white text-xs hover:bg-green-600"
                      >
                        WhatsApp Aç
                      </button>
                    </div>
                  </div>

                  <div className="border border-gray-200 rounded-xl p-4">
                    <label className="text-xs uppercase text-gray-400">Şablon Seç</label>
                    <select
                      value={selectedTemplateId ?? ''}
                      onChange={(e) => handleTemplateSelect(e.target.value)}
                      className="mt-2 w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-200 focus:border-blue-500"
                    >
                      <option value="">Serbest yaz</option>
                      {sablonlar.map((sablon) => (
                        <option key={sablon.id} value={sablon.id}>
                          {sablon.baslik}
                        </option>
                      ))}
                    </select>
                    <div className="mt-3">
                      <p className="text-xs text-gray-400 mb-1">Hızlı değişkenler:</p>
                      <div className="flex flex-wrap gap-2">
                        {PLACEHOLDERS.map((p) => (
                          <button
                            key={p.key}
                            onClick={() => handleInsertTokenToDraft(p.key)}
                            className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-700 hover:bg-gray-200"
                          >
                            {`{{${p.key}}}`}
                          </button>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="md:col-span-2 space-y-4">
                  <textarea
                    rows={10}
                    value={messageDraft}
                    onChange={(e) => setMessageDraft(e.target.value)}
                    className="w-full rounded-2xl border border-gray-200 px-4 py-3 text-gray-800 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 shadow-inner"
                  />
                  <div className="flex flex-wrap gap-3">
                    <button
                      onClick={handleCopyMessage}
                      className="px-4 py-2 rounded-xl bg-gray-100 text-gray-800 hover:bg-gray-200"
                    >
                      {copyState === 'copied' ? '✅ Mesaj Kopyalandı' : '📋 Mesajı Kopyala'}
                    </button>
                    <button
                      onClick={() => navigate('/mesajlasma')}
                      className="px-4 py-2 rounded-xl bg-blue-500 text-white hover:bg-blue-600"
                    >
                      Toplu Gönderime Git
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

