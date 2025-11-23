package com.intensescaper.service.browser;

import com.intensescaper.config.PlaywrightProperties;
import com.microsoft.playwright.Page;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class HumanBehaviorSimulator {

    public void simulate(Page page, PlaywrightProperties properties) {
        long wait = randomBetween(properties.getWaitAfterNavigationMinMs(), properties.getWaitAfterNavigationMaxMs());
        page.waitForTimeout(wait);

        PlaywrightProperties.HumanBehavior human = properties.getHuman();
        if (human.isMouseMovements()) {
            moveMouseRandomly(page);
        }
        if (human.isRandomScroll()) {
            scrollPage(page, human.getScrollSteps(), human.getScrollDelayMs());
        }
    }

    private void moveMouseRandomly(Page page) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 3; i++) {
            double x = random.nextDouble(0, 800);
            double y = random.nextDouble(0, 600);
            // page.mouse().move(x, y, new
            // Page.MouseMoveOptions().setSteps(random.nextInt(3, 7)));
            page.waitForTimeout(randomBetween(120, 320));
        }
    }

    private void scrollPage(Page page, int steps, long delayMs) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < steps; i++) {
            int deltaY = random.nextInt(200, 800);
            page.mouse().wheel(0, deltaY);
            page.waitForTimeout(delayMs + random.nextInt(50, 150));
        }
        page.mouse().wheel(0, -400);
    }

    private long randomBetween(long min, long max) {
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextLong(min, max);
    }
}
