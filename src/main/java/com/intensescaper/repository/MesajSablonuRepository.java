package com.intensescaper.repository;

import com.intensescaper.entity.Kullanici;
import com.intensescaper.entity.MesajSablonu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesajSablonuRepository extends JpaRepository<MesajSablonu, Long> {
    
    List<MesajSablonu> findByKullanici(Kullanici kullanici);
    
    List<MesajSablonu> findByKullaniciOrderByOlusturmaTarihiDesc(Kullanici kullanici);
    
}

