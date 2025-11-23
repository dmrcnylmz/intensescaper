package com.intensescaper.controller;

import com.intensescaper.dto.request.GirisRequest;
import com.intensescaper.dto.request.KayitRequest;
import com.intensescaper.dto.response.AuthResponse;
import com.intensescaper.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kayit")
    public ResponseEntity<AuthResponse> kayitOl(@Valid @RequestBody KayitRequest request) {
        AuthResponse response = authService.kayitOl(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/giris")
    public ResponseEntity<AuthResponse> girisYap(@Valid @RequestBody GirisRequest request) {
        AuthResponse response = authService.girisYap(request);
        return ResponseEntity.ok(response);
    }

}

