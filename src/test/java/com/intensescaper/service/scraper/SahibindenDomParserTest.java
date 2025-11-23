package com.intensescaper.service.scraper;

import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SahibindenDomParserTest {

    private SahibindenScraperImpl scraper;

    @BeforeEach
    void setUp() {
        scraper = new SahibindenScraperImpl(null, null);
    }

    @Test
    void mapDocumentToIlanParsesDetailFields() {
        String html = """
                <html>
                <body id="classifiedDetail">
                  <div class="classifiedDetailTitle"><h1>Süper Arsa</h1></div>
                  <div class="classified-price-container">1.250.000 TL</div>
                  <div class="classified-location">İstanbul / Bağcılar</div>
                  <div class="user-info-phones"><dd>0555 444 3322</dd></div>
                  <div id="classifiedDescription">Büyük ve köşe başı arsa.</div>
                </body>
                </html>
                """;
        Document document = Jsoup.parse(html, "https://www.sahibinden.com/ilan/123456/detay");

        Kullanici kullanici = new Kullanici();
        Ilan ilan = scraper.mapDocumentToIlan(document, "https://www.sahibinden.com/ilan/123456/detay", kullanici, null);

        assertEquals("Süper Arsa", ilan.getBaslik());
        assertEquals("İstanbul / Bağcılar", ilan.getKonum());
        assertEquals("5554443322".substring(0, 10), ilan.getTelefonNumarasi());
        assertEquals(1_250_000d, ilan.getFiyat());
    }

    @Test
    void collectListingSeedsParsesMultipleRows() {
        String html = """
                <html>
                <table id="searchResultsTable">
                  <tbody>
                    <tr data-id="111">
                      <td><a class="classifiedTitle" href="https://www.sahibinden.com/ilan/111/detay">İlan 1</a></td>
                      <td class="searchResultsLocationValue">İstanbul / Bağcılar</td>
                      <td class="searchResultsPriceValue">900.000 TL</td>
                      <td class="searchResultsDateValue">Bugün</td>
                    </tr>
                    <tr data-id="222">
                      <td><a class="classifiedTitle" href="https://www.sahibinden.com/ilan/222/detay">İlan 2</a></td>
                      <td class="searchResultsLocationValue">İstanbul / Göktürk</td>
                      <td class="searchResultsPriceValue">1.200.000 TL</td>
                      <td class="searchResultsDateValue">Dün</td>
                    </tr>
                  </tbody>
                </table>
                </html>
                """;
        Document document = Jsoup.parse(html, "https://www.sahibinden.com");

        List<SahibindenScraperImpl.ListingSeed> seeds = scraper.collectListingSeeds(document);

        assertEquals(2, seeds.size());
        assertEquals("İlan 1", seeds.get(0).baslik());
        assertEquals("İstanbul / Göktürk", seeds.get(1).konum());
    }

    @Test
    void detectPageTypeUnderstandsListPages() {
        Document document = Jsoup.parse("<table id='searchResultsTable'><tr data-id='1'></tr></table>", "https://www.sahibinden.com");
        SahibindenScraperImpl.PageType type = scraper.detectPageType(document, "https://www.sahibinden.com/satilik");
        assertEquals(SahibindenScraperImpl.PageType.LIST, type);
    }
}

