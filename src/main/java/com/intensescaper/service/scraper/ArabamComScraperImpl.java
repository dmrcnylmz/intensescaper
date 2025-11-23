package com.intensescaper.service.scraper;

import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.exception.ScrapingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArabamComScraperImpl implements ScraperService {

    private final WebDriver webDriver;

    @Override
    public List<Ilan> scrape(String url, Kullanici kullanici) throws ScrapingException {
        List<Ilan> ilanlar = new ArrayList<>();
        WebDriver driver = null;

        try {
            driver = webDriver;
            log.info("Arabam.com scraping başlatılıyor: {}", url);
            
            driver.get(url);
            Thread.sleep(3000);
            
            // İlan listesi - Arabam.com için örnek selector'lar
            List<WebElement> ilanElements = driver.findElements(By.cssSelector("tr.listing-row"));
            log.info("Toplam {} ilan bulundu", ilanElements.size());
            
            for (int i = 0; i < Math.min(ilanElements.size(), 10); i++) {
                try {
                    WebElement ilanElement = ilanElements.get(i);
                    WebElement linkElement = ilanElement.findElement(By.cssSelector("a.listing-link"));
                    String ilanUrl = linkElement.getAttribute("href");
                    
                    if (!ilanUrl.startsWith("http")) {
                        ilanUrl = "https://www.arabam.com" + ilanUrl;
                    }
                    
                    driver.get(ilanUrl);
                    Thread.sleep(2000);
                    
                    Ilan ilan = new Ilan();
                    ilan.setSite("arabam");
                    ilan.setIlanUrl(ilanUrl);
                    ilan.setKullanici(kullanici);
                    ilan.setCekilmeTarihi(LocalDateTime.now());
                    ilan.setMesajGonderildi(false);
                    
                    // Başlık
                    try {
                        WebElement baslikElement = driver.findElement(By.cssSelector("h1.detail-title"));
                        ilan.setBaslik(baslikElement.getText());
                    } catch (Exception e) {
                        ilan.setBaslik("Başlık Yok");
                    }
                    
                    // Fiyat
                    try {
                        WebElement fiyatElement = driver.findElement(By.cssSelector(".detail-price"));
                        String fiyatStr = fiyatElement.getText().replaceAll("[^0-9]", "");
                        ilan.setFiyat(Double.parseDouble(fiyatStr));
                    } catch (Exception e) {
                        ilan.setFiyat(0.0);
                    }
                    
                    // Konum
                    try {
                        WebElement konumElement = driver.findElement(By.cssSelector(".detail-location"));
                        ilan.setKonum(konumElement.getText());
                    } catch (Exception e) {
                        ilan.setKonum("Belirtilmemiş");
                    }
                    
                    // Telefon numarası
                    try {
                        WebElement telefonButton = new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-phone")));
                        
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", telefonButton);
                        Thread.sleep(1500);
                        
                        WebElement telefonElement = new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".phone-number")));
                        
                        String telefon = telefonElement.getText().replaceAll("[^0-9]", "");
                        ilan.setTelefonNumarasi(telefon);
                    } catch (Exception e) {
                        log.error("Telefon numarası alınamadı: {}", ilanUrl);
                        ilan.setTelefonNumarasi("Alınamadı");
                    }
                    
                    ilanlar.add(ilan);
                    log.info("İlan eklendi: {} - {}", ilan.getBaslik(), ilan.getTelefonNumarasi());
                    
                    driver.navigate().back();
                    Thread.sleep(2000);
                    
                    ilanElements = driver.findElements(By.cssSelector("tr.listing-row"));
                    
                } catch (Exception e) {
                    log.error("İlan işlenirken hata: {}", e.getMessage());
                }
            }
            
            log.info("Arabam.com scraping tamamlandı. Toplam {} ilan çekildi", ilanlar.size());
            
        } catch (Exception e) {
            log.error("Scraping hatası: ", e);
            throw new ScrapingException("Arabam.com'dan veri çekilirken hata oluştu: " + e.getMessage(), e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.error("WebDriver kapatılırken hata: {}", e.getMessage());
                }
            }
        }
        
        return ilanlar;
    }

    @Override
    public boolean supports(String url) {
        return url != null && url.contains("arabam.com");
    }

    @Override
    public String getSiteName() {
        return "arabam";
    }

}

