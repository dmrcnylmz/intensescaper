package com.intensescaper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intensescaper.dto.request.GirisRequest;
import com.intensescaper.dto.request.KayitRequest;
import com.intensescaper.dto.response.AuthResponse;
import com.intensescaper.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void kayitOl_ValidRequest_ReturnsAuthResponse() throws Exception {
        // Given
        KayitRequest request = new KayitRequest("testuser", "password123");
        AuthResponse response = AuthResponse.builder()
                .token("test-jwt-token")
                .kullaniciAdi("testuser")
                .rol("ROLE_USER")
                .mesaj("Kayıt başarılı")
                .build();

        when(authService.kayitOl(any(KayitRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/kayit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.kullaniciAdi").value("testuser"))
                .andExpect(jsonPath("$.rol").value("ROLE_USER"))
                .andExpect(jsonPath("$.mesaj").value("Kayıt başarılı"));
    }

    @Test
    void kayitOl_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - kullanıcı adı çok kısa
        KayitRequest request = new KayitRequest("te", "password123");

        // When & Then
        mockMvc.perform(post("/auth/kayit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void girisYap_ValidRequest_ReturnsAuthResponse() throws Exception {
        // Given
        GirisRequest request = new GirisRequest("testuser", "password123");
        AuthResponse response = AuthResponse.builder()
                .token("test-jwt-token")
                .kullaniciAdi("testuser")
                .rol("ROLE_USER")
                .mesaj("Giriş başarılı")
                .build();

        when(authService.girisYap(any(GirisRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/giris")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.kullaniciAdi").value("testuser"));
    }

}

