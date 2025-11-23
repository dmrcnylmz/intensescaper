package com.intensescaper.repository;

import com.intensescaper.entity.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {
    
    Optional<Kullanici> findByKullaniciAdi(String kullaniciAdi);
    
    boolean existsByKullaniciAdi(String kullaniciAdi);
    
}

