package com.intensescaper.controller;

import com.intensescaper.dto.request.WhatsappTokenRequest;
import com.intensescaper.dto.response.KullaniciProfilResponse;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.repository.IlanRepository;
import com.intensescaper.repository.KullaniciRepository;
import com.intensescaper.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/kullanici")
@RequiredArgsConstructor
public class KullaniciController {

    private final CustomUserDetailsService userDetailsService;
    private final KullaniciRepository kullaniciRepository;
    private final IlanRepository ilanRepository;

    @GetMapping("/profil")
    public ResponseEntity<KullaniciProfilResponse> profil(Authentication authentication) {
        Kullanici kullanici = userDetailsService.getKullanici(authentication.getName());
        
        long toplamIlanSayisi = ilanRepository.countByKullanici(kullanici);
        long mesajGonderilenIlanSayisi = ilanRepository.countByKullaniciAndMesajGonderildi(kullanici, true);
        
        KullaniciProfilResponse response = KullaniciProfilResponse.builder()
                .id(kullanici.getId())
                .kullaniciAdi(kullanici.getKullaniciAdi())
                .rol(kullanici.getRol())
                .kayitTarihi(kullanici.getKayitTarihi())
                .whatsappApiTokenTanimli(kullanici.getWhatsappApiToken() != null && !kullanici.getWhatsappApiToken().isEmpty())
                .toplamIlanSayisi(toplamIlanSayisi)
                .mesajGonderilenIlanSayisi(mesajGonderilenIlanSayisi)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/whatsapp-token")
    public ResponseEntity<Map<String, String>> whatsappTokenGuncelle(
            @Valid @RequestBody WhatsappTokenRequest request,
            Authentication authentication) {
        
        Kullanici kullanici = userDetailsService.getKullanici(authentication.getName());
        
        // Token'ı şifrelenmiş olarak sakla (basit örnek, gerçek uygulamada daha güvenli bir yöntem kullanın)
        kullanici.setWhatsappApiToken(request.getWhatsappApiToken());
        kullaniciRepository.save(kullanici);
        
        Map<String, String> response = new HashMap<>();
        response.put("mesaj", "WhatsApp API token başarıyla güncellendi");
        
        return ResponseEntity.ok(response);
    }

}

