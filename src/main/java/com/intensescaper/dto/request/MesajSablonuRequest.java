package com.intensescaper.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesajSablonuRequest {

    @NotBlank(message = "Başlık boş olamaz")
    @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
    private String baslik;

    @NotBlank(message = "İçerik boş olamaz")
    private String icerik;

    private String aciklama;

}

