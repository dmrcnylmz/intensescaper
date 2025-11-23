package com.intensescaper.service.browser;

import com.intensescaper.config.PlaywrightProperties;
import com.intensescaper.proxy.ProxyRotator;
import com.intensescaper.proxy.ProxySpec;
import com.intensescaper.service.browser.PlaywrightStealthClient.RenderedDocument;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.Proxy;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaywrightStealthClient {

    private final PlaywrightProperties properties;
    private final ProxyRotator proxyRotator;
    private final PlaywrightSessionStore sessionStore;
    private final HumanBehaviorSimulator humanBehaviorSimulator;

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public RenderedDocument render(String url) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("Playwright devre dışı bırakıldı.");
        }

        try (Playwright playwright = Playwright.create()) {
            Browser browser = launchBrowser(playwright);
            try {
                BrowserContext context = createContext(browser, url);
                try {
                    Page page = context.newPage();
                    applyStealthScripts(page);
                    bootstrapSessionIfNeeded(page);
                    navigate(page, url);
                    handleCloudflareChallenge(page);
                    humanBehaviorSimulator.simulate(page, properties);
                    String content = page.content();

                    // DEBUG: Save content to file
                    try {
                        String title = page.title();
                        log.info("Playwright Render Title: {}", title);
                        java.nio.file.Path debugPath = java.nio.file.Paths.get(
                                "/Users/pc/.gemini/antigravity/brain/4d75cb9b-a992-4721-b351-2e9f0f82f6c2/debug_page.html");
                        java.nio.file.Files.writeString(debugPath, content);
                        log.info("Saved debug HTML to: {}", debugPath);
                    } catch (Exception e) {
                        log.error("Failed to save debug HTML", e);
                    }

                    return new RenderedDocument(Jsoup.parse(content, page.url()));
                } finally {
                    if (context != null && properties.getSession().isPersistCookies()) {
                        sessionStore.persist(context);
                    }
                    if (context != null) {
                        context.close();
                    }
                }
            } finally {
                browser.close();
            }
        } catch (PlaywrightException e) {
            throw new IllegalStateException("Playwright ile sayfa render edilirken hata oluştu: " + url, e);
        }
    }

    private Browser launchBrowser(Playwright playwright) {
        BrowserType browserType = playwright.chromium();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(properties.isHeadless())
                .setChannel("chromium");

        proxyRotator.acquire().ifPresent(proxySpec -> launchOptions.setProxy(toPlaywrightProxy(proxySpec)));

        return browserType.launch(launchOptions);
    }

    private Proxy toPlaywrightProxy(ProxySpec spec) {
        String server = (spec.protocol() != null ? spec.protocol() + "://" : "") + spec.host() + ":" + spec.port();
        Proxy proxy = new Proxy(server);
        if (spec.hasAuth()) {
            proxy.setUsername(spec.username());
            proxy.setPassword(spec.password());
        }
        return proxy;
    }

    private BrowserContext createContext(Browser browser, String url) {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setLocale(properties.getLocale())
                .setViewportSize(randomBetween(1280, 1920), randomBetween(720, 1080))
                .setUserAgent(pickUserAgent());

        if (properties.getSession().isPersistCookies()) {
            java.nio.file.Path storagePath = sessionStore.getStorageStatePath();
            if (storagePath != null) {
                log.info("Loading session from: {}", storagePath);
                options.setStorageStatePath(storagePath);
            }
        }

        BrowserContext context = browser.newContext(options);
        // restoreCookies is now handled by storageStatePath
        return context;
    }

    private void bootstrapSessionIfNeeded(Page page) {
        if (!properties.getSession().isBootstrapEnabled()) {
            return;
        }
        String bootstrapUrl = properties.getSession().getBootstrapUrl();
        if (bootstrapUrl == null || bootstrapUrl.isBlank()) {
            return;
        }
        try {
            navigate(page, bootstrapUrl);
            humanBehaviorSimulator.simulate(page, properties);
        } catch (Exception ex) {
            log.warn("Bootstrap sırasında hata: {}", ex.getMessage());
        }
    }

    private void navigate(Page page, String url) {
        Page.NavigateOptions options = new Page.NavigateOptions()
                .setTimeout((double) properties.getNavigationTimeoutMs());
        page.navigate(url, options);
    }

    private void applyStealthScripts(Page page) {
        if (!properties.getStealth().isSpoofNavigator() &&
                !properties.getStealth().isSpoofPlugins() &&
                !properties.getStealth().isSpoofWebdriverFlag()) {
            return;
        }

        StringBuilder script = new StringBuilder();

        // 1. Remove navigator.webdriver
        if (properties.getStealth().isSpoofWebdriverFlag()) {
            script.append("Object.defineProperty(navigator, 'webdriver', {get: () => undefined});");
        }

        // 2. Spoof Navigator Properties (Hardware, Language, Platform)
        if (properties.getStealth().isSpoofNavigator()) {
            // Languages
            script.append(
                    "Object.defineProperty(navigator, 'languages', {get: () => ['tr-TR', 'tr', 'en-US', 'en']});");

            // Platform (Win32 is standard for many scrapers, but matching the OS is better.
            // For now, we stick to Win32 as it's the most common desktop UA)
            script.append("Object.defineProperty(navigator, 'platform', {get: () => 'Win32'});");

            // Hardware Concurrency (4, 8, 12, 16 are common)
            script.append("Object.defineProperty(navigator, 'hardwareConcurrency', {get: () => 8});");

            // Device Memory (4, 8, 16, 32)
            script.append("Object.defineProperty(navigator, 'deviceMemory', {get: () => 8});");
        }

        // 3. Spoof Plugins (More realistic mock)
        if (properties.getStealth().isSpoofPlugins()) {
            script.append(
                    """
                                // Mock Plugins
                                const mockPlugins = [
                                    { name: 'PDF Viewer', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
                                    { name: 'Chrome PDF Viewer', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
                                    { name: 'Chromium PDF Viewer', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
                                    { name: 'Microsoft Edge PDF Viewer', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
                                    { name: 'WebKit built-in PDF', filename: 'internal-pdf-viewer', description: 'Portable Document Format' }
                                ];

                                const pluginArray = [];
                                mockPlugins.forEach(p => {
                                    const plugin = {
                                        name: p.name,
                                        filename: p.filename,
                                        description: p.description,
                                        length: 1,
                                        item: () => null,
                                        namedItem: () => null
                                    };
                                    pluginArray.push(plugin);
                                });

                                // Add iterator and other array-like properties
                                pluginArray.item = (index) => pluginArray[index];
                                pluginArray.namedItem = (name) => pluginArray.find(p => p.name === name);
                                pluginArray.refresh = () => {};

                                Object.defineProperty(navigator, 'plugins', {
                                    get: () => pluginArray
                                });

                                // Mock MimeTypes
                                const mimeTypeArray = [];
                                mimeTypeArray.item = (index) => mimeTypeArray[index];
                                mimeTypeArray.namedItem = (name) => mimeTypeArray.find(m => m.type === name);

                                Object.defineProperty(navigator, 'mimeTypes', {
                                    get: () => mimeTypeArray
                                });
                            """);

            // Remove window.chrome property if it reveals automation,
            // OR ensure it looks like a normal Chrome instance.
            // Automation often has `window.chrome` but missing `runtime`.
            // We'll add a basic runtime object.
            script.append("""
                        if (!window.chrome) {
                            window.chrome = {};
                        }
                        if (!window.chrome.runtime) {
                            window.chrome.runtime = {};
                        }
                    """);
        }

        // 4. WebGL Vendor/Renderer Spoofing (Optional but good for fingerprinting)
        // This is a bit more advanced, adding a basic override.
        script.append("""
                    const getParameter = WebGLRenderingContext.prototype.getParameter;
                    WebGLRenderingContext.prototype.getParameter = function(parameter) {
                        // UNMASKED_VENDOR_WEBGL
                        if (parameter === 37445) {
                            return 'Google Inc. (NVIDIA)';
                        }
                        // UNMASKED_RENDERER_WEBGL
                        if (parameter === 37446) {
                            return 'ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)';
                        }
                        return getParameter.apply(this, [parameter]);
                    };
                """);

        // 5. Permissions API (Pass 'notifications' check which is common)
        script.append("""
                    const originalQuery = window.navigator.permissions.query;
                    window.navigator.permissions.query = (parameters) => (
                        parameters.name === 'notifications' ?
                        Promise.resolve({ state: Notification.permission }) :
                        originalQuery(parameters)
                    );
                """);

        page.addInitScript(script.toString());
    }

    private String pickUserAgent() {
        List<String> agents = properties.getUserAgents();
        if (CollectionUtils.isEmpty(agents)) {
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        }
        int index = ThreadLocalRandom.current().nextInt(agents.size());
        return agents.get(index);
    }

    private int randomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public void primeSession() {
        log.info("🚀 SESSION PRIMING MODE STARTED");
        log.info("The browser will open. Please manually solve any Cloudflare challenge.");
        log.info("You have 120 seconds.");

        // Force headed mode for priming
        boolean originalHeadless = properties.isHeadless();
        properties.setHeadless(false);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = launchBrowser(playwright);
            BrowserContext context = createContext(browser, properties.getSession().getBootstrapUrl());
            Page page = context.newPage();

            applyStealthScripts(page);

            log.info("Navigating to bootstrap URL: {}", properties.getSession().getBootstrapUrl());
            page.navigate(properties.getSession().getBootstrapUrl());

            log.info("Waiting for user interaction (120s)...");
            page.waitForTimeout(120000);

            // Persist session
            if (properties.getSession().isPersistCookies()) {
                sessionStore.persist(context);
                log.info("✅ Session primed and saved successfully!");
            }

        } catch (Exception e) {
            log.error("Priming failed", e);
        } finally {
            properties.setHeadless(originalHeadless);
        }
    }

    private void handleCloudflareChallenge(Page page) {
        try {
            String title = page.title();
            if (title.contains("Bir dakika lütfen") || title.contains("Just a moment")
                    || title.contains("Attention Required")) {
                log.warn("🛡️ Cloudflare challenge detected! Attempting to solve...");

                // Wait a bit for the widget to load
                page.waitForTimeout(3000);

                // Try to find the Turnstile iframe
                try {
                    // Look for the iframe
                    page.frames().stream()
                            .filter(f -> f.url().contains("cloudflare") || f.url().contains("turnstile"))
                            .findFirst()
                            .ifPresent(frame -> {
                                log.info("Found Turnstile frame, attempting click...");
                                try {
                                    // Click the checkbox or the box
                                    frame.click("input[type='checkbox'], .ctp-checkbox-label, #challenge-stage");
                                    log.info("Clicked Turnstile widget");
                                } catch (Exception e) {
                                    log.warn("Failed to click in frame: " + e.getMessage());
                                }
                            });

                    // Also try clicking shadow DOM or main page elements if iframe approach fails
                    if (page.locator("#challenge-stage").isVisible()) {
                        page.click("#challenge-stage");
                    }

                } catch (Exception e) {
                    log.warn("Error interacting with challenge: {}", e.getMessage());
                }

                // Wait for navigation or title change
                try {
                    page.waitForFunction("document.title !== '" + title + "'", null,
                            new Page.WaitForFunctionOptions().setTimeout(10000));
                    log.info("Challenge passed (probably) - Title changed to: {}", page.title());
                } catch (Exception e) {
                    log.warn("Timeout waiting for challenge solution. Current title: {}", page.title());
                }
            }
        } catch (Exception e) {
            log.warn("Error in challenge handler: {}", e.getMessage());
        }
    }

    public record RenderedDocument(Document document) {
    }
}
