package com.intensescaper.service.scraper;

import com.intensescaper.config.SeleniumConfig;
import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.mockito.Mockito.mock;

public class SeleniumDebugRunner {

    @Test
    public void testSeleniumScraping() {
        System.out.println("==================================================================================");
        System.out.println("🔧 SELENIUM ANTI-DETECTION TEST STARTED");
        System.out.println("Testing enhanced Selenium with Cloudflare bypass...");
        System.out.println("==================================================================================");

        // 1. Setup Selenium with anti-detection
        SeleniumConfig seleniumConfig = new SeleniumConfig();
        setField(seleniumConfig, "headless", false); // Headed for visibility
        setField(seleniumConfig, "pageLoadTimeout", 30);
        setField(seleniumConfig, "implicitWait", 10);
        setField(seleniumConfig, "useStealth", true);

        WebDriver driver = seleniumConfig.webDriver();

        try {
            // 2. Navigate to test URL
            String url = "https://www.sahibinden.com/ilan/emlak-arsa-satilik-imarli-kose-arsa-750-m2-1284630368/detay";
            System.out.println("Navigating to: " + url);
            driver.get(url);

            // 3. Wait and check title
            Thread.sleep(5000);
            String title = driver.getTitle();
            System.out.println("Page title: " + title);

            if (title.contains("Bir dakika lütfen") || title.contains("Just a moment")) {
                System.out.println("❌ CLOUDFLARE CHALLENGE DETECTED - Selenium failed to bypass");
            } else {
                System.out.println("✅ SUCCESS - Page loaded without Cloudflare challenge!");
                System.out.println("Page source length: " + driver.getPageSource().length());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Test
    public void testFullSeleniumScrape() {
        System.out.println("==================================================================================");
        System.out.println("📊 FULL SELENIUM SCRAPE TEST");
        System.out.println("Testing complete scraping flow with Selenium...");
        System.out.println("==================================================================================");

        // Setup
        ApplicationContext context = mock(ApplicationContext.class);
        SahibindenScraperImpl scraper = new SahibindenScraperImpl(context, null); // null = use Selenium fallback

        setField(scraper, "maxListItems", 5);
        setField(scraper, "pageWaitMs", 5000L);
        setField(scraper, "detailWaitMs", 2000L);
        setField(scraper, "maxRetry", 2);
        setField(scraper, "randomWaitEnabled", true);
        setField(scraper, "mobileFallbackEnabled", false);

        String url = "https://www.sahibinden.com/ilan/emlak-arsa-satilik-imarli-kose-arsa-750-m2-1284630368/detay";
        Kullanici kullanici = new Kullanici();
        kullanici.setKullaniciAdi("testuser");

        try {
            List<Ilan> results = scraper.scrape(url, kullanici);
            System.out.println("Scrape finished. Found: " + results.size());
            for (Ilan ilan : results) {
                System.out.println("SELENIUM_DATA_TITLE: " + ilan.getBaslik());
                System.out.println("SELENIUM_DATA_PHONE: " + ilan.getTelefonNumarasi());
                if (ilan.getTelefonNumarasi() != null && !ilan.getTelefonNumarasi().isEmpty()
                        && !ilan.getTelefonNumarasi().startsWith("555")) {
                    System.out.println("✅ SELENIUM_SUCCESS: Real phone number extracted!");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Scrape failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
