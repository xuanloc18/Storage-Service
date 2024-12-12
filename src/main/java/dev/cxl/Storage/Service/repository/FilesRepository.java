package dev.cxl.Storage.Service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.cxl.Storage.Service.entity.Files;

@Repository
public interface FilesRepository extends JpaRepository<Files, String> {

    Boolean existsFilesByFileName(String fileName);

    boolean existsFilesByOwnerIdAndDeletedFalse(String ownerId);

    Optional<Files> findFilesByOwnerIdAndDeletedFalse(String ownerId);

    Optional<Files> findFilesByIDAndDeletedFalse(String id);
}
