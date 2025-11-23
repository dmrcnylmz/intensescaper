package com.intensescaper.repository;

import com.intensescaper.entity.Kullanici;
import com.intensescaper.entity.ScrapingIslem;
import com.intensescaper.enums.IslemDurumu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapingIslemRepository extends JpaRepository<ScrapingIslem, String> {
    
    Page<ScrapingIslem> findByKullaniciOrderByBaslamaTarihiDesc(Kullanici kullanici, Pageable pageable);
    
    List<ScrapingIslem> findByKullaniciAndDurum(Kullanici kullanici, IslemDurumu durum);
    
}

