package dev.cxl.Storage.Service.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import dev.cxl.Storage.Service.dto.response.APIResponse;
import dev.cxl.Storage.Service.entity.Files;
import dev.cxl.Storage.Service.service.FileServices;
import dev.cxl.Storage.Service.service.FileUtils;

@RestController
@RequestMapping("/public/files")
@RequiredArgsConstructor
public class FilesControllerPublic {

    private final FileUtils fileUtils;


    private final FileServices fileServices;

    @PostMapping()
    public APIResponse<String> createFiles(
            @RequestParam("file") List<MultipartFile> files, @RequestParam("ownerID") String ownerID)
            throws IOException {
        fileServices.createMoreFile(files, ownerID, true);
        return APIResponse.<String>builder().result("Thành công").build();
    }

    @GetMapping("/{fileID}")
    public APIResponse<Files> getFilePub(@PathVariable("fileID") String fileID) {
        fileUtils.checkPublic(fileID);
        return APIResponse.<Files>builder().result(fileServices.getFile(fileID)).build();
    }

    @GetMapping("/view-file/{fileID}")
    public ResponseEntity<InputStreamResource> getFileView(
            @PathVariable("fileID") String fileID,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam(value = "ratio", required = false) Double ratio)
            throws MalformedURLException, IOException {
        fileUtils.checkPublic(fileID);
        return fileServices.viewFile(fileID, width, height, ratio);
    }
}
