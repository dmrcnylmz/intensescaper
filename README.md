# 🚀 IntenseScraper Backend

[![CI/CD Pipeline](https://github.com/dmrcnylmz/intensescaper/actions/workflows/ci.yml/badge.svg)](https://github.com/dmrcnylmz/intensescaper/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Modern web sitelerinden (Sahibinden, Emlakjet, Arabam.com) veri çekme ve toplu WhatsApp mesajlaşma servisi.

## 📋 İçindekiler

- [Özellikler](#özellikler)
- [Teknolojiler](#teknolojiler)
- [Kurulum](#kurulum)
- [API Dokümantasyonu](#api-dokümantasyonu)
- [Docker ile Çalıştırma](#docker-ile-çalıştırma)
- [Kullanım](#kullanım)

## ✨ Özellikler

- 🔍 **Web Scraping**: Selenium ile dinamik web sitelerinden veri çekme
- 📱 **WhatsApp Entegrasyonu**: Toplu mesajlaşma servisi (simüle/gerçek API)
- 🔐 **JWT Kimlik Doğrulama**: Güvenli kullanıcı yönetimi
- ⚡ **Asenkron İşlemler**: Arka planda çalışan scraping işlemleri
- 📊 **İlerleme Takibi**: Real-time işlem durumu izleme
- 🎯 **Mesaj Şablonları**: Özelleştirilebilir mesaj içerikleri
- 📚 **Swagger UI**: İnteraktif API dokümantasyonu
- 🐳 **Docker Support**: Containerized deployment
- 🧪 **Unit Tests**: Kapsamlı test coverage

## 🛠 Teknolojiler

- **Java 17**
- **Spring Boot 3.2.0**
  - Spring Web (REST API)
  - Spring Data JPA (Veritabanı)
  - Spring Security (JWT)
  - Spring Validation
- **MySQL** (Production) / **H2** (Development)
- **Selenium 4.16.1** + **WebDriverManager** (Web Scraping)
- **JWT 0.12.3** (Token bazlı kimlik doğrulama)
- **SpringDoc OpenAPI 2.3.0** (Swagger)
- **Lombok** (Boilerplate azaltma)
- **Maven** (Bağımlılık yönetimi)
- **Docker** & **Docker Compose**

## 📦 Kurulum

### Gereksinimler

- JDK 17 veya üzeri
- Maven 3.6+
- MySQL 8.0+ (Production) veya H2 (Development)
- Docker & Docker Compose (opsiyonel)
- Playwright için Chromium bağımlılıkları (Playwright Java otomatik indirir)

### Playwright Stealth Ayarları

ScraperAPI tamamen kaldırıldı. Anti-bot engellerini aşmak için Playwright + insan benzeri davranış simülasyonu kullanıyoruz. Varsayılan `application.yml` içinde gelen yapılandırma:

```yaml
playwright:
  enabled: true            # Devre dışı bırakmak için false yapın
  headless: true           # Debug için false
  navigation-timeout-ms: 45000
  wait-after-navigation-min-ms: 800
  wait-after-navigation-max-ms: 1500
  human:
    scroll-steps: 5
    scroll-delay-ms: 400
  session:
    bootstrap-enabled: true
    bootstrap-url: https://www.sahibinden.com
    persist-cookies: true
```

Proxy/LB entegrasyonu için `ProxyRotator` arayüzü hazırdır. Varsayılan `NoOpProxyRotator` hiçbir proxy döndürmez; kendi rotasyon servisinizi Spring bean olarak tanımlamanız yeterli.

### Smoke Testleri

Gerçek siteye istek atan smoke testler varsayılan olarak **skip** edilir. Manuel gözlem için:

```bash
# Selenium akışı
RUN_SELENIUM_SMOKE=true mvn -Dtest=SahibindenSeleniumSmokeTest test

# Playwright akışı
RUN_PLAYWRIGHT_SMOKE=true mvn -Dtest=SahibindenPlaywrightSmokeTest test
```

### 1. Projeyi Klonlayın

```bash
git clone git@github.com:dmrcnylmz/intensescaper.git
cd intensescaper
```

### 2. Development Mode (H2 Database)

```bash
# Projeyi derle
mvn clean package

# Development profili ile çalıştır
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Veya JAR ile:
java -jar target/veribot-backend-1.0.0.jar --spring.profiles.active=dev
```

### 3. Production Mode (MySQL)

#### MySQL ile Manuel Kurulum

```bash
# MySQL'i başlat
mysql.server start

# Veritabanı oluştur
mysql -u root -p
CREATE DATABASE intensescaper;
exit

# Production profili ile çalıştır
java -jar target/intensescaper-backend-1.0.0.jar --spring.profiles.active=prod
```

## 🐳 Docker ile Çalıştırma

### Docker Compose (Önerilen)

```bash
# Tüm servisleri başlat (MySQL + App)
docker-compose up -d

# Logları takip et
docker-compose logs -f app

# Servisleri durdur
docker-compose down

# Verileri de sil
docker-compose down -v
```

### Manuel Docker Build

```bash
# Docker image oluştur
docker build -t intensescaper-backend .

# Container'ı çalıştır
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/intensescaper \
  intensescaper-backend
```

## 📚 API Dokümantasyonu

### Swagger UI

Proje çalıştıktan sonra tarayıcınızda:

```
http://localhost:8080/api/v1/swagger-ui.html
```

### OpenAPI JSON

```
http://localhost:8080/api/v1/v3/api-docs
```

### H2 Console (Development)

```
http://localhost:8080/api/v1/h2-console

URL: jdbc:h2:mem:intensescaper
Username: sa
Password: (boş)
```

### Authentication Endpoints

#### Kullanıcı Kaydı
```http
POST /api/v1/auth/kayit
Content-Type: application/json

{
  "kullaniciAdi": "testuser",
  "parola": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "kullaniciAdi": "testuser",
  "rol": "ROLE_USER",
  "mesaj": "Kayıt başarılı"
}
```

#### Giriş Yap
```http
POST /api/v1/auth/giris
Content-Type: application/json

{
  "kullaniciAdi": "testuser",
  "parola": "password123"
}
```

### Scraping Endpoints

#### Scraping Başlat
```http
POST /api/v1/scraping/baslat
Authorization: Bearer {token}
Content-Type: application/json

{
  "url": "https://www.sahibinden.com/kategori?query=..."
}
```

#### İşlem Durumu
```http
GET /api/v1/scraping/durum/{islemId}
Authorization: Bearer {token}
```

### Diğer Endpoint'ler

Tüm endpoint'ler için detaylı dokümantasyon Swagger UI'de mevcuttur.

## 🎯 Kullanım

### 1. Kullanıcı Kaydı ve Giriş

```bash
# Kayıt ol
curl -X POST http://localhost:8080/api/v1/auth/kayit \
  -H "Content-Type: application/json" \
  -d '{"kullaniciAdi":"test","parola":"test123"}'

# Token'ı kaydedin
TOKEN="<response_token>"
```

### 2. Mesaj Şablonu Oluştur

```bash
curl -X POST http://localhost:8080/api/v1/sablonlar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "baslik":"İlan Mesajı",
    "icerik":"Merhaba {{telefonNumarasi}}, {{baslik}} ilanınız hakkında bilgi alabilir miyim?"
  }'
```

### 3. Scraping Başlat

```bash
curl -X POST http://localhost:8080/api/v1/scraping/baslat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.sahibinden.com/..."}'
```

## 📝 Mesaj Şablonu Placeholder'ları

- `{{telefonNumarasi}}` - İlanın telefon numarası
- `{{baslik}}` - İlan başlığı
- `{{fiyat}}` - İlan fiyatı
- `{{konum}}` - İlan konumu
- `{{site}}` - İlan sitesi
- `{{ilanUrl}}` - İlan URL'i
- `{{kullaniciAdi}}` - Kullanıcı adınız

## 🧪 Testler

```bash
# Tüm testleri çalıştır
mvn test

# Belirli bir test sınıfını çalıştır
mvn test -Dtest=AuthServiceTest

# Test coverage raporu
mvn clean test jacoco:report
```

## 🔒 Güvenlik

- JWT token tabanlı kimlik doğrulama
- BCrypt ile şifrelenmiş parolalar
- CORS yapılandırması
- Kullanıcı bazlı yetkilendirme
- OWASP Dependency Check (CI/CD)

## 📊 CI/CD

GitHub Actions ile otomatik:
- ✅ Build ve Test
- ✅ Docker Image Build
- ✅ Code Quality Analysis
- ✅ Security Dependency Check (haftalık)

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'feat: Add amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 👨‍💻 Geliştirici

**Demir Can Yılmaz**
- GitHub: [@dmrcnylmz](https://github.com/dmrcnylmz)

## ⚠️ Uyarı

Bu araç eğitim amaçlıdır. Web scraping yaparken:
- Hedef sitenin kullanım şartlarını okuyun
- robots.txt dosyasına uyun
- Rate limiting uygulayın
- Sorumlu bir şekilde kullanın

## 📞 İletişim

Sorularınız için issue açabilir veya email gönderebilirsiniz.

---

⭐ Projeyi beğendiyseniz yıldız vermeyi unutmayın!
