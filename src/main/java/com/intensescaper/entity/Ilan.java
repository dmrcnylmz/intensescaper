package com.intensescaper.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ilanlar", indexes = {
    @Index(name = "idx_site", columnList = "site"),
    @Index(name = "idx_mesaj_gonderildi", columnList = "mesajGonderildi"),
    @Index(name = "idx_cekilme_tarihi", columnList = "cekilmeTarihi")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ilan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String site; // sahibinden, emlakjet, arabam

    @Column(nullable = false, length = 1000)
    private String ilanUrl;

    @Column(nullable = false, length = 500)
    private String baslik;

    @Column
    private Double fiyat;

    @Column(nullable = false, length = 20)
    private String telefonNumarasi; // En kritik veri

    @Column(length = 200)
    private String konum;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String aciklama;
    
    // Detaylı özellikler (JSON formatında)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String detayOzellikler; // {"Oda Sayısı": "2+1", "m²": "90", ...}
    
    @Column(length = 50)
    private String ilanNo; // 1283635133
    
    @Column(length = 50)
    private String ilanTarihi; // "17 Kasım 2025"
    
    @Column(length = 100)
    private String emlakTipi; // "Satılık Daire"

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime cekilmeTarihi;

    @Column(nullable = false)
    private Boolean mesajGonderildi = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private Kullanici kullanici;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scraping_islem_id")
    private ScrapingIslem scrapingIslem;

}

