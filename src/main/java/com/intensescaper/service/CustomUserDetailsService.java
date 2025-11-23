package com.intensescaper.service;

import com.intensescaper.entity.Kullanici;
import com.intensescaper.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final KullaniciRepository kullaniciRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));

        return User.builder()
                .username(kullanici.getKullaniciAdi())
                .password(kullanici.getParola())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(kullanici.getRol())))
                .build();
    }

    public Kullanici getKullanici(String username) {
        return kullaniciRepository.findByKullaniciAdi(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
    }

}

