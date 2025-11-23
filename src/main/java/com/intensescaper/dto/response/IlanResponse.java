package com.intensescaper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IlanResponse {

    private Long id;
    private String site;
    private String ilanUrl;
    private String baslik;
    private Double fiyat;
    private String telefonNumarasi;
    private String konum;
    private String aciklama;
    private String detayOzellikler; // JSON string
    private String ilanNo;
    private String ilanTarihi;
    private String emlakTipi;
    private LocalDateTime cekilmeTarihi;
    private Boolean mesajGonderildi;

}

