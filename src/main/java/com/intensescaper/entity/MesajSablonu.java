package com.intensescaper.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mesaj_sablonlari")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesajSablonu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String baslik;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String icerik; // Placeholder destekli: {{telefonNumarasi}}, {{baslik}}, {{fiyat}}, {{konum}}, {{kullaniciAdi}}

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime olusturmaTarihi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    @JsonIgnore
    private Kullanici kullanici;

    @Column(length = 1000)
    private String aciklama; // Şablonun ne için kullanıldığına dair not

}

