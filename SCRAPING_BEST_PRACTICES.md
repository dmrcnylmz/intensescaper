# 🔍 Web Scraping Best Practices - IntenseScraper

## ⚠️ Dikkat Edilmesi Gerekenler

### 1. Yasal & Etik Konular
- ✅ robots.txt dosyasına uyun
- ✅ Terms of Service'i okuyun
- ✅ Kişisel verileri KVKK'ya uygun işleyin
- ✅ Rate limiting uygulayın
- ⚠️ Telif hakkı olan içeriklere dikkat

### 2. Bot Detection'dan Kaçınma

#### Mevcut Korumalarımız:
```java
// SeleniumConfig.java
options.addArguments("--disable-blink-features=AutomationControlled");
options.addArguments("--user-agent=Mozilla/5.0...");
options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
```

#### Ek Öneriler:
- [ ] **Stealth Plugin** kullanın (puppeteer-extra-plugin-stealth benzeri)
- [ ] **Random delays** ekleyin (her request arasında 2-5 saniye)
- [ ] **Proxy rotation** kullanın
- [ ] **Cookie management** - Session'ları simüle edin
- [ ] **Browser fingerprinting** - Canvas, WebGL vb.

### 3. Performans & Ölçeklenebilirlik

#### ✅ Zaten Yapılanlar:
- Asenkron scraping (`@Async`)
- Batch processing (max 10 ilan)
- WebDriver pooling (prototype scope)

#### 🚀 İyileştirme Önerileri:
```java
// Distributed scraping için Redis queue
// Multiple browser instances (ThreadPoolExecutor)
// Caching layer (çekilen ilanları cache'le)
// Database indexing (ilan URL'leri için unique index)
```

### 4. Error Handling & Resilience

#### Mevcut:
```java
try {
    // scraping logic
} catch (Exception e) {
    log.error("Scraping hatası: ", e);
    throw new ScrapingException(...);
}
```

#### Öneriler:
- [ ] **Retry mechanism** (3 deneme, exponential backoff)
- [ ] **Circuit breaker pattern**
- [ ] **Dead letter queue** (başarısız işlemler için)
- [ ] **Health checks** (site erişilebilirliği)

### 5. Selector Strategy

#### ✅ İyi Pratikler:
```java
// Fallback selectors
List<WebElement> ilanElements = driver.findElements(By.cssSelector("primary-selector"));
if (ilanElements.isEmpty()) {
    ilanElements = driver.findElements(By.cssSelector("fallback-selector"));
}
```

#### Öneriler:
- [ ] XPath yerine CSS selectors (daha hızlı)
- [ ] data-testid gibi stable attributes kullanın
- [ ] Relative locators (Selenium 4+)
- [ ] Selector versioning (site değişirse eski sürümü dene)

### 6. Data Quality & Validation

```java
// Örnek validasyon
if (telefon.matches("^5\\d{9}$")) {
    ilan.setTelefonNumarasi(telefon);
} else {
    log.warn("Geçersiz telefon formatı: {}", telefon);
    throw new ValidationException();
}

// Fiyat validasyonu
if (fiyat > 0 && fiyat < 1_000_000_000) {
    ilan.setFiyat(fiyat);
}
```

### 7. Monitoring & Alerting

```java
// Metrics ekleyin
@Timed("scraping.duration")
@Counted("scraping.attempts")
public List<Ilan> scrape(String url, Kullanici kullanici) {
    // ...
}

// Alert conditions:
// - Success rate < 80%
// - Average duration > 5 minutes
// - Captcha detected
// - IP banned
```

### 8. Resource Management

#### ✅ Zaten Yapılan:
```java
finally {
    if (driver != null) {
        driver.quit();
    }
}
```

#### Eklenebilir:
- [ ] Connection pooling
- [ ] Memory monitoring (heap dumps)
- [ ] File cleanup (screenshots, logs)
- [ ] Database connection cleanup

### 9. Anti-Detection Stratejileri

#### Browser Fingerprinting Önlemleri:
```java
// Canvas fingerprinting
js.executeScript("Object.defineProperty(HTMLCanvasElement.prototype, 'toDataURL', {...});");

// WebGL fingerprinting
js.executeScript("const getParameter = WebGLRenderingContext.prototype.getParameter;...");

// Navigator properties
js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => false});");
```

