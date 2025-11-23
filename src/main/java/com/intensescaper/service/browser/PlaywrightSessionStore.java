package com.intensescaper.service.browser;

import com.microsoft.playwright.BrowserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PlaywrightSessionStore {

    private static final java.nio.file.Path SESSION_PATH = java.nio.file.Paths.get("browser_session.json");

    public java.nio.file.Path getStorageStatePath() {
        if (java.nio.file.Files.exists(SESSION_PATH)) {
            return SESSION_PATH;
        }
        return null;
    }

    public void persist(BrowserContext context) {
        try {
            log.info("Attempting to persist session to: {}", SESSION_PATH.toAbsolutePath());
            context.storageState(new BrowserContext.StorageStateOptions().setPath(SESSION_PATH));
            log.info("Session persisted successfully.");
        } catch (Exception e) {
            log.error("Failed to persist session", e);
        }
    }

    // Deprecated/Unused methods kept for compatibility if needed, or removed.
    // Removing old cookie methods to force usage of new full-state persistence.
    public void restoreCookies(BrowserContext context, String url) {
        // No-op, handled by setStorageStatePath in creation
    }

    public void persistCookies(BrowserContext context, String url) {
        persist(context);
    }
}
