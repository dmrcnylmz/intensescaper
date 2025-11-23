package com.intensescaper.repository;

import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IlanRepository extends JpaRepository<Ilan, Long> {
    
    Page<Ilan> findByKullanici(Kullanici kullanici, Pageable pageable);
    
    Page<Ilan> findByKullaniciAndSite(Kullanici kullanici, String site, Pageable pageable);
    
    Page<Ilan> findByKullaniciAndMesajGonderildi(Kullanici kullanici, Boolean mesajGonderildi, Pageable pageable);
    
    @Query("SELECT i FROM Ilan i WHERE i.kullanici = :kullanici " +
           "AND (:site IS NULL OR i.site = :site) " +
           "AND (:mesajGonderildi IS NULL OR i.mesajGonderildi = :mesajGonderildi)")
    Page<Ilan> findByFilters(@Param("kullanici") Kullanici kullanici,
                              @Param("site") String site,
                              @Param("mesajGonderildi") Boolean mesajGonderildi,
                              Pageable pageable);
    
    List<Ilan> findByIdIn(List<Long> ids);
    
    long countByKullanici(Kullanici kullanici);
    
    long countByKullaniciAndMesajGonderildi(Kullanici kullanici, Boolean mesajGonderildi);
    
}

