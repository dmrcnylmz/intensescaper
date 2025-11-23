package com.intensescaper.controller;

import com.intensescaper.dto.response.IlanResponse;
import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.exception.ResourceNotFoundException;
import com.intensescaper.repository.IlanRepository;
import com.intensescaper.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ilanlar")
@RequiredArgsConstructor
public class IlanController {

    private final IlanRepository ilanRepository;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping
    public ResponseEntity<Page<IlanResponse>> ilanListesi(
            @RequestParam(required = false) String site,
            @RequestParam(required = false) Boolean mesajGonderildi,
            Pageable pageable,
            Authentication authentication) {
        
        Kullanici kullanici = userDetailsService.getKullanici(authentication.getName());
        
        Page<Ilan> ilanlar = ilanRepository.findByFilters(kullanici, site, mesajGonderildi, pageable);
        
        Page<IlanResponse> response = ilanlar.map(ilan -> IlanResponse.builder()
                .id(ilan.getId())
                .site(ilan.getSite())
                .ilanUrl(ilan.getIlanUrl())
                .baslik(ilan.getBaslik())
                .fiyat(ilan.getFiyat())
                .telefonNumarasi(ilan.getTelefonNumarasi())
                .konum(ilan.getKonum())
                .aciklama(ilan.getAciklama())
                .detayOzellikler(ilan.getDetayOzellikler())
                .ilanNo(ilan.getIlanNo())
                .ilanTarihi(ilan.getIlanTarihi())
                .emlakTipi(ilan.getEmlakTipi())
                .cekilmeTarihi(ilan.getCekilmeTarihi())
                .mesajGonderildi(ilan.getMesajGonderildi())
                .build());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IlanResponse> ilanDetay(@PathVariable Long id, Authentication authentication) {
        Kullanici kullanici = userDetailsService.getKullanici(authentication.getName());
        
        Ilan ilan = ilanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("İlan", "id", id));
        
        // Kullanıcı kontrolü
        if (!ilan.getKullanici().getId().equals(kullanici.getId())) {
            throw new IllegalArgumentException("Bu ilana erişim yetkiniz yok");
        }
        
        IlanResponse response = IlanResponse.builder()
                .id(ilan.getId())
                .site(ilan.getSite())
                .ilanUrl(ilan.getIlanUrl())
                .baslik(ilan.getBaslik())
                .fiyat(ilan.getFiyat())
                .telefonNumarasi(ilan.getTelefonNumarasi())
                .konum(ilan.getKonum())
                .aciklama(ilan.getAciklama())
                .detayOzellikler(ilan.getDetayOzellikler())
                .ilanNo(ilan.getIlanNo())
                .ilanTarihi(ilan.getIlanTarihi())
                .emlakTipi(ilan.getEmlakTipi())
                .cekilmeTarihi(ilan.getCekilmeTarihi())
                .mesajGonderildi(ilan.getMesajGonderildi())
                .build();
        
        return ResponseEntity.ok(response);
    }

}

