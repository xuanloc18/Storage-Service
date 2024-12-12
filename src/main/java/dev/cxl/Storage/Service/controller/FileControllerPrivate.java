package dev.cxl.Storage.Service.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import dev.cxl.Storage.Service.dto.request.FilesSearchRequest;
import dev.cxl.Storage.Service.dto.response.APIResponse;
import dev.cxl.Storage.Service.entity.Files;
import dev.cxl.Storage.Service.repository.FilesRepositoryImpl;
import dev.cxl.Storage.Service.service.FileServices;
import dev.cxl.Storage.Service.service.FileUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/private/files")
public class FileControllerPrivate {

    private final FileUtils fileUtils;

    private final FileServices fileServices;

    private final FilesRepositoryImpl filesRepositoryImpl;

    @PostMapping()
    public APIResponse<String> createFiles(
            @RequestParam("file") List<MultipartFile> files, @RequestParam("ownerID") String ownerID)
            throws IOException {
        fileServices.createMoreFile(files, ownerID, false);
        return APIResponse.<String>builder().result("Thành công").build();
    }

    @GetMapping("/{fileID}")
    public APIResponse<Files> getFilePrivate(@PathVariable("fileID") String fileID) {
        fileUtils.checkPrivate(fileID);
        return APIResponse.<Files>builder().result(fileServices.getFile(fileID)).build();
    }

    @GetMapping("/view-file/{fileID}")
    public ResponseEntity<InputStreamResource> getFileView(
            @PathVariable("fileID") String fileID,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam(value = "ratio", required = false) Double ratio)
            throws IOException {

        fileUtils.checkPrivate(fileID);
        return fileServices.viewFile(fileID, width, height, ratio);
    }

    @PostMapping("/{fileID}/deleted")
    public APIResponse<String> deleteFilePrivate(@PathVariable("fileID") String fileID) {
        fileServices.deleteFile(fileID);
        return APIResponse.<String>builder().result("deleted thành công").build();
    }

    @GetMapping("/{fileID}/download")
    public ResponseEntity<?> downloadFile(@PathVariable("fileID") String fileID) throws IOException {

        return fileServices.downloadFile(fileID);
    }

    @PostMapping("/getFiles")
    public APIResponse<Page<Files>> getFiles(@RequestBody FilesSearchRequest request) throws IOException {

        return APIResponse.<Page<Files>>builder()
                .result(filesRepositoryImpl.search(request))
                .build();
    }
}
