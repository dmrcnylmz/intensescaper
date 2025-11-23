package com.intensescaper.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopluMesajRequest {

    @NotEmpty(message = "İlan ID listesi boş olamaz")
    private List<Long> ilanIdListesi;

    @NotNull(message = "Şablon ID boş olamaz")
    private Long sablonId;

}

