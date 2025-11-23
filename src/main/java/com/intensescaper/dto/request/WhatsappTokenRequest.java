package com.intensescaper.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappTokenRequest {

    @NotBlank(message = "WhatsApp API token boş olamaz")
    private String whatsappApiToken;

}

