package com.example.demo.repository;

import com.example.demo.model.FileDetails;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDetailsRepository extends JpaRepository<FileDetails, UUID> {
    // Custom queries can be added here if needed
}
