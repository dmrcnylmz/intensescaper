package com.intensescaper.service;

import com.intensescaper.dto.request.GirisRequest;
import com.intensescaper.dto.request.KayitRequest;
import com.intensescaper.dto.response.AuthResponse;
import com.intensescaper.entity.Kullanici;
import com.intensescaper.repository.KullaniciRepository;
import com.intensescaper.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KullaniciRepository kullaniciRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private KayitRequest kayitRequest;
    private GirisRequest girisRequest;
    private Kullanici kullanici;

    @BeforeEach
    void setUp() {
        kayitRequest = new KayitRequest("testuser", "password123");
        girisRequest = new GirisRequest("testuser", "password123");
        
        kullanici = new Kullanici();
        kullanici.setId(1L);
        kullanici.setKullaniciAdi("testuser");
        kullanici.setParola("encodedPassword");
        kullanici.setRol("ROLE_USER");
    }

    @Test
    void kayitOl_Success() {
        // Given
        when(kullaniciRepository.existsByKullaniciAdi(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(kullaniciRepository.save(any(Kullanici.class))).thenReturn(kullanici);
        when(jwtUtil.generateToken(anyString())).thenReturn("test-jwt-token");

        // When
        AuthResponse response = authService.kayitOl(kayitRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getKullaniciAdi());
        assertEquals("ROLE_USER", response.getRol());
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("Kayıt başarılı", response.getMesaj());
        
        verify(kullaniciRepository, times(1)).existsByKullaniciAdi("testuser");
        verify(kullaniciRepository, times(1)).save(any(Kullanici.class));
    }

    @Test
    void kayitOl_UserAlreadyExists_ThrowsException() {
        // Given
        when(kullaniciRepository.existsByKullaniciAdi(anyString())).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authService.kayitOl(kayitRequest));
        verify(kullaniciRepository, never()).save(any(Kullanici.class));
    }

    @Test
    void girisYap_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(kullaniciRepository.findByKullaniciAdi(anyString())).thenReturn(Optional.of(kullanici));
        when(jwtUtil.generateToken(anyString())).thenReturn("test-jwt-token");

        // When
        AuthResponse response = authService.girisYap(girisRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getKullaniciAdi());
        assertEquals("ROLE_USER", response.getRol());
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("Giriş başarılı", response.getMesaj());
    }

    @Test
    void girisYap_UserNotFound_ThrowsException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(kullaniciRepository.findByKullaniciAdi(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authService.girisYap(girisRequest));
    }

}

