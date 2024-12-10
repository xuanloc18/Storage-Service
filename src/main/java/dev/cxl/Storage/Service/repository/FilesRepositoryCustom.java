package dev.cxl.Storage.Service.repository;

import org.springframework.data.domain.Page;

import dev.cxl.Storage.Service.dto.request.FilesSearchRequest;
import dev.cxl.Storage.Service.entity.Files;

public interface FilesRepositoryCustom {
    Page<Files> search(FilesSearchRequest request);

    Long count(FilesSearchRequest request);
}
