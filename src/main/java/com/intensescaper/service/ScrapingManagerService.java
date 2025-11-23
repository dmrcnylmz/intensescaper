package com.intensescaper.service;

import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.entity.ScrapingIslem;
import com.intensescaper.enums.IslemDurumu;
import com.intensescaper.exception.ScrapingException;
import com.intensescaper.repository.IlanRepository;
import com.intensescaper.repository.ScrapingIslemRepository;
import com.intensescaper.service.scraper.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapingManagerService {

    private final List<ScraperService> scraperServices;
    private final ScrapingIslemRepository scrapingIslemRepository;
    private final IlanRepository ilanRepository;

    /**
     * Scraping işlemini başlatır ve işlem ID'si döndürür
     */
    public String startScraping(String url, Kullanici kullanici) {
        // Uygun scraper'ı bul
        Optional<ScraperService> scraperOpt = scraperServices.stream()
                .filter(scraper -> scraper.supports(url))
                .findFirst();

        if (scraperOpt.isEmpty()) {
            throw new ScrapingException("Desteklenmeyen site URL'si: " + url);
        }

        ScraperService scraper = scraperOpt.get();

        // Scraping işlemi kaydı oluştur
        ScrapingIslem islem = new ScrapingIslem();
        islem.setId(UUID.randomUUID().toString());
        islem.setDurum(IslemDurumu.BASLADI);
        islem.setHedefUrl(url);
        islem.setSite(scraper.getSiteName());
        // islem.setKullanici(kullanici); // Skip for testing without authentication
        islem.setBaslamaTarihi(LocalDateTime.now());

        scrapingIslemRepository.save(islem);

        log.info("Scraping işlemi başlatıldı. İşlem ID: {}", islem.getId());

        // Asenkron olarak scraping'i başlat
        scrapeAsync(islem.getId(), url, kullanici, scraper);

        return islem.getId();
    }

    /**
     * Asenkron olarak scraping işlemini gerçekleştirir
     */
    @Async("taskExecutor")
    @Transactional
    public void scrapeAsync(String islemId, String url, Kullanici kullanici, ScraperService scraper) {
        log.info("Asenkron scraping başlıyor. İşlem ID: {}", islemId);

        ScrapingIslem islem = scrapingIslemRepository.findById(islemId)
                .orElseThrow(() -> new ScrapingException("İşlem bulunamadı: " + islemId));

        int maxRetries = 3;
        int retryCount = 0;
        List<Ilan> ilanlar = null;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                // Durumu güncelle
                islem.setDurum(IslemDurumu.DEVAM_EDIYOR);
                scrapingIslemRepository.save(islem);

                // Scraping işlemini gerçekleştir
                ilanlar = scraper.scrape(url, kullanici);
                break; // Başarılı olursa döngüden çık

            } catch (Exception e) {
                lastException = e;
                retryCount++;
                log.warn("Scraping denemesi {} / {} başarısız oldu: {}", retryCount, maxRetries, e.getMessage());

                if (retryCount < maxRetries) {
                    try {
                        // Exponential backoff: 2, 4, 8 saniye bekle
                        long waitTime = (long) Math.pow(2, retryCount) * 1000;
                        log.info("{} ms bekleniyor...", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        try {
            if (ilanlar != null && !ilanlar.isEmpty()) {
                // Başarılı - İlanları kaydet

                // İlanları kaydet
                islem.setToplamIlan(ilanlar.size());

                for (Ilan ilan : ilanlar) {
                    ilan.setScrapingIslem(islem);
                    ilanRepository.save(ilan);
                    islem.incrementTamamlanan();
                    scrapingIslemRepository.save(islem);
                }

                // İşlem tamamlandı
                islem.setDurum(IslemDurumu.TAMAMLANDI);
                islem.setBitisTarihi(LocalDateTime.now());
                scrapingIslemRepository.save(islem);

                log.info("Scraping işlemi başarıyla tamamlandı. İşlem ID: {}, Toplam ilan: {}",
                        islemId, ilanlar.size());
            } else {
                // Tüm denemeler başarısız
                islem.setDurum(IslemDurumu.HATA);
                String errorMsg = lastException != null ? lastException.getMessage() : "Bilinmeyen hata";
                islem.setHataMesaji("Scraping " + retryCount + " denemeden sonra başarısız oldu: " + errorMsg);
                islem.setBitisTarihi(LocalDateTime.now());
                scrapingIslemRepository.save(islem);

                log.error("Scraping işlemi {} denemeden sonra başarısız oldu. İşlem ID: {}",
                        retryCount, islemId, lastException);
            }

        } catch (Exception e) {
            log.error("Scraping işlemi sırasında beklenmeyen hata oluştu. İşlem ID: {}", islemId, e);

            islem.setDurum(IslemDurumu.HATA);
            islem.setHataMesaji(e.getMessage());
            islem.setBitisTarihi(LocalDateTime.now());
            scrapingIslemRepository.save(islem);
        }
    }

    /**
     * İşlem durumunu döndürür
     */
    public ScrapingIslem getIslemDurumu(String islemId) {
        return scrapingIslemRepository.findById(islemId)
                .orElseThrow(() -> new ScrapingException("İşlem bulunamadı: " + islemId));
    }

}
