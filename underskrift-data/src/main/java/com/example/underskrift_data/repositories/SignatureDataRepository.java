package com.example.underskrift_data.repositories;

import com.example.underskrift_data.models.entity.SignatureDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureDataRepository extends JpaRepository<SignatureDataEntity, Long> {
}
