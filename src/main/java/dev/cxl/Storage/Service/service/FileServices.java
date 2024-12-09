package dev.cxl.Storage.Service.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.util.Resource;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.cxl.Storage.Service.entity.Files;
import dev.cxl.Storage.Service.exception.AppException;
import dev.cxl.Storage.Service.exception.ErrorCode;
import dev.cxl.Storage.Service.repository.FilesRepository;

@Service
public class FileServices {
    @Autowired
    FilesRepository filesRepository;

    @Autowired
    FileUtils fileUtils;

    @Value("${file.document.path}")
    String documentPath;

    public Boolean createMoreFile(List<MultipartFile> files, String ownerId, Boolean visibility) {
        files.forEach(multipartFile -> {
            try {
                createFile(multipartFile, ownerId, visibility);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return true;
    }

    public Boolean createFile(MultipartFile file, String ownerId, Boolean visibility) throws IOException {
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        String fileNameDes = UUID.randomUUID() + fileName.substring(index, fileName.length());
        Path path = Paths.get(System.getProperty("user.dir"), documentPath);
        String filePath = path + "/" + fileNameDes;
        file.transferTo(new File(filePath));
        filesRepository.save(Files.builder()
                .ownerId(ownerId)
                .fileName(fileName)
                .fileSize(file.getSize())
                .filePath(filePath)
                .visibility(visibility)
                .fileType(file.getContentType())
                .build());

        return true;
    }

    public Files getFilePrivate(String id) {
        Files file = fileUtils.search(id);
        if (!file.getVisibility()) {
            throw new AppException(ErrorCode.FILE_PUBLIC);
        }
        return file;
    }

    public Files getFilePublic(String id) {
        Files file = fileUtils.search(id);
        if (file.getVisibility()) {
            throw new AppException(ErrorCode.FILE_PRIVATE);
        }
        return file;
    }

    public Boolean deleteFile(String id) {
        Files file = fileUtils.search(id);
        file.setDeleted(true);
        filesRepository.save(file);
        return true;
    }

    public ResponseEntity<InputStreamResource> viewFile(String id) throws MalformedURLException, IOException {
        Files file = fileUtils.search(id);
        Path path = Paths.get(file.getFilePath());

        if (!path.toFile().exists()) {
            throw new AppException(ErrorCode.FILE_NOT_EXIST);
        }

        // Xác định loại MIME của file
        String contentType = file.getFileType(); // file.getContentType() hoặc kiểm tra bằng cách khác

        // Đọc file dưới dạng InputStream
        InputStreamResource resource = new InputStreamResource(new FileInputStream(path.toFile()));

        // Đặt headers cho Content-Type và Content-Disposition
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + path.getFileName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(path.toFile().length())
                .body(resource);
    }
    public Files updateFile(String id,String ownerId,MultipartFile fileUpdate) throws MalformedURLException, IOException {
        Files file = fileUtils.search(id);
        if(! file.getOwnerId().equals(ownerId))
        {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        File file1 = new File(file.getFilePath());
        file1.delete();
        String fileName = fileUpdate.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        String fileNameDes = UUID.randomUUID() + fileName.substring(index, fileName.length());
        Path path = Paths.get(System.getProperty("user.dir"), documentPath);
        String filePath = path + "/" + fileNameDes;
        fileUpdate.transferTo(new File(filePath));
        file.setFilePath(filePath);
        return  filesRepository.save(file);

    }
    public ResponseEntity<?> downloadFile(String id) throws IOException {
        Files files=fileUtils.search(id);
        try {
            // Tạo đối tượng Path trỏ đến file
            Path filePath = Paths.get(files.getFilePath());
            UrlResource resource = new UrlResource(filePath.toUri());

            // Kiểm tra xem file có tồn tại không
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Trả về file cùng với header cho trình duyệt tải xuống
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + files.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }


    @PostConstruct
    public void init() throws IOException {
        // Lấy đường dẫn tuyệt đối đến thư mục gốc của dự án và nối thêm thư mục upload
        Path path = Paths.get(System.getProperty("user.dir"), documentPath);

        // Kiểm tra xem thư mục đã tồn tại chưa; nếu chưa, tạo thư mục
        if (!java.nio.file.Files.exists(path)) {
            java.nio.file.Files.createDirectories(path);
            System.out.println("Upload directory created at: " + path.toAbsolutePath());
        } else {
            System.out.println("Upload directory already exists at: " + path.toAbsolutePath());
        }
    }

}
