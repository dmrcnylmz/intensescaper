package com.intensescaper.scheduler;

import com.intensescaper.entity.MesajKuyrugu;
import com.intensescaper.enums.MesajDurumu;
import com.intensescaper.repository.MesajKuyruguRepository;
import com.intensescaper.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MesajGondericiScheduler {

    private final MesajKuyruguRepository mesajKuyruguRepository;
    private final WhatsappService whatsappService;

    @Value("${whatsapp.message.max-retry}")
    private int maxRetry;

    /**
     * Her 60 saniyede bir çalışarak beklemedeki mesajları işler
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000) // 60 saniye, 10 saniye başlangıç gecikmesi
    @Transactional
    public void processMesajKuyrugu() {
        log.debug("Mesaj kuyruğu kontrol ediliyor...");

        try {
            // Beklemedeki mesajları al (en fazla 10 tane)
            List<MesajKuyrugu> bekleyenMesajlar = mesajKuyruguRepository
                    .findByDurumOrderByOlusturmaTarihiAsc(MesajDurumu.BEKLEMEDE, PageRequest.of(0, 10));

            if (bekleyenMesajlar.isEmpty()) {
                log.debug("Kuyrukta bekleyen mesaj yok");
                return;
            }

            log.info("Kuyrukta {} mesaj bulundu, işleniyor...", bekleyenMesajlar.size());

            for (MesajKuyrugu mesaj : bekleyenMesajlar) {
                try {
                    // Maksimum deneme sayısını aşmış mı kontrol et
                    if (mesaj.getDenemeSayisi() >= maxRetry) {
                        log.warn("Mesaj maksimum deneme sayısını aştı, hata olarak işaretleniyor: {}", 
                                 mesaj.getTelefonNumarasi());
                        mesaj.setDurum(MesajDurumu.HATA);
                        mesaj.setHataMesaji("Maksimum deneme sayısı aşıldı");
                        mesajKuyruguRepository.save(mesaj);
                        continue;
                    }

                    // Mesajı gönder
                    boolean basarili = whatsappService.mesajGonder(mesaj);

                    if (!basarili) {
                        log.warn("Mesaj gönderilemedi, daha sonra tekrar denenecek: {}", 
                                 mesaj.getTelefonNumarasi());
                    }

                    // Rate limiting için kısa bir bekleme
                    Thread.sleep(2000);

                } catch (Exception e) {
                    log.error("Mesaj işlenirken hata: {} - {}", mesaj.getId(), e.getMessage());
                }
            }

            log.info("Mesaj kuyruğu işleme tamamlandı");

        } catch (Exception e) {
            log.error("Mesaj kuyruğu işlenirken beklenmeyen hata: ", e);
        }
    }

    /**
     * Her gün saat 00:00'da çalışarak hatalı mesajları temizler
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOldMessages() {
        log.info("Eski mesajlar temizleniyor...");
        
        try {
            // 30 günden eski ve hatalı olan mesajları silebiliriz
            // Şimdilik sadece log yazdırıyoruz
            long hataliMesajSayisi = mesajKuyruguRepository.countByDurum(MesajDurumu.HATA);
            log.info("Toplam {} hatalı mesaj var", hataliMesajSayisi);
            
        } catch (Exception e) {
            log.error("Mesaj temizleme işlemi sırasında hata: ", e);
        }
    }

}

