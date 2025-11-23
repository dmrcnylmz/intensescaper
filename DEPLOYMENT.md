# 🚀 IntenseScraper Deployment Guide

## 📋 Ön Gereksinimler

- Docker & Docker Compose
- MySQL 8.0+ (veya Docker ile)
- Java 17
- Node.js 18+

## 🔧 Production Deployment

### 1. Environment Variables

```bash
# Backend (.env veya docker-compose.yml)
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/intensescaper
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
JWT_SECRET=your_super_secret_jwt_key_min_32_characters
WHATSAPP_API_ENABLED=false
SELENIUM_HEADLESS=true

# Frontend (.env.production)
VITE_API_URL=https://your-domain.com/api/v1
```

### 2. Docker Compose ile Deployment

```bash
# MySQL + Backend + Frontend
docker-compose up -d

# Logları kontrol et
docker-compose logs -f app

# Durdur
docker-compose down
```

### 3. Manuel Deployment

#### Backend:
```bash
cd /path/to/intensescaper
mvn clean package -DskipTests
java -jar target/intensescaper-backend-1.0.0.jar --spring.profiles.active=prod
```

#### Frontend:
```bash
cd frontend
npm install
npm run build
# dist/ klasörünü nginx/apache ile serve et
```

### 4. Nginx Reverse Proxy (Önerilen)

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # Frontend
    location / {
        root /var/www/intensescaper/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 💰 Monetization - Abonelik Sistemi

### Stripe Entegrasyonu (Önerilen)

```java
// Backend dependency
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>24.0.0</version>
</dependency>

// Abonelik planları
BASIC: 99 TL/ay - 100 mesaj
PRO: 299 TL/ay - 500 mesaj
ENTERPRISE: 999 TL/ay - Sınırsız
```

### Payment Gateway Seçenekleri

1. **Stripe** (Uluslararası) ✅
2. **PayTR** (Türkiye için)
3. **iyzico** (Türkiye için)

## 🎯 Hızlı Başlangıç

```bash
# 1. Projeyi klonla
git clone https://github.com/your-repo/intensescaper.git
cd intensescaper

# 2. MySQL başlat
docker-compose up -d mysql

# 3. Backend başlat
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# 4. Frontend başlat
cd frontend && npm install && npm run dev

# 5. Tarayıcıda aç
http://localhost:3000
```

## 📊 Monitoring & Logs

```bash
# Backend logs
tail -f logs/application.log

# Docker logs
docker-compose logs -f

# Database backup
docker exec mysql mysqldump -u root -p intensescaper > backup.sql
```

## 🔐 Security Checklist

- [ ] JWT secret güçlü ve benzersiz
- [ ] HTTPS zorunlu (Let's Encrypt)
- [ ] CORS doğru yapılandırılmış
- [ ] Rate limiting aktif
- [ ] SQL injection koruması
- [ ] XSS koruması
- [ ] Environment variables güvenli

## 🚀 Production Optimizations

1. **Backend:**
   - Connection pooling ayarla
   - Cache mekanizması ekle (Redis)
   - Async işlemler optimize et

2. **Frontend:**
   - Bundle size optimize et
   - CDN kullan
   - Lazy loading

3. **Database:**
   - Index'leri optimize et
   - Query cache aktif et
   - Backup stratejisi

## 💡 Pricing Strategy

### Aylık Abonelik
- **Başlangıç:** 99 TL - 100 mesaj/ay
- **Profesyonel:** 299 TL - 500 mesaj/ay
- **İşletme:** 999 TL - Sınırsız

### Özellikler
- Otomatik telefon çekme
- Toplu WhatsApp mesajlaşma
- Mesaj şablonları
- İstatistikler ve raporlar
- API erişimi (Enterprise)

## 📞 Destek

- Email: support@intensescraper.com
- Discord: discord.gg/intensescraper
- Dokümantasyon: docs.intensescraper.com

---

**Para Kazanmaya Başla!** 💰

Deployment sonrası ilk 10 müşteriye %50 indirim!

