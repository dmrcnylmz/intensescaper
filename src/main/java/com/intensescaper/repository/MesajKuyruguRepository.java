package com.intensescaper.repository;

import com.intensescaper.entity.MesajKuyrugu;
import com.intensescaper.enums.MesajDurumu;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesajKuyruguRepository extends JpaRepository<MesajKuyrugu, Long> {
    
    @Query("SELECT m FROM MesajKuyrugu m WHERE m.durum = :durum ORDER BY m.olusturmaTarihi ASC")
    List<MesajKuyrugu> findByDurumOrderByOlusturmaTarihiAsc(MesajDurumu durum, Pageable pageable);
    
    long countByDurum(MesajDurumu durum);
    
}

