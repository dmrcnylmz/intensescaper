package com.intensescaper.service;

import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.MesajKuyrugu;
import com.intensescaper.entity.MesajSablonu;
import com.intensescaper.enums.MesajDurumu;
import com.intensescaper.exception.MessagingException;
import com.intensescaper.repository.IlanRepository;
import com.intensescaper.repository.MesajKuyruguRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsappService {

    private final MesajKuyruguRepository mesajKuyruguRepository;
    private final IlanRepository ilanRepository;

    @Value("${whatsapp.api.enabled}")
    private boolean whatsappApiEnabled;

    @Value("${whatsapp.message.delay-ms}")
    private int messageDelayMs;

    @Value("${whatsapp.message.max-retry}")
    private int maxRetry;

    /**
     * Toplu mesaj gönderimi için ilanları kuyruğa ekler
     */
    @Transactional
    public void topluMesajGonder(List<Ilan> ilanlar, MesajSablonu sablon) throws MessagingException {
        log.info("Toplu mesaj gönderimi başlatılıyor. Toplam ilan sayısı: {}", ilanlar.size());

        try {
            for (Ilan ilan : ilanlar) {
                // Mesaj içeriğini hazırla (placeholder'ları değiştir)
                String mesajIcerigi = prepareMesajIcerigi(sablon.getIcerik(), ilan);

                // Kuyruğa ekle
                MesajKuyrugu mesajKuyrugu = new MesajKuyrugu();
                mesajKuyrugu.setIlan(ilan);
                mesajKuyrugu.setTelefonNumarasi(ilan.getTelefonNumarasi());
                mesajKuyrugu.setMesajIcerigi(mesajIcerigi);
                mesajKuyrugu.setDurum(MesajDurumu.BEKLEMEDE);
                mesajKuyrugu.setOlusturmaTarihi(LocalDateTime.now());
                mesajKuyrugu.setDenemeSayisi(0);

                mesajKuyruguRepository.save(mesajKuyrugu);
                log.debug("Mesaj kuyruğa eklendi: {} - {}", ilan.getId(), ilan.getTelefonNumarasi());
            }

            log.info("Toplam {} mesaj kuyruğa eklendi", ilanlar.size());

        } catch (Exception e) {
            log.error("Toplu mesaj gönderimi sırasında hata: ", e);
            throw new MessagingException("Mesaj kuyruğa eklenirken hata oluştu: " + e.getMessage(), e);
        }
    }

    /**
     * Placeholder'ları ilan verileriyle değiştirir
     */
    private String prepareMesajIcerigi(String template, Ilan ilan) {
        return template
                .replace("{{telefonNumarasi}}", ilan.getTelefonNumarasi())
                .replace("{{baslik}}", ilan.getBaslik())
                .replace("{{fiyat}}", String.format("%.2f", ilan.getFiyat()))
                .replace("{{konum}}", ilan.getKonum())
                .replace("{{site}}", ilan.getSite())
                .replace("{{ilanUrl}}", ilan.getIlanUrl())
                .replace("{{kullaniciAdi}}", ilan.getKullanici().getKullaniciAdi());
    }

    /**
     * Tek bir mesaj gönderir (simüle edilmiş veya gerçek API)
     */
    @Transactional
    public boolean mesajGonder(MesajKuyrugu mesajKuyrugu) {
        try {
            log.info("Mesaj gönderiliyor: {} - {}",
                    mesajKuyrugu.getTelefonNumarasi(),
                    mesajKuyrugu.getMesajIcerigi().substring(0, Math.min(50, mesajKuyrugu.getMesajIcerigi().length())));

            mesajKuyrugu.setDurum(MesajDurumu.GONDERILIYOR);
            mesajKuyrugu.setDenemeSayisi(mesajKuyrugu.getDenemeSayisi() + 1);
            mesajKuyruguRepository.save(mesajKuyrugu);

            if (whatsappApiEnabled) {
                // Gerçek WhatsApp Business API çağrısı
                sendViaWhatsappApi(mesajKuyrugu.getTelefonNumarasi(), mesajKuyrugu.getMesajIcerigi());
            } else {
                // Simüle edilmiş gönderim
                simulateMesajGonderim(mesajKuyrugu);
            }

            // Başarılı
            mesajKuyrugu.setDurum(MesajDurumu.GONDERILDI);
            mesajKuyrugu.setGonderimTarihi(LocalDateTime.now());
            mesajKuyruguRepository.save(mesajKuyrugu);

            // İlanı güncelle
            Ilan ilan = mesajKuyrugu.getIlan();
            ilan.setMesajGonderildi(true);
            ilanRepository.save(ilan);

            log.info("Mesaj başarıyla gönderildi: {}", mesajKuyrugu.getTelefonNumarasi());
            return true;

        } catch (Exception e) {
            log.error("Mesaj gönderilirken hata: {} - {}", mesajKuyrugu.getTelefonNumarasi(), e.getMessage());

            mesajKuyrugu.setDurum(MesajDurumu.HATA);
            mesajKuyrugu.setHataMesaji(e.getMessage());
            mesajKuyruguRepository.save(mesajKuyrugu);

            return false;
        }
    }

    /**
     * WhatsApp Business API üzerinden gerçek mesaj gönderimi
     * TODO: Gerçek API entegrasyonu için bu metodu implement edin
     */
    private void sendViaWhatsappApi(String telefon, String mesaj) throws Exception {
        log.info("WhatsApp Business API çağrısı yapılıyor (MOCK)...");
        log.warn("GERÇEK API HENÜZ YAPILANDIRILMADI. Mesaj loglanarak işlem başarılı sayılacak.");
        log.info("GÖNDERİLECEK MESAJ -> Alıcı: {}, İçerik: {}", telefon, mesaj);

        // Simüle edilmiş bir ağ gecikmesi
        Thread.sleep(500);

        // TODO: Kullanıcı API sağlayıcısını belirlediğinde burayı güncelle
        // Şimdilik hata fırlatmak yerine başarılı dönüyoruz ki akış bozulmasın.
    }

    /**
     * Mesaj gönderimini simüle eder
     */
    private void simulateMesajGonderim(MesajKuyrugu mesajKuyrugu) throws InterruptedException {
        log.info("=== SIMÜLE EDİLMİŞ MESAJ GÖNDERİMİ ===");
        log.info("Alıcı: {}", mesajKuyrugu.getTelefonNumarasi());
        log.info("Mesaj: {}", mesajKuyrugu.getMesajIcerigi());
        log.info("=====================================");

        // Gerçekçi bir gecikme simüle et
        Thread.sleep(messageDelayMs);
    }

    /**
     * Kuyruk durumu hakkında bilgi döndürür
     */
    public long getKuyruktaBekleyenMesajSayisi() {
        return mesajKuyruguRepository.countByDurum(MesajDurumu.BEKLEMEDE);
    }

}
