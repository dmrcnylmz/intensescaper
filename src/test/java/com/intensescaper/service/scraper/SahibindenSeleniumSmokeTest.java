package com.intensescaper.service.scraper;

import com.intensescaper.config.SeleniumConfig;
import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "selenium.chrome.headless=true",
        "selenium.chrome.page-load-timeout=40",
        "selenium.chrome.implicit-wait=10",
        "selenium.chrome.user-agents=",
        "selenium.chrome.proxy=",
        "selenium.chrome.proxies=",
        "selenium.chrome.use-stealth=true",
        "scraper.sahibinden.max-list-items=2"
})
@Import(SeleniumConfig.class)
class SahibindenSeleniumSmokeTest {

    @Autowired
    private SahibindenScraperImpl scraper;

    @Test
    void scrapeProvidedUrlsWithWebDriver() throws Exception {
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv("RUN_SELENIUM_SMOKE")),
                "RUN_SELENIUM_SMOKE=true olduğunda çalışır.");

        List<String> urls = List.of(
                "https://www.sahibinden.com/satilik-arsa/istanbul-bagcilar/sahibinden?address_region=1",
                "https://www.sahibinden.com/satilik-arsa/sahibinden?address_region=1&address_town=432&address_town=435&address_town=417"
        );

        Kullanici kullanici = new Kullanici();
        kullanici.setKullaniciAdi("selenium-smoke");

        for (String url : urls) {
            System.out.println(">>> URL: " + url);
            List<Ilan> ilanlar = scraper.scrape(url, kullanici);
            assertFalse(ilanlar.isEmpty(), "URL için ilan bulunamadı: " + url);
            ilanlar.stream()
                    .limit(2)
                    .forEach(ilan -> System.out.printf(" - %s | %s | %s | %.0f%n",
                            ilan.getBaslik(),
                            ilan.getKonum(),
                            ilan.getTelefonNumarasi(),
                            ilan.getFiyat()));
        }
    }
}

