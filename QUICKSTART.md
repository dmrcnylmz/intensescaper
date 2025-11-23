# 🚀 IntenseScraper - 5 Dakikada Başla!

## 💰 Bu Proje Ne Yapar?

Sahibinden.com'dan otomatik telefon numaralarını çekip WhatsApp'tan toplu mesaj gönderir!

### 💸 Para Kazanma Modeli

1. **SaaS Abonelik:**
   - Başlangıç: 99 TL/ay
   - Profesyonel: 299 TL/ay
   - İşletme: 999 TL/ay

2. **Hedef Kitle:**
   - Emlak ofisleri
   - Oto galeriler
   - İkinci el satıcılar
   - Pazarlama ajansları

## 🎯 Hızlı Başlangıç

### 1. Gereksinimler
```bash
✅ Java 17
✅ Node.js 18+
✅ MySQL (veya H2 dev modu)
```

### 2. Backend Başlat
```bash
cd /Users/pc/Desktop/intensescaper
mvn spring-boot:run
```

### 3. Frontend Başlat
```bash
cd frontend
npm install
npm run dev
```

### 4. Kullan!
```
http://localhost:3000

Kullanıcı: test
Şifre: test123
```

## 📊 Nasıl Çalışır?

```
1. Sahibinden URL'i yapıştır
   ↓
2. Selenium otomatik ilan bilgilerini çeker
   ↓
3. Telefon numarası kaydedilir
   ↓
4. WhatsApp toplu mesaj gönder
   ↓
5. Para kazan! 💰
```

## 🎨 Özellikler

✅ Otomatik veri çekme (Selenium)  
✅ Toplu WhatsApp mesajlaşma  
✅ Mesaj şablonları  
✅ İstatistikler  
✅ Kullanıcı yönetimi (JWT)  
✅ Responsive UI  

## 🔥 Deployment

### Docker ile (En Kolay)
```bash
docker-compose up -d
```

### Manuel
```bash
# Backend
mvn package
java -jar target/intensescaper-backend-1.0.0.jar

# Frontend
cd frontend
npm run build
# dist/ klasörünü serve et
```

## 💡 Abonelik Sistemi Eklemek İçin

### Stripe Entegrasyonu

1. **Dependency ekle:**
```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>24.0.0</version>
</dependency>
```

2. **Controller oluştur:**
```java
@RestController
@RequestMapping("/subscription")
public class SubscriptionController {
    
    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(@RequestBody SubscriptionRequest request) {
        // Stripe ile abonelik oluştur
        // Kullanıcıyı premium yap
        // Mesaj limitini artır
    }
}
```

3. **Frontend'e ödeme sayfası ekle:**
```jsx
import { loadStripe } from '@stripe/stripe-js';

function Subscription() {
  const handlePay = async (plan) => {
    // Stripe Checkout'a yönlendir
    // Ödeme başarılıysa aboneliği aktive et
  };
}
```

## 📈 Pazarlama Stratejisi

1. **İlk 10 Müşteri:** %50 indirim
2. **Referral Program:** Her yeni müşteri için 1 ay bedava
3. **Free Trial:** 7 gün ücretsiz deneme
4. **Landing Page:** Özellikler + Fiyatlandırma + Demo video

## 💻 Tech Stack

- **Backend:** Spring Boot + Selenium
- **Frontend:** React + Tailwind CSS
- **Database:** MySQL / H2
- **Auth:** JWT
- **Messaging:** WhatsApp (simüle + API ready)

## 🎁 Bonus Features Ekle

- [ ] WhatsApp Web Automation (Selenium)
- [ ] Telegram Bot entegrasyonu
- [ ] CSV export/import
- [ ] API for integrations
- [ ] Mobile app (React Native)
- [ ] Analytics dashboard
- [ ] Multi-language support

## 📞 İletişim

Sorular için: github.com/your-repo/issues

---

**PARA KAZANMAYA BAŞLA!** 💰🚀

İlk ayda 10 müşteri x 199 TL = **2.000 TL** gelir!

