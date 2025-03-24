package com.example.underskrift_export.repositories;

import com.example.underskrift_export.models.SignDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignDataRepository extends JpaRepository<SignDataEntity, String> {
}
