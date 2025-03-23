package com.example.underskrift_data.repositories;

import com.example.underskrift_data.models.SignDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignDataRepository extends JpaRepository<SignDataEntity, String> {
}
