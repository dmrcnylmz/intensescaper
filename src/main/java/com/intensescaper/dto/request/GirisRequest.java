package com.intensescaper.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GirisRequest {

    @NotBlank(message = "Kullanıcı adı boş olamaz")
    private String kullaniciAdi;

    @NotBlank(message = "Şifre boş olamaz")
    private String parola;

}