#### Request Pattern Randomization:
```java
// Random scroll
js.executeScript("window.scrollBy(0, " + (100 + random.nextInt(400)) + ")");
Thread.sleep(500 + random.nextInt(1500));

// Random mouse movements (Action chains)
Actions actions = new Actions(driver);
actions.moveByOffset(random.nextInt(100), random.nextInt(100)).perform();
```

### 10. Site-Specific Stratejiler

#### Sahibinden.com:
- ✅ Telefon butonu JavaScript ile tıklanıyor
- ⚠️ Captcha riski yüksek (çok istek atarsanız)
- ⚠️ Login gerekebilir (bazı ilanlar için)
- ⚠️ Rate limit: ~100-200 istek/saat (tahmini)

#### Emlakjet.com:
- API kullanımı mümkün (daha güvenilir)
- JSON response parse et
- Authentication gerekebilir

#### Arabam.com:
- Infinite scroll (JavaScript scroll gerekli)
- Lazy loading images
- Progressive enhancement

## 🛠️ Önerilen Kütüphaneler

### Java Selenium Alternatives:
```xml
<!-- Playwright (Microsoft) - Daha modern -->
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.40.0</version>
</dependency>

<!-- HtmlUnit (Lightweight, headless) -->
<dependency>
    <groupId>net.sourceforge.htmlunit</groupId>
    <artifactId>htmlunit</artifactId>
    <version>2.70.0</version>
</dependency>

<!-- Jsoup (HTML parsing, no JS) -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.1</version>
</dependency>
```

### Proxy & Anti-Detection:
```java
// Rotating proxies
// BrightData, Smartproxy, Oxylabs
// veya Tor network

// Selenium Stealth (Python equivalent için)
// Java'da manual implementation gerekli
```

## 📊 Performance Benchmarks

| Metrik | Hedef | Mevcut |
|--------|-------|--------|
| İlan/dakika | 10-20 | ~2-3 |
| Success Rate | >95% | ~70% |
| Memory Usage | <500MB | ~800MB |
| CPU Usage | <50% | ~60% |

## 🔧 Troubleshooting

### Yaygın Sorunlar:

1. **"Element not found"**
   - Selector değişmiş olabilir
   - Sayfa yüklenmemiş olabilir
   - `WebDriverWait` kullanın

2. **"Stale element reference"**
   - DOM güncellenmiş
   - Element'i yeniden bulun

3. **"Timeout exception"**
   - Sayfa yavaş
   - Timeout'u artırın
   - Daha spesifik wait condition kullanın

4. **"Captcha detected"**
   - Rate limiting ekleyin
   - Proxy değiştirin
   - Captcha solving service kullanın (2captcha, anti-captcha)

5. **"Session expired / 401"**
   - Cookie management ekleyin
   - Login flow implement edin
   - Token refresh mechanism

## 📈 Monitoring Dashboard Önerileri

```java
// Grafana + Prometheus
// Metrics:
// - scraping_requests_total
// - scraping_duration_seconds
// - scraping_errors_total
// - scraping_success_rate
// - active_scraping_sessions
// - webdriver_instances_count
```

## 🎯 Sonraki Adımlar

1. **Kısa Vadeli (1-2 hafta)**
   - [ ] Retry mechanism ekle
   - [ ] Better error messages
   - [ ] Selector versioning
   - [ ] Basic metrics

2. **Orta Vadeli (1-2 ay)**
   - [ ] Proxy rotation
   - [ ] Captcha handling
   - [ ] Multi-site support (Arabam, Emlakjet)
   - [ ] Admin dashboard

3. **Uzun Vadeli (3-6 ay)**
   - [ ] Distributed scraping (Kubernetes)
   - [ ] Machine learning (anomaly detection)
   - [ ] Real-time scraping
   - [ ] API first approach

---

## 📝 Notlar

- Her site için ayrı strateji gerekir
- Yasal uyumluluk her zaman öncelik
- Performance vs. Detection trade-off
- Monitoring olmadan scraping yapmayın

