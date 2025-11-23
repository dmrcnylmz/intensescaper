package com.intensescaper.controller;

import com.intensescaper.dto.request.TopluMesajRequest;
import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.entity.MesajSablonu;
import com.intensescaper.exception.ResourceNotFoundException;
import com.intensescaper.repository.IlanRepository;
import com.intensescaper.repository.MesajSablonuRepository;
import com.intensescaper.service.CustomUserDetailsService;
import com.intensescaper.service.WhatsappService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mesajlasma")
@RequiredArgsConstructor
public class MesajlasmaController {

    private final WhatsappService whatsappService;
    private final IlanRepository ilanRepository;
    private final MesajSablonuRepository mesajSablonuRepository;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/gonder")
    public ResponseEntity<Map<String, Object>> topluMesajGonder(
            @Valid @RequestBody TopluMesajRequest request,
            Authentication authentication) {
        
        Kullanici kullanici = userDetailsService.getKullanici(authentication.getName());
        
        // İlanları al
        List<Ilan> ilanlar = ilanRepository.findByIdIn(request.getIlanIdListesi());
        
        if (ilanlar.isEmpty()) {
            throw new ResourceNotFoundException("Belirtilen ID'lerle ilan bulunamadı");
        }
        
        // Kullanıcı kontrolü
        for (Ilan ilan : ilanlar) {
            if (!ilan.getKullanici().getId().equals(kullanici.getId())) {
                throw new IllegalArgumentException("Bazı ilanlara erişim yetkiniz yok");
            }
        }
        
        // Şablonu al
        MesajSablonu sablon = mesajSablonuRepository.findById(request.getSablonId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesaj şablonu", "id", request.getSablonId()));
        
        if (!sablon.getKullanici().getId().equals(kullanici.getId())) {
            throw new IllegalArgumentException("Bu şablona erişim yetkiniz yok");
        }
        
        // Toplu mesaj gönder
        whatsappService.topluMesajGonder(ilanlar, sablon);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mesaj", "Toplu mesaj gönderimi başlatıldı");
        response.put("toplamIlan", ilanlar.size());
        response.put("kuyrukDurumu", "Mesajlar kuyruğa eklendi");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kuyruk-durumu")
    public ResponseEntity<Map<String, Object>> kuyruDurumu() {
        long bekleyenMesajSayisi = whatsappService.getKuyruktaBekleyenMesajSayisi();
        
        Map<String, Object> response = new HashMap<>();
        response.put("bekleyenMesajSayisi", bekleyenMesajSayisi);
        
        return ResponseEntity.ok(response);
    }

}

