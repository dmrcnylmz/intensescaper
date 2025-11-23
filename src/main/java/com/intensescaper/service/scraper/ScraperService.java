package com.intensescaper.service.scraper;

import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.exception.ScrapingException;

import java.util.List;

public interface ScraperService {
    
    /**
     * Belirtilen URL'den veri çeker
     */
    List<Ilan> scrape(String url, Kullanici kullanici) throws ScrapingException;
    
    /**
     * Bu scraper'ın verilen URL'yi destekleyip desteklemediğini kontrol eder
     */
    boolean supports(String url);
    
    /**
     * Scraper'ın hangi site için olduğunu döndürür
     */
    String getSiteName();
    
}

