package com.intensescaper.service.scraper;

import com.intensescaper.config.PlaywrightProperties;
import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.proxy.ProxyRotator;
import com.intensescaper.service.browser.HumanBehaviorSimulator;
import com.intensescaper.service.browser.PlaywrightSessionStore;
import com.intensescaper.service.browser.PlaywrightStealthClient;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScraperDebugRunner {

    @Test
    public void testRealScraping() {
        // 1. Setup Dependencies
        PlaywrightProperties properties = new PlaywrightProperties();
        properties.setEnabled(true);
        properties.setHeadless(false); // Try HEADED to see if session works
        // Enable stealth
        properties.getStealth().setSpoofNavigator(true);
        properties.getStealth().setSpoofPlugins(true);
        properties.getStealth().setSpoofWebdriverFlag(true);

        // Mock other dependencies
        ProxyRotator proxyRotator = mock(ProxyRotator.class);
        when(proxyRotator.acquire()).thenReturn(Optional.empty()); // No proxy for now

        PlaywrightSessionStore sessionStore = new PlaywrightSessionStore();
        HumanBehaviorSimulator humanBehaviorSimulator = new HumanBehaviorSimulator(); // Use real or mock? Let's use
                                                                                      // simple mock if complex
        // Actually HumanBehaviorSimulator might be simple enough to use real if we had
        // it, but let's mock it to isolate
        HumanBehaviorSimulator mockHuman = mock(HumanBehaviorSimulator.class);

        PlaywrightStealthClient client = new PlaywrightStealthClient(
                properties, proxyRotator, sessionStore, mockHuman);

        ApplicationContext context = mock(ApplicationContext.class);

        SahibindenScraperImpl scraper = new SahibindenScraperImpl(context, client);

        // Inject values using reflection or setters if available, or just rely on
        // defaults if they are fields
        // The fields are private with @Value. We need to set them via reflection or
        // constructor if possible.
        // Since they are field injected, we might have trouble.
        // Let's try to use reflection to set the fields.

        setField(scraper, "maxListItems", 5);
        setField(scraper, "pageWaitMs", 5000L);
        setField(scraper, "detailWaitMs", 2000L);
        setField(scraper, "maxRetry", 2);
        setField(scraper, "randomWaitEnabled", true);
        setField(scraper, "mobileFallbackEnabled", false);

        // 2. Run Scraper
        String url = "https://www.sahibinden.com/ilan/emlak-arsa-satilik-imarli-kose-arsa-750-m2-1284630368/detay";
        Kullanici kullanici = new Kullanici();
        kullanici.setKullaniciAdi("testuser");

        System.out.println("Starting scrape for: " + url);
        try {
            List<Ilan> results = scraper.scrape(url, kullanici);
            System.out.println("Scrape finished. Found: " + results.size());
            for (Ilan ilan : results) {
                System.out.println("DATA_CHECK_TITLE: " + ilan.getBaslik());
                System.out.println("DATA_CHECK_PHONE: " + ilan.getTelefonNumarasi());
                if (ilan.getTelefonNumarasi() != null && !ilan.getTelefonNumarasi().isEmpty()) {
                    System.out.println("DATA_CHECK_SUCCESS: Phone number found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void primeSession() {
        System.out.println("==================================================================================");
        System.out.println("🚀 SESSION PRIMING MODE STARTED");
        System.out.println("The browser will open. Please manually solve any Cloudflare challenge (Turnstile).");
        System.out.println("You have 120 seconds.");
        System.out.println("==================================================================================");

        // 1. Setup Dependencies
        PlaywrightProperties properties = new PlaywrightProperties();
        properties.setEnabled(true);
        properties.setHeadless(false); // HEADED MODE for manual interaction
        properties.setNavigationTimeoutMs(120000); // Long timeout

        // Enable stealth
        properties.getStealth().setSpoofNavigator(true);
        properties.getStealth().setSpoofPlugins(true);
        properties.getStealth().setSpoofWebdriverFlag(true);
        properties.getSession().setPersistCookies(true); // Important!

        ProxyRotator proxyRotator = mock(ProxyRotator.class);
        when(proxyRotator.acquire()).thenReturn(Optional.empty());

        PlaywrightSessionStore sessionStore = new PlaywrightSessionStore();
        HumanBehaviorSimulator mockHuman = mock(HumanBehaviorSimulator.class);

        PlaywrightStealthClient client = new PlaywrightStealthClient(
                properties, proxyRotator, sessionStore, mockHuman);

        ApplicationContext context = mock(ApplicationContext.class);
        SahibindenScraperImpl scraper = new SahibindenScraperImpl(context, client);

        // Inject fields
        setField(scraper, "maxListItems", 5);
        setField(scraper, "pageWaitMs", 5000L);
        setField(scraper, "detailWaitMs", 2000L);
        setField(scraper, "maxRetry", 2);
        setField(scraper, "randomWaitEnabled", true);
        setField(scraper, "mobileFallbackEnabled", false);

        // 2. Run Scraper on a listing page to trigger challenge
        String url = "https://www.sahibinden.com/satilik-daire"; // Generic listing page

        try {
            scraper.scrape(url, new Kullanici());
            System.out.println("✅ Session primed successfully!");
        } catch (Exception e) {
            System.out
                    .println("⚠️ Scrape finished with error (expected if you just solved captcha): " + e.getMessage());
            // The session should still be saved by the finally block in
            // PlaywrightStealthClient
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
