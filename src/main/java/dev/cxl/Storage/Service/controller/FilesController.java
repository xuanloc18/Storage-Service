package dev.cxl.Storage.Service.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import dev.cxl.Storage.Service.dto.response.APIResponse;
import dev.cxl.Storage.Service.entity.Files;
import dev.cxl.Storage.Service.service.FileServices;

@RestController
@RequestMapping("/files")
public class FilesController {
    @Autowired
    FileServices fileServices;

    @PostMapping()
    public APIResponse<String> createFiles(
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam("ownerID") String ownerID,
            @RequestParam("visibility") Boolean visibility)
            throws IOException {
        fileServices.createMoreFile(files, ownerID, visibility);
        return APIResponse.<String>builder().result("Thành công").build();
    }

    @GetMapping("/private/{fileID}")
    public APIResponse<Files> getFilePrivate(@PathVariable("fileID") String fileID) {
        return APIResponse.<Files>builder()
                .result(fileServices.getFilePrivate(fileID))
                .build();
    }

    @GetMapping("/public/{fileID}")
    public APIResponse<Files> getFilePub(@PathVariable("fileID") String fileID) {
        return APIResponse.<Files>builder()
                .result(fileServices.getFilePublic(fileID))
                .build();
    }

    @GetMapping("/public/view-file/{fileID}")
    public ResponseEntity<InputStreamResource> getFileView(@PathVariable("fileID") String fileID)
            throws MalformedURLException, IOException {
        return fileServices.viewFile(fileID);
    }

    @PostMapping("/private/{fileID}/deleted")
    public APIResponse<String> deleteFilePrivate(@PathVariable("fileID") String fileID) {
        fileServices.deleteFile(fileID);
        return APIResponse.<String>builder().result("deleted thành công").build();
    }
    @PostMapping("/private/{fileID}/update")
    public APIResponse<String> updateFile(@PathVariable("fileID") String fileID,@RequestParam("userID") String userID,@RequestParam("file") MultipartFile file) throws IOException {
        fileServices.updateFile(fileID,userID,file);
        return APIResponse.<String>builder().result("update thành công").build();
    }
    @GetMapping("/public/{fileID}/download")
    public ResponseEntity<?> downloadFile(@PathVariable("fileID") String fileID) throws IOException {

        return fileServices.downloadFile(fileID);
    }

}
