package com.intensescaper.entity;

import com.intensescaper.enums.MesajDurumu;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mesaj_kuyrugu", indexes = {
    @Index(name = "idx_durum", columnList = "durum"),
    @Index(name = "idx_olusturma_tarihi", columnList = "olusturmaTarihi")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesajKuyrugu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ilan_id", nullable = false)
    private Ilan ilan;

    @Column(nullable = false, length = 20)
    private String telefonNumarasi;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mesajIcerigi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MesajDurumu durum = MesajDurumu.BEKLEMEDE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime olusturmaTarihi;

    @Column
    private LocalDateTime gonderimTarihi;

    @Column
    private Integer denemeSayisi = 0;

    @Column(length = 1000)
    private String hataMesaji;

}

