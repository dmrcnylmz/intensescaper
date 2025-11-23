package com.intensescaper.entity;

import com.intensescaper.enums.IslemDurumu;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scraping_islemler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrapingIslem {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IslemDurumu durum = IslemDurumu.BASLADI;

    @Column(nullable = false)
    private Integer toplamIlan = 0;

    @Column(nullable = false)
    private Integer tamamlananIlan = 0;

    @Column(length = 50)
    private String site;

    @Column(length = 1000)
    private String hedefUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime baslamaTarihi;

    @Column
    private LocalDateTime bitisTarihi;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String hataMesaji;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = true) // Allow null for testing
    private Kullanici kullanici;

    @OneToMany(mappedBy = "scrapingIslem", cascade = CascadeType.ALL)
    private List<Ilan> ilanlar = new ArrayList<>();

    public void incrementTamamlanan() {
        this.tamamlananIlan++;
    }

    public double getIlerlemeYuzdesi() {
        if (toplamIlan == 0)
            return 0.0;
        return (tamamlananIlan * 100.0) / toplamIlan;
    }

}
