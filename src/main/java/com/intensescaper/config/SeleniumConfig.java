package com.intensescaper.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@Slf4j
public class SeleniumConfig {

    @Value("${selenium.chrome.headless}")
    private boolean headless;

    @Value("${selenium.chrome.page-load-timeout}")
    private int pageLoadTimeout;

    @Value("${selenium.chrome.implicit-wait}")
    private int implicitWait;

    @Value("${selenium.chrome.user-agents:}")
    private String userAgents;

    @Value("${selenium.chrome.proxy:}")
    private String proxy;

    @Value("${selenium.chrome.proxies:}")
    private String proxies;

    @Value("${selenium.chrome.use-stealth:true}")
    private boolean useStealth;

    @Bean
    @Scope("prototype") // Her çağrıda yeni bir WebDriver örneği oluştur
    public WebDriver webDriver() {
        // WebDriverManager ile ChromeDriver'ı otomatik olarak indir ve yapılandır
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=" + randomWindowSize());
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=" + pickUserAgent());
        options.addArguments("--lang=tr-TR,tr");
        options.addArguments("--disable-features=TranslateUI");
        // Additional anti-detection flags
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=IsolateOrigins,site-per-process");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-notifications");
        String proxyToUse = pickProxy();
        if (proxyToUse != null) {
            options.addArguments("--proxy-server=" + proxyToUse);
        }
        options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("prefs", java.util.Map.of("intl.accept_languages", "tr-TR,tr",
                "profile.default_content_setting_values.notifications", 2));

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        if (useStealth) {
            applyStealth(driver);
        }

        log.info("WebDriver başlatıldı (headless: {}, WebDriverManager ile otomatik yönetiliyor)", headless);
        return driver;
    }

    private String pickUserAgent() {
        List<String> defaults = Arrays.asList(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:118.0) Gecko/20100101 Firefox/118.0");

        List<String> configured = userAgents == null || userAgents.isBlank()
                ? defaults
                : Arrays.stream(userAgents.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList();

        return configured.get(ThreadLocalRandom.current().nextInt(configured.size()));
    }

    private String randomWindowSize() {
        int width = ThreadLocalRandom.current().nextInt(1280, 1921);
        int height = ThreadLocalRandom.current().nextInt(720, 1081);
        return width + "," + height;
    }

    private String pickProxy() {
        if (proxy != null && !proxy.isBlank()) {
            return proxy.trim();
        }
        if (proxies == null || proxies.isBlank()) {
            return null;
        }
        var list = proxies != null ? java.util.Arrays.stream(proxies.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList() : java.util.List.<String>of();
        if (list.isEmpty())
            return null;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private void applyStealth(WebDriver driver) {
        try {
            if (driver instanceof JavascriptExecutor executor) {
                // Remove webdriver flag
                executor.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined});");

                // Mock Chrome runtime
                executor.executeScript(
                        "window.navigator.chrome = {runtime: {}, loadTimes: function() {}, csi: function() {}, app: {}};");

                // Realistic languages
                executor.executeScript(
                        "Object.defineProperty(navigator, 'languages', {get: () => ['tr-TR', 'tr', 'en-US', 'en']});");

                // Realistic plugins (PDF viewers)
                executor.executeScript(
                        "Object.defineProperty(navigator, 'plugins', {" +
                                "  get: () => [" +
                                "    {name: 'PDF Viewer', filename: 'internal-pdf-viewer', description: 'Portable Document Format'},"
                                +
                                "    {name: 'Chrome PDF Viewer', filename: 'internal-pdf-viewer', description: 'Portable Document Format'},"
                                +
                                "    {name: 'Chromium PDF Viewer', filename: 'internal-pdf-viewer', description: 'Portable Document Format'}"
                                +
                                "  ]" +
                                "});");

                // Hardware properties
                executor.executeScript("Object.defineProperty(navigator, 'hardwareConcurrency', {get: () => 8});");
                executor.executeScript("Object.defineProperty(navigator, 'deviceMemory', {get: () => 8});");
                executor.executeScript("Object.defineProperty(navigator, 'platform', {get: () => 'Win32'});");

                // Permissions API mock
                executor.executeScript(
                        "const originalQuery = window.navigator.permissions.query;" +
                                "window.navigator.permissions.query = (parameters) => (" +
                                "  parameters.name === 'notifications' ?" +
                                "  Promise.resolve({ state: Notification.permission }) :" +
                                "  originalQuery(parameters)" +
                                ");");

                // WebGL spoofing
                executor.executeScript(
                        "const getParameter = WebGLRenderingContext.prototype.getParameter;" +
                                "WebGLRenderingContext.prototype.getParameter = function(parameter) {" +
                                "  if (parameter === 37445) return 'Google Inc. (NVIDIA)';" +
                                "  if (parameter === 37446) return 'ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)';"
                                +
                                "  return getParameter.apply(this, [parameter]);" +
                                "};");

                log.info("Advanced stealth scripts applied successfully");
            }
        } catch (Exception e) {
            log.warn("Stealth script uygulanamadı: {}", e.getMessage());
        }
    }
}
