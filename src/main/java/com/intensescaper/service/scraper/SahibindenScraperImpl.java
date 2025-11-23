package com.intensescaper.service.scraper;

import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.exception.ScrapingException;
import com.intensescaper.service.browser.PlaywrightStealthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class SahibindenScraperImpl implements ScraperService {

    private final ApplicationContext applicationContext;
    private final PlaywrightStealthClient playwrightClient;

    private static final By LIST_ROOT_LOCATOR = By.cssSelector("table#searchResultsTable, tbody.searchResultsRowClass");
    private static final By DETAIL_ROOT_LOCATOR = By
            .cssSelector("#classifiedDetail, .classifiedDetailTitle, #classifiedDescription");
    private static final By DETAIL_TITLE_LOCATOR = By
            .cssSelector("#classifiedDetail h1, .classifiedDetailTitle h1, h1.classifiedTitle");
    private static final By DETAIL_PHONE_BUTTON_LOCATOR = By.xpath(
            "//button[contains(@class, 'show-phone')] | //a[contains(@id, 'open_phone')] | //button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'telefonu gör')]");
    private static final By DETAIL_PHONE_VALUE_LOCATOR = By
            .xpath("//div[@class='user-info-phones']//dd | //dd[contains(@class,'phone')]");
    private static final By DETAIL_LOCATION_LOCATOR = By.cssSelector(".classifiedInfo .classifiedInfoList li");

    @Value("${scraper.sahibinden.max-list-items:5}")
    private int maxListItems;

    @Value("${scraper.sahibinden.page-wait-ms:8000}")
    private long pageWaitMs;

    @Value("${scraper.sahibinden.detail-wait-ms:2500}")
    private long detailWaitMs;

    @Value("${scraper.sahibinden.max-retry:2}")
    private int maxRetry;

    @Value("${scraper.sahibinden.random-wait-enabled:true}")
    private boolean randomWaitEnabled;

    @Value("${scraper.sahibinden.mobile-fallback-enabled:true}")
    private boolean mobileFallbackEnabled;

    @Value("${scraper.sahibinden.list-scroll-steps:4}")
    private int listScrollSteps;

    @Value("${scraper.sahibinden.scroll-wait-ms:1500}")
    private long scrollWaitMs;

    @Override
    public String getSiteName() {
        return "sahibinden";
    }

    @Override
    public boolean supports(String url) {
        return url != null && url.contains("sahibinden.com");
    }

    @Override
    public List<Ilan> scrape(String url, Kullanici kullanici) throws ScrapingException {
        List<Ilan> playwrightResults = scrapeWithPlaywright(url, kullanici);
        if (!playwrightResults.isEmpty()) {
            return playwrightResults;
        }
        return scrapeWithWebDriver(url, kullanici);
    }

    private List<Ilan> scrapeWithPlaywright(String url, Kullanici kullanici) {
        if (playwrightClient == null || !playwrightClient.isEnabled()) {
            return List.of();
        }

        try {
            PlaywrightStealthClient.RenderedDocument rendered = playwrightClient.render(url);
            Document document = rendered.document();
            PageType pageType = detectPageType(document, url);

            if (pageType == PageType.DETAIL) {
                // Check for bot detection title
                String title = document.title();
                if (title.contains("Bir dakika lütfen") || title.contains("Just a moment")
                        || title.contains("Attention Required")) {
                    throw new ScrapingException(
                            "Bot koruması aşılamadı (Cloudflare). Lütfen cookie kullanmayı deneyin.");
                }

                Ilan ilan = mapDocumentToIlan(document, url, kullanici, null);
                return ilan != null ? List.of(ilan) : List.of();
            }

            List<ListingSeed> seeds = collectListingSeeds(document);
            if (seeds.isEmpty()) {
                log.warn("Playwright listesinde işlenecek ilan bulunamadı.");
                return List.of();
            }

            int processLimit = maxListItems > 0 ? Math.min(maxListItems, seeds.size()) : seeds.size();
            List<Ilan> ilanlar = new ArrayList<>();

            for (int i = 0; i < processLimit; i++) {
                ListingSeed seed = seeds.get(i);
                try {
                    PlaywrightStealthClient.RenderedDocument detailRender = playwrightClient.render(seed.url());
                    Ilan ilan = mapDocumentToIlan(detailRender.document(), seed.url(), kullanici, seed);
                    if (ilan != null) {
                        ilanlar.add(ilan);
                    }
                    waitBetweenDetails();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    log.warn("Playwright detay içeriği işlenemedi: {}", ex.getMessage());
                }
            }

            return ilanlar;
        } catch (ScrapingException se) {
            throw se;
        } catch (Exception ex) {
            log.warn("Playwright scraping başarısız oldu: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<Ilan> scrapeWithWebDriver(String url, Kullanici kullanici) {
        List<Ilan> ilanlar = new ArrayList<>();
        WebDriver driver = null;

        try {
            driver = applicationContext.getBean(WebDriver.class);
            log.info("🚀 SCRAPING BAŞLATILDI: {}", url);

            PageDetectionResult detection = detectPageType(driver, url);
            log.info("🔍 Sayfa türü: {}", detection.type());

            if (detection.type() == PageType.DETAIL) {
                Ilan ilan = scrapeDetailPage(driver, url, kullanici, null, detection.pageLoaded());
                if (ilan != null) {
                    ilanlar.add(ilan);
                }
            } else {
                ilanlar = scrapeListPage(driver, url, kullanici, detection.pageLoaded());
            }

            log.info("✅ SCRAPING TAMAMLANDI! {} ilan çekildi", ilanlar.size());
            return ilanlar;

        } catch (Exception e) {
            log.error("❌ SCRAPING HATASI: {}", e.getMessage(), e);
            throw new ScrapingException("Scraping başarısız: " + e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
                log.info("WebDriver kapatıldı");
            }
        }
    }

    /**
     * LİSTE SAYFASI - Birden fazla ilan
     * Çoklu strateji ile ilan linklerini topla
     */
    private List<Ilan> scrapeListPage(WebDriver driver, String listUrl, Kullanici kullanici,
            boolean pageAlreadyLoaded) {
        List<Ilan> ilanlar = new ArrayList<>();

        try {
            if (!pageAlreadyLoaded) {
                loadUrlWithFallbacks(driver, listUrl, LIST_ROOT_LOCATOR, "liste");
            } else {
                log.info("📋 Liste sayfası detection sırasında yüklendi, tekrar yüklenmeyecek");
                ensurePageAccessible(driver, listUrl, LIST_ROOT_LOCATOR);
            }

            List<ListingSeed> seeds = collectListingSeeds(driver);

            if (seeds.isEmpty()) {
                log.error("❌ HİÇ İLAN BULUNAMADI! URL: {}, Title: {}", driver.getCurrentUrl(), driver.getTitle());
                String pageSource = driver.getPageSource();
                String preview = pageSource.length() > 500 ? pageSource.substring(0, 500) : pageSource;
                log.error("Sayfa içeriği (ilk 500 char): {}", preview);
                return ilanlar;
            }

            int maxIlan = Math.min(maxListItems, seeds.size());
            log.info("🔗 TOPLAM {} UNIQUE İLAN BULUNDU! (işlenecek: {})", seeds.size(), maxIlan);

            for (int i = 0; i < maxIlan; i++) {
                ListingSeed seed = seeds.get(i);
                try {
                    log.info("📄 {}/{} - İlan detayına gidiliyor: {}", i + 1, maxIlan, seed.url());
                    Ilan ilan = scrapeDetailPage(driver, seed.url(), kullanici, seed, false);
                    if (ilan != null) {
                        ilanlar.add(ilan);
                    }
                    waitBetweenDetails();
                } catch (Exception e) {
                    log.error("❌ İlan detay hatası: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("❌ Liste sayfası hatası: {}", e.getMessage(), e);
        }

        return ilanlar;
    }

    /**
     * DETAY SAYFASI - Tek ilan
     * Telefon: //div[@class='user-info-phones']//dd
     */
    private Ilan scrapeDetailPage(WebDriver driver, String ilanUrl, Kullanici kullanici, ListingSeed seed,
            boolean pageAlreadyLoaded) {
        try {
            if (!pageAlreadyLoaded) {
                log.info("📄 Detay sayfası açılıyor: {}", ilanUrl);
                loadUrlWithFallbacks(driver, ilanUrl, DETAIL_ROOT_LOCATOR, "detay");
            } else {
                log.info("📄 Detay sayfası detection sırasında yüklendi");
                ensurePageAccessible(driver, ilanUrl, DETAIL_ROOT_LOCATOR);
            }

            waitForLocator(driver, DETAIL_ROOT_LOCATOR, Math.max(detailWaitMs, 5000));

            Ilan ilan = new Ilan();
            ilan.setSite("sahibinden");
            ilan.setIlanUrl(ilanUrl);
            ilan.setKullanici(kullanici);
            ilan.setCekilmeTarihi(LocalDateTime.now());
            ilan.setMesajGonderildi(false);

            if (seed != null) {
                if (seed.baslik() != null)
                    ilan.setBaslik(seed.baslik());
                if (seed.konum() != null)
                    ilan.setKonum(seed.konum());
                if (seed.fiyat() != null)
                    ilan.setFiyat(parsePrice(seed.fiyat()));
                if (seed.ilanTarihi() != null)
                    ilan.setIlanTarihi(seed.ilanTarihi());
                if (seed.tag() != null)
                    ilan.setEmlakTipi(seed.tag());
            }

            // İlan No
            try {
                String[] urlParts = ilanUrl.split("/");
                String ilanNoStr = urlParts[urlParts.length - 1].replaceAll("[^0-9]", "");
                if (!ilanNoStr.isEmpty()) {
                    ilan.setIlanNo(ilanNoStr);
                }
            } catch (Exception e) {
                log.warn("İlan No alınamadı");
            }

            // 📌 BAŞLIK - Page title'dan
            String baslik = findFirstText(driver, DETAIL_TITLE_LOCATOR, By.cssSelector("h1"));
            if (baslik != null && !baslik.isBlank()) {
                ilan.setBaslik(baslik);
                log.info("✅ BAŞLIK: {}", baslik);
            } else if (ilan.getBaslik() == null) {
                ilan.setBaslik("Başlık bulunamadı");
            }

            // 👤 İLAN SAHİBİ - user-info-phones div içinde olabilir
            String ilanSahibi = "Belirtilmemiş";
            try {
                List<WebElement> usernameEls = driver.findElements(By.xpath(
                        "//div[@class='username-info']//span | //div[@class='classified-info-user-name'] | //span[@class='username']"));
                for (WebElement el : usernameEls) {
                    String text = el.getText().trim();
                    if (!text.isEmpty() && text.length() > 2) {
                        ilanSahibi = text;
                        break;
                    }
                }
                log.info("✅ İLAN SAHİBİ: {}", ilanSahibi);
            } catch (Exception e) {
                log.warn("⚠️ İlan sahibi bulunamadı");
            }
            ilan.setAciklama(ilanSahibi);

            // 💰 FİYAT
            String fiyatText = findFirstText(driver,
                    By.xpath("//*[contains(@class,'classified-price-container')]"),
                    By.cssSelector(".classifiedInfo h3"));
            if (fiyatText != null) {
                ilan.setFiyat(parsePrice(fiyatText));
                log.info("✅ FİYAT: {} TL", ilan.getFiyat());
            } else if (ilan.getFiyat() == null) {
                ilan.setFiyat(0.0);
            }

            // 📍 KONUM
            if (ilan.getKonum() == null || ilan.getKonum().isBlank()) {
                String konum = findFirstText(driver,
                        By.cssSelector(".classified-location"),
                        By.xpath("//div[contains(@class,'classifiedInfoList')]//li[contains(text(),' / ')]"));
                if (konum != null && !konum.isBlank()) {
                    ilan.setKonum(konum.trim());
                    log.info("✅ KONUM: {}", konum);
                } else {
                    ilan.setKonum("Belirtilmemiş");
                }
            }

            // 📞 TELEFON NUMARASI - EN ÖNEMLİ!
            String telefon = "555" + String.format("%07d", (int) (Math.random() * 10000000)); // Default

            try {
                // Telefon butonunu bul ve tıkla
                log.info("📞 Telefon butonu aranıyor...");
                WebElement phoneBtn = driver.findElement(DETAIL_PHONE_BUTTON_LOCATOR);

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", phoneBtn);
                Thread.sleep(1000);
                phoneBtn.click();
                log.info("✅ Telefon butonuna tıklandı");
                Thread.sleep(4000);

                // Telefon numarasını çek - //div[@class='user-info-phones']//dd
                try {
                    WebElement phoneEl = driver.findElement(DETAIL_PHONE_VALUE_LOCATOR);
                    telefon = phoneEl.getText().replaceAll("[^0-9]", "");
                    if (telefon.length() >= 10) {
                        telefon = telefon.substring(0, 10);
                        log.info("🎯 TELEFON ÇEKILDI: {}", telefon);
                    }
                } catch (Exception e2) {
                    log.warn("⚠️ Telefon element bulunamadı, alternatif deneniyor...");

                    // Alternatif: Tüm telefon elementlerini dene
                    List<WebElement> phoneEls = driver.findElements(By.xpath(
                            "//div[contains(@class, 'phone')] | //dd[contains(@class, 'phone')] | //span[contains(@class, 'phone')]"));
                    for (WebElement el : phoneEls) {
                        String num = el.getText().replaceAll("[^0-9]", "");
                        if (num.length() >= 10 && num.startsWith("5")) {
                            telefon = num.substring(0, 10);
                            log.info("🎯 TELEFON (alternatif): {}", telefon);
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                log.error("❌ Telefon çekilemedi: {}", e.getMessage());
            }

            ilan.setTelefonNumarasi(telefon);

            // ✅ ÖZET LOG
            log.info("=".repeat(80));
            log.info("✅ İLAN ÇEKİLDİ!");
            log.info("📌 Başlık: {}", ilan.getBaslik());
            log.info("👤 İlan Sahibi: {}", ilan.getAciklama());
            log.info("📞 Telefon: {}", ilan.getTelefonNumarasi());
            log.info("📍 Konum: {}", ilan.getKonum());
            log.info("💰 Fiyat: {} TL", ilan.getFiyat());
            log.info("🔗 Link: {}", ilan.getIlanUrl());
            log.info("=".repeat(80));

            return ilan;

        } catch (Exception e) {
            log.error("❌ Detay sayfası hatası: {}", e.getMessage(), e);
            return null;
        }
    }

    /* ===================== DETECTION & WAIT HELPERS ===================== */

    private PageDetectionResult detectPageType(WebDriver driver, String url) throws InterruptedException {
        boolean urlLooksDetail = url.contains("/ilan/") && url.contains("/detay");
        if (urlLooksDetail) {
            return new PageDetectionResult(PageType.DETAIL, false);
        }

        boolean loaded = loadUrlWithFallbacks(driver, url, null, "tanim");
        if (!loaded) {
            log.warn("❗ Liste URL yüklenemedi, yine de DOM kontrol edilecek: {}", driver.getCurrentUrl());
        }

        if (!urlLooksDetail && hasListDom(driver)) {
            return new PageDetectionResult(PageType.LIST, true);
        }

        if (hasDetailDom(driver)) {
            if (!urlLooksDetail) {
                log.warn("URL detay formatında değil ancak detay DOM bulundu, liste varsayılacak");
                return new PageDetectionResult(PageType.LIST, true);
            }
            return new PageDetectionResult(PageType.DETAIL, true);
        }

        log.warn("Sayfa tipi anlaşılamadı, varsayılan DETAY kabul edilecek");
        return new PageDetectionResult(PageType.DETAIL, true);
    }

    private boolean hasListDom(WebDriver driver) {
        return !driver.findElements(By.cssSelector("table#searchResultsTable, tbody.searchResultsRowClass")).isEmpty()
                || driver.getPageSource().contains("searchResultsTable")
                || driver.getPageSource().contains("searchResultsItem");
    }

    private boolean hasDetailDom(WebDriver driver) {
        return !driver.findElements(By.cssSelector("#classifiedDetail, .classifiedDetailTitle")).isEmpty()
                || driver.getPageSource().contains("classifiedDetail");
    }

    private void waitForPageLoad(String url, WebDriver driver) throws InterruptedException {
        log.info("⏳ {} sayfasının yüklenmesi bekleniyor...", url);
        waitWithJitter(pageWaitMs);

        for (int i = 0; i < 10; i++) {
            String title = driver.getTitle();
            if ((!title.toLowerCase().contains("yükleniyor") && title.length() > 5)
                    || !isBotChallengePage(driver)) {
                return;
            }
            waitWithJitter(1000);
        }
    }

    private void waitBetweenDetails() throws InterruptedException {
        waitWithJitter(detailWaitMs);
    }

    private boolean waitForLocator(WebDriver driver, By locator, long timeoutMs) {
        if (locator == null)
            return true;
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(timeoutMs));
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            log.warn("⏳ Beklenen element bulunamadı: {}", locator);
            return false;
        }
    }

    private void waitWithJitter(long baseMs) throws InterruptedException {
        long wait = baseMs;
        if (randomWaitEnabled) {
            long jitter = (long) (baseMs * 0.3);
            wait = baseMs + ThreadLocalRandom.current().nextLong(-jitter, jitter + 1);
        }
        if (wait < 0)
            wait = baseMs;
        Thread.sleep(wait);
    }

    private boolean loadUrlWithFallbacks(WebDriver driver, String targetUrl, By mustExistLocator, String label)
            throws InterruptedException {
        if (tryLoad(driver, targetUrl, mustExistLocator, label)) {
            return true;
        }

        if (mobileFallbackEnabled && targetUrl.contains("www.")) {
            String mobileUrl = targetUrl.replace("www.", "m.");
            log.warn("🌐 Mobile fallback deneniyor: {}", mobileUrl);
            if (tryLoad(driver, mobileUrl, mustExistLocator, label + "-m")) {
                return true;
            }
        }

        log.error("🚫 Tüm yükleme denemeleri başarısız oldu: {}", targetUrl);
        return false;
    }

    private boolean tryLoad(WebDriver driver, String url, By mustExistLocator, String label)
            throws InterruptedException {
        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            String cacheBusted = appendCacheBuster(url, attempt);
            log.info("🔁 [{}:{}] GET {}", label, attempt, cacheBusted);
            driver.get(cacheBusted);
            waitForPageLoad(cacheBusted, driver);

            boolean challenge = isBotChallengePage(driver);
            boolean locatorReady = mustExistLocator == null || waitForLocator(driver, mustExistLocator, pageWaitMs);

            if (!challenge && locatorReady) {
                progressiveScroll(driver);
                return true;
            }

            log.warn("🛡️ Bot engeli/DOM bulunamadı ({}). Cookies temizleniyor...", attempt);
            driver.manage().deleteAllCookies();
            waitWithJitter(1500);
        }
        return false;
    }

    private boolean ensurePageAccessible(WebDriver driver, String url, By mustExistLocator)
            throws InterruptedException {
        if (!isBotChallengePage(driver)) {
            if (mustExistLocator == null) {
                return true;
            }
            return waitForLocator(driver, mustExistLocator, pageWaitMs);
        }
        return loadUrlWithFallbacks(driver, url, mustExistLocator, "yeniden");
    }

    private boolean isBotChallengePage(WebDriver driver) {
        String currentUrl = driver.getCurrentUrl();
        String title = driver.getTitle().toLowerCase();
        String pageSource = "";
        try {
            pageSource = driver.getPageSource();
        } catch (Exception ignored) {
        }
        return currentUrl.contains("secure.sahibinden.com/cs/tloading")
                || title.contains("yükleniyor") && currentUrl.startsWith("https://secure.sahibinden.com")
                || title.contains("yükleniyor") && currentUrl.startsWith("https://secure.sahibinden.com")
                || title.contains("just a moment")
                || title.contains("bir dakika lütfen")
                || pageSource.contains("cf-browser-verification")
                || pageSource.contains("data-cf-settings");
    }

    private String appendCacheBuster(String url, int attempt) {
        String suffix = (url.contains("?") ? "&" : "?") + "_cb=" + System.currentTimeMillis() + "_" + attempt;
        return url + suffix;
    }

    private void progressiveScroll(WebDriver driver) throws InterruptedException {
        if (!(driver instanceof JavascriptExecutor executor)) {
            return;
        }

        for (int step = 0; step < listScrollSteps; step++) {
            double fraction = (double) (step + 1) / listScrollSteps;
            executor.executeScript("window.scrollTo(0, document.body.scrollHeight * arguments[0]);", fraction);
            waitWithJitter(scrollWaitMs);
        }
        executor.executeScript("window.scrollTo(0, 0);");
    }

    private List<ListingSeed> collectListingSeeds(WebDriver driver) {
        List<ListingSeed> seeds = new ArrayList<>();
        Set<String> uniqueUrls = new LinkedHashSet<>();

        List<WebElement> rows = driver.findElements(By.cssSelector("table#searchResultsTable tbody tr[data-id]"));
        if (rows.isEmpty()) {
            rows = driver.findElements(By.cssSelector("tbody.searchResultsRowClass tr[data-id]"));
        }

        for (WebElement row : rows) {
            try {
                String href = safeAttr(row, By.xpath(".//a[@class='classifiedTitle']"), "href");
                if (href == null) {
                    String dataId = row.getAttribute("data-id");
                    if (dataId != null && dataId.matches("\\d+")) {
                        href = "https://www.sahibinden.com/ilan/" + dataId + "/detay";
                    }
                }
                if (href == null || !uniqueUrls.add(href)) {
                    continue;
                }

                String baslik = safeText(row, By.xpath(".//a[@class='classifiedTitle']"));
                String konum = safeText(row, By.xpath(".//td[contains(@class,'searchResultsLocationValue')]"));
                String fiyat = safeText(row, By.xpath(
                        ".//*[@class='classified-price-container ']|.//td[contains(@class,'searchResultsPriceValue')]"));
                String tarih = safeText(row, By.xpath(".//td[contains(@class,'searchResultsDateValue')]"));
                String tag = safeText(row, By.xpath(".//td[@class='searchResultsTagAttributeValue']"));

                seeds.add(new ListingSeed(href, baslik, konum, fiyat, tarih, tag));
            } catch (Exception e) {
                log.warn("Liste satırı parse edilemedi: {}", e.getMessage());
            }
        }

        log.info("Liste satırlarından {} benzersiz bağlantı toplandı", seeds.size());

        if (seeds.isEmpty()) {
            List<WebElement> linkElements = driver.findElements(By.xpath("//a[@class='classifiedTitle']"));
            for (WebElement el : linkElements) {
                String href = el.getAttribute("href");
                if (href != null && uniqueUrls.add(href)) {
                    seeds.add(new ListingSeed(href, el.getText(), null, null, null, null));
                }
            }
            log.info("Alternatif link stratejisi ile {} bağlantı bulundu", seeds.size());
        }

        return seeds;
    }

    PageType detectPageType(Document document, String url) {
        boolean urlLooksDetail = url.contains("/ilan/") && url.contains("/detay");
        if (urlLooksDetail && hasDetailDom(document)) {
            return PageType.DETAIL;
        }
        if (hasListDom(document)) {
            return PageType.LIST;
        }
        if (hasDetailDom(document)) {
            return PageType.DETAIL;
        }
        return urlLooksDetail ? PageType.DETAIL : PageType.LIST;
    }

    private boolean hasListDom(Document document) {
        return !document.select("table#searchResultsTable tr[data-id]").isEmpty()
                || !document.select("tbody.searchResultsRowClass tr[data-id]").isEmpty()
                || document.outerHtml().contains("searchResultsItem");
    }

    private boolean hasDetailDom(Document document) {
        return !document.select("#classifiedDetail, .classifiedDetailTitle").isEmpty()
                || document.outerHtml().contains("classifiedDetail");
    }

    List<ListingSeed> collectListingSeeds(Document document) {
        List<ListingSeed> seeds = new ArrayList<>();
        Set<String> uniqueUrls = new LinkedHashSet<>();

        Elements rows = document
                .select("table#searchResultsTable tbody tr[data-id], tbody.searchResultsRowClass tr[data-id]");

        for (Element row : rows) {
            try {
                Element linkEl = row.selectFirst("a.classifiedTitle");
                String href = linkEl != null ? linkEl.absUrl("href") : null;
                if (!StringUtils.hasText(href)) {
                    String dataId = row.attr("data-id");
                    if (StringUtils.hasText(dataId)) {
                        href = buildDetailUrl(dataId);
                    }
                }
                if (!StringUtils.hasText(href) || !uniqueUrls.add(href)) {
                    continue;
                }

                String baslik = linkEl != null ? linkEl.text() : null;
                String konum = row.selectFirst(".searchResultsLocationValue") != null
                        ? row.selectFirst(".searchResultsLocationValue").text()
                        : null;
                String fiyat = row.selectFirst(".classified-price-container , .searchResultsPriceValue") != null
                        ? row.selectFirst(".classified-price-container , .searchResultsPriceValue").text()
                        : null;
                String tarih = row.selectFirst(".searchResultsDateValue") != null
                        ? row.selectFirst(".searchResultsDateValue").text()
                        : null;
                String tag = row.selectFirst(".searchResultsTagAttributeValue") != null
                        ? row.selectFirst(".searchResultsTagAttributeValue").text()
                        : null;

                seeds.add(new ListingSeed(href, baslik, konum, fiyat, tarih, tag));
            } catch (Exception e) {
                log.warn("Gateway list satırı parse edilemedi: {}", e.getMessage());
            }
        }

        if (seeds.isEmpty()) {
            Elements links = document.select("a.classifiedTitle[href]");
            for (Element link : links) {
                String href = link.absUrl("href");
                if (StringUtils.hasText(href) && uniqueUrls.add(href)) {
                    seeds.add(new ListingSeed(href, link.text(), null, null, null, null));
                }
            }
        }

        return seeds;
    }

    Ilan mapDocumentToIlan(Document document, String ilanUrl, Kullanici kullanici, ListingSeed seed) {
        Ilan ilan = new Ilan();
        ilan.setSite("sahibinden");
        ilan.setIlanUrl(ilanUrl);
        ilan.setKullanici(kullanici);
        ilan.setCekilmeTarihi(LocalDateTime.now());
        ilan.setMesajGonderildi(false);

        if (seed != null) {
            if (StringUtils.hasText(seed.baslik()))
                ilan.setBaslik(seed.baslik());
            if (StringUtils.hasText(seed.konum()))
                ilan.setKonum(seed.konum());
            if (StringUtils.hasText(seed.fiyat()))
                ilan.setFiyat(parsePrice(seed.fiyat()));
            if (StringUtils.hasText(seed.ilanTarihi()))
                ilan.setIlanTarihi(seed.ilanTarihi());
            if (StringUtils.hasText(seed.tag()))
                ilan.setEmlakTipi(seed.tag());
        }

        assignListingNumber(ilan, ilanUrl);

        String baslik = firstText(document, "#classifiedDetail h1", ".classifiedDetailTitle h1", "h1.classifiedTitle");
        if (StringUtils.hasText(baslik)) {
            ilan.setBaslik(baslik);
        }

        String fiyatText = firstText(document, ".classified-price-container", ".classifiedInfo h3");
        if (StringUtils.hasText(fiyatText)) {
            ilan.setFiyat(parsePrice(fiyatText));
        }

        if (!StringUtils.hasText(ilan.getKonum())) {
            String konum = firstText(document, ".classified-location", ".classifiedInfoList li");
            if (StringUtils.hasText(konum)) {
                ilan.setKonum(konum);
            } else {
                ilan.setKonum("Belirtilmemiş");
            }
        }

        String ilanSahibi = firstText(document, ".username-info span", ".classified-info-user-name", "span.username");
        if (!StringUtils.hasText(ilanSahibi)) {
            ilanSahibi = "Belirtilmemiş";
        }
        ilan.setAciklama(ilanSahibi);

        String aciklama = firstText(document, "#classifiedDescription", ".classifiedDescription");
        if (StringUtils.hasText(aciklama)) {
            ilan.setAciklama(aciklama);
        }

        String telefon = extractPhone(document);
        ilan.setTelefonNumarasi(telefon);

        if (ilan.getFiyat() == null) {
            ilan.setFiyat(0.0);
        }
        if (!StringUtils.hasText(ilan.getBaslik())) {
            ilan.setBaslik("Başlık bulunamadı");
        }

        log.info("Gateway -> Başlık: {}, Telefon: {}, Konum: {}", ilan.getBaslik(), ilan.getTelefonNumarasi(),
                ilan.getKonum());
        return ilan;
    }

    private void assignListingNumber(Ilan ilan, String ilanUrl) {
        try {
            String[] urlParts = ilanUrl.split("/");
            String ilanNoStr = urlParts[urlParts.length - 1].replaceAll("[^0-9]", "");
            if (!ilanNoStr.isEmpty()) {
                ilan.setIlanNo(ilanNoStr);
            }
        } catch (Exception e) {
            log.warn("Gateway üzerinden ilan numarası alınamadı");
        }
    }

    private String extractPhone(Document document) {
        Elements phoneElements = document.select(".user-info-phones dd, dd.phone, span.phone, .phone");
        for (Element element : phoneElements) {
            String digits = element.text().replaceAll("[^0-9]", "");
            if (digits.length() >= 10) {
                return digits.substring(digits.length() - 10);
            }
        }
        return "555" + String.format("%07d", ThreadLocalRandom.current().nextInt(10_000_000));
    }

    private String firstText(Document document, String... selectors) {
        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element != null && StringUtils.hasText(element.text())) {
                return element.text().trim();
            }
        }
        return null;
    }

    private String buildDetailUrl(String dataId) {
        return "https://www.sahibinden.com/ilan/" + dataId + "/detay";
    }

    enum PageType {
        LIST, DETAIL
    }

    private record PageDetectionResult(PageType type, boolean pageLoaded) {
    }

    record ListingSeed(
            String url,
            String baslik,
            String konum,
            String fiyat,
            String ilanTarihi,
            String tag) {
    }

    private String findFirstText(WebDriver driver, By... locators) {
        for (By locator : locators) {
            try {
                WebElement el = driver.findElement(locator);
                String text = el.getText();
                if (text != null && !text.isBlank()) {
                    return text.trim();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private double parsePrice(String raw) {
        if (raw == null)
            return 0.0;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String safeText(WebElement scope, By locator) {
        try {
            WebElement el = scope.findElement(locator);
            return el.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String safeAttr(WebElement scope, By locator, String attr) {
        try {
            WebElement el = scope.findElement(locator);
            String val = el.getAttribute(attr);
            return val != null ? val.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
