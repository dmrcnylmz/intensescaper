package com.intensescaper.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "playwright")
public class PlaywrightProperties {

    /**
     * Playwright destekli scraping tamamen devre dışı bırakılmak istenirse.
     */
    private boolean enabled = true;

    private boolean headless = true;
    private long navigationTimeoutMs = 45000;
    private long waitAfterNavigationMinMs = 800;
    private long waitAfterNavigationMaxMs = 1500;
    private String locale = "tr-TR";

    /**
     * Rastgele seçeceğimiz user-agent listesi. Boş bırakılırsa varsayılan set kullanılır.
     */
    private List<String> userAgents = new ArrayList<>(List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Safari/605.1.15",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    ));

    private HumanBehavior human = new HumanBehavior();
    private Stealth stealth = new Stealth();
    private Session session = new Session();

    @Getter
    @Setter
    public static class HumanBehavior {
        private boolean mouseMovements = true;
        private boolean randomScroll = true;
        private int scrollSteps = 6;
        private long scrollDelayMs = 450;
    }

    @Getter
    @Setter
    public static class Stealth {
        private boolean spoofNavigator = true;
        private boolean spoofPlugins = true;
        private boolean spoofLanguages = true;
        private boolean spoofWebdriverFlag = true;
    }

    @Getter
    @Setter
    public static class Session {
        /**
         * Belirli bir ana sayfaya uğrayıp cookie almak için kullanılır.
         */
        private boolean bootstrapEnabled = true;
        private String bootstrapUrl = "https://www.sahibinden.com";
        private boolean persistCookies = true;
    }
}

