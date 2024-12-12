package dev.cxl.Storage.Service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.cxl.Storage.Service.entity.Files;
import dev.cxl.Storage.Service.exception.AppException;
import dev.cxl.Storage.Service.exception.ErrorCode;
import dev.cxl.Storage.Service.repository.FilesRepository;

@Service
public class FileUtils {
    @Autowired
    FilesRepository filesRepository;

    public void checkFileExist(String fileName) {
        if (filesRepository.existsFilesByFileName(fileName)) {
            throw new AppException(ErrorCode.FILE_EXIST);
        }
    }

    public void checkFileNotExist(String fileName) {
        if (!filesRepository.existsFilesByFileName(fileName)) {
            throw new AppException(ErrorCode.FILE_NOT_EXIST);
        }
    }

    public Files search(String id) {
        Files files = filesRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_EXIST));
        return files;
    }

    public Files searchProfile(String id) {
        Files files = filesRepository
                .findFilesByIDAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_EXIST));
        return files;
    }

    public void checkPublic(String id) {
        Files files = search(id);
        if (!files.getVisibility()) {
            throw new AppException(ErrorCode.FILE_PRIVATE);
        }
    }

    public void checkPrivate(String id) {
        Files files = search(id);
        if (files.getVisibility()) {
            throw new AppException(ErrorCode.FILE_PUBLIC);
        }
    }
}
