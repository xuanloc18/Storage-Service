package dev.cxl.Storage.Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.cxl.Storage.Service.entity.Files;

@Repository
public interface FilesRepository extends JpaRepository<Files, String> {

    Boolean existsFilesByFileName(String fileName);
}
