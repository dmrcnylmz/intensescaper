package com.intensescaper.dto.response;

import com.intensescaper.enums.IslemDurumu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapingIslemResponse {

    private String islemId;
    private IslemDurumu durum;
    private String site;
    private String hedefUrl;
    private Integer toplamIlan;
    private Integer tamamlananIlan;
    private Double ilerlemeYuzdesi;
    private LocalDateTime baslamaTarihi;
    private LocalDateTime bitisTarihi;
    private String hataMesaji;

}

