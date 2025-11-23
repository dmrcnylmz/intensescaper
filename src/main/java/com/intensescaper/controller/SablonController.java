package com.intensescaper.controller;

import com.intensescaper.dto.request.MesajSablonuRequest;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.entity.MesajSablonu;
import com.intensescaper.repository.MesajSablonuRepository;
import com.intensescaper.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sablonlar")
@RequiredArgsConstructor
public class SablonController {

    private final MesajSablonuRepository mesajSablonuRepository;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping
    public ResponseEntity<MesajSablonu> sablonOlustur(
            @Valid @RequestBody MesajSablonuRequest request,
            Authentication authentication) {
        
        Kullanici kullanici = userDetailsService.getKullanici(authentication.getName());
        
        MesajSablonu sablon = new MesajSablonu();
        sablon.setBaslik(request.getBaslik());
        sablon.setIcerik(request.getIcerik());
        sablon.setAciklama(request.getAciklama());
        sablon.setKullanici(kullanici);
        
        MesajSablonu kaydedilen = mesajSablonuRepository.save(sablon);
        
        return ResponseEntity.ok(kaydedilen);
    }

    @GetMapping
    public ResponseEntity<List<MesajSablonu>> sablonlariListele(Authentication authentication) {
        Kullanici kullanici = userDetailsService.getKullanici(authentication.getName());
        List<MesajSablonu> sablonlar = mesajSablonuRepository.findByKullaniciOrderByOlusturmaTarihiDesc(kullanici);
        return ResponseEntity.ok(sablonlar);
    }

}

