package com.intensescaper.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrapingBaslatRequest {

    @NotBlank(message = "URL boş olamaz")
    @Pattern(regexp = "^https?://.*", message = "Geçerli bir URL olmalıdır")
    private String url;

}

