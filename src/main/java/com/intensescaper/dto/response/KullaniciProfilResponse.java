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
public class KullaniciProfilResponse {

    private Long id;
    private String kullaniciAdi;
    private String rol;
    private LocalDateTime kayitTarihi;
    private Boolean whatsappApiTokenTanimli;
    private Long toplamIlanSayisi;
    private Long mesajGonderilenIlanSayisi;

}

