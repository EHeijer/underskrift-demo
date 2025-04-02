package com.example.underskrift_export.repositories;

import com.example.underskrift_export.models.SignDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface SignDataRepository extends JpaRepository<SignDataEntity, String> {

    @Modifying
    @Query(value = "DELETE FROM signature_data s WHERE s.saved_at < :now", nativeQuery = true)
    void deleteAllBeforeNow(@Param("now") OffsetDateTime now);
}
