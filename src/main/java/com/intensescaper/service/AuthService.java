package com.intensescaper.service;

import com.intensescaper.dto.request.GirisRequest;
import com.intensescaper.dto.request.KayitRequest;
import com.intensescaper.dto.response.AuthResponse;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.repository.KullaniciRepository;
import com.intensescaper.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse kayitOl(KayitRequest request) {
        // Kullanıcı adı kontrolü
        if (kullaniciRepository.existsByKullaniciAdi(request.getKullaniciAdi())) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten kullanılıyor");
        }

        // Yeni kullanıcı oluştur
        Kullanici kullanici = new Kullanici();
        kullanici.setKullaniciAdi(request.getKullaniciAdi());
        kullanici.setParola(passwordEncoder.encode(request.getParola()));
        kullanici.setRol("ROLE_USER");

        kullaniciRepository.save(kullanici);
        log.info("Yeni kullanıcı kaydedildi: {}", kullanici.getKullaniciAdi());

        // JWT token oluştur
        String token = jwtUtil.generateToken(kullanici.getKullaniciAdi());

        return AuthResponse.builder()
                .token(token)
                .kullaniciAdi(kullanici.getKullaniciAdi())
                .rol(kullanici.getRol())
                .mesaj("Kayıt başarılı")
                .build();
    }

    public AuthResponse girisYap(GirisRequest request) {
        // Kimlik doğrulama
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getKullaniciAdi(), request.getParola())
        );

        // Kullanıcıyı bul
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(request.getKullaniciAdi())
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        // JWT token oluştur
        String token = jwtUtil.generateToken(kullanici.getKullaniciAdi());

        log.info("Kullanıcı giriş yaptı: {}", kullanici.getKullaniciAdi());

        return AuthResponse.builder()
                .token(token)
                .kullaniciAdi(kullanici.getKullaniciAdi())
                .rol(kullanici.getRol())
                .mesaj("Giriş başarılı")
                .build();
    }

}

