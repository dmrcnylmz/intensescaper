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
public class EmlakjetScraperImpl implements ScraperService {

    private final WebDriver webDriver;

    @Override
    public List<Ilan> scrape(String url, Kullanici kullanici) throws ScrapingException {
        List<Ilan> ilanlar = new ArrayList<>();
        WebDriver driver = null;

        try {
            driver = webDriver;
            log.info("Emlakjet scraping başlatılıyor: {}", url);
            
            driver.get(url);
            Thread.sleep(3000);
            
            // İlan listesi - Emlakjet için örnek selector'lar
            List<WebElement> ilanElements = driver.findElements(By.cssSelector("._3qUI9q"));
            log.info("Toplam {} ilan bulundu", ilanElements.size());
            
            for (int i = 0; i < Math.min(ilanElements.size(), 10); i++) { // İlk 10 ilan
                try {
                    WebElement ilanElement = ilanElements.get(i);
                    WebElement linkElement = ilanElement.findElement(By.tagName("a"));
                    String ilanUrl = linkElement.getAttribute("href");
                    
                    driver.get(ilanUrl);
                    Thread.sleep(2000);
                    
                    Ilan ilan = new Ilan();
                    ilan.setSite("emlakjet");
                    ilan.setIlanUrl(ilanUrl);
                    ilan.setKullanici(kullanici);
                    ilan.setCekilmeTarihi(LocalDateTime.now());
                    ilan.setMesajGonderildi(false);
                    
                    // Başlık
                    try {
                        WebElement baslikElement = driver.findElement(By.cssSelector("h1._1dELD"));
                        ilan.setBaslik(baslikElement.getText());
                    } catch (Exception e) {
                        ilan.setBaslik("Başlık Yok");
                    }
                    
                    // Fiyat
                    try {
                        WebElement fiyatElement = driver.findElement(By.cssSelector("._2TxNQv"));
                        String fiyatStr = fiyatElement.getText().replaceAll("[^0-9]", "");
                        ilan.setFiyat(Double.parseDouble(fiyatStr));
                    } catch (Exception e) {
                        ilan.setFiyat(0.0);
                    }
                    
                    // Konum
                    try {
                        WebElement konumElement = driver.findElement(By.cssSelector("._3VVkl"));
                        ilan.setKonum(konumElement.getText());
                    } catch (Exception e) {
                        ilan.setKonum("Belirtilmemiş");
                    }
                    
                    // Telefon numarası
                    try {
                        WebElement telefonButton = new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("button._1gNbJ")));
                        
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", telefonButton);
                        Thread.sleep(1500);
                        
                        WebElement telefonElement = new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("._2jBQM")));
                        
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
                    
                    // Listeyi yeniden bul (stale element hatası önleme)
                    ilanElements = driver.findElements(By.cssSelector("._3qUI9q"));
                    
                } catch (Exception e) {
                    log.error("İlan işlenirken hata: {}", e.getMessage());
                }
            }
            
            log.info("Emlakjet scraping tamamlandı. Toplam {} ilan çekildi", ilanlar.size());
            
        } catch (Exception e) {
            log.error("Scraping hatası: ", e);
            throw new ScrapingException("Emlakjet'ten veri çekilirken hata oluştu: " + e.getMessage(), e);
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
        return url != null && url.contains("emlakjet.com");
    }

    @Override
    public String getSiteName() {
        return "emlakjet";
    }

}

