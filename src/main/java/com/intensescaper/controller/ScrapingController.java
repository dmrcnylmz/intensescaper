package com.intensescaper.controller;

import com.intensescaper.dto.request.ScrapingBaslatRequest;
import com.intensescaper.dto.response.ScrapingIslemResponse;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.entity.ScrapingIslem;
import com.intensescaper.service.CustomUserDetailsService;
import com.intensescaper.service.ScrapingManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/scraping")
@RequiredArgsConstructor
public class ScrapingController {

    private final com.intensescaper.service.scraper.ScraperService scraperService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/baslat")
    public ResponseEntity<?> scrapingBaslat(
            @Valid @RequestBody ScrapingBaslatRequest request,
            Authentication authentication) {

        Kullanici kullanici;
        if (authentication != null && authentication.isAuthenticated()) {
            kullanici = userDetailsService.getKullanici(authentication.getName());
        } else {
            kullanici = new Kullanici();
            kullanici.setKullaniciAdi("test_user");
        }

        // Synchronous execution for UI testing
        try {
            java.util.List<com.intensescaper.entity.Ilan> ilanlar = scraperService.scrape(request.getUrl(), kullanici);
            return ResponseEntity.ok(ilanlar);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Scraping hatası: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/durum/{islemId}")
    public ResponseEntity<ScrapingIslemResponse> islemDurumu(@PathVariable String islemId) {
        ScrapingIslem islem = scrapingManagerService.getIslemDurumu(islemId);

        ScrapingIslemResponse response = ScrapingIslemResponse.builder()
                .islemId(islem.getId())
                .durum(islem.getDurum())
                .site(islem.getSite())
                .hedefUrl(islem.getHedefUrl())
                .toplamIlan(islem.getToplamIlan())
                .tamamlananIlan(islem.getTamamlananIlan())
                .ilerlemeYuzdesi(islem.getIlerlemeYuzdesi())
                .baslamaTarihi(islem.getBaslamaTarihi())
                .bitisTarihi(islem.getBitisTarihi())
                .hataMesaji(islem.getHataMesaji())
                .build();

        return ResponseEntity.ok(response);
    }

}
