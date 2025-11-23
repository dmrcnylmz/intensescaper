package com.intensescaper.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kullanicilar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String kullaniciAdi;

    @Column(nullable = false)
    private String parola; // BCrypt ile şifrelenmiş

    @Column(nullable = false, length = 20)
    private String rol = "ROLE_USER";

    @Column(length = 500)
    private String whatsappApiToken; // Şifrelenmiş saklanacak

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime kayitTarihi;

    @OneToMany(mappedBy = "kullanici", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ilan> ilanlar = new ArrayList<>();

    @OneToMany(mappedBy = "kullanici", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MesajSablonu> mesajSablonlari = new ArrayList<>();

    @OneToMany(mappedBy = "kullanici", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScrapingIslem> scrapingIslemleri = new ArrayList<>();

}

