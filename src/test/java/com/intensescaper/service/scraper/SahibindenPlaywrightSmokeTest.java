package com.intensescaper.service.scraper;

import com.intensescaper.service.browser.PlaywrightStealthClient;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "playwright.enabled=true"
})
class SahibindenPlaywrightSmokeTest {

    @Autowired
    private PlaywrightStealthClient playwrightStealthClient;

    @Test
    void renderExampleDotComWhenFlagEnabled() {
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv("RUN_PLAYWRIGHT_SMOKE")),
                "RUN_PLAYWRIGHT_SMOKE=true olduğunda çalışır.");

        PlaywrightStealthClient.RenderedDocument rendered = playwrightStealthClient.render("https://www.example.com");
        assertTrue(rendered.document().title().contains("Example"),
                "Example.com Playwright render başarısız");
    }
}

