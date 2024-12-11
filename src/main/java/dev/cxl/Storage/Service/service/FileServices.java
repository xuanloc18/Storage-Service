package dev.cxl.Storage.Service.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;

import jakarta.annotation.PostConstruct;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Path uploadPath = Paths.get(documentPath, currentDate);
        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath); // Tạo thư mục
        }
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        String fileNameDes = UUID.randomUUID() + fileName.substring(index, fileName.length());
        Path path = Paths.get(System.getProperty("user.dir"), String.valueOf(uploadPath));
        String filePath = path + "/" + fileNameDes;
        file.transferTo(new File(filePath));
        filesRepository.save(Files.builder()
                .ownerId(ownerId)
                .fileName(fileName)
                .fileSize(file.getSize())
                .filePath(filePath)
                .visibility(visibility)
                .fileType(file.getContentType())
                .deleted(false)
                .build());

        return true;
    }

    public Files getFile(String id) {
        Files file = fileUtils.search(id);
        return file;
    }

    public Boolean deleteFile(String id) {
        Files file = fileUtils.search(id);
        file.setDeleted(true);
        File file1 = new File(file.getFilePath());
        file1.delete();
        filesRepository.save(file);
        return true;
    }

    public ResponseEntity<?> downloadFile(String id) throws IOException {
        Files files = fileUtils.search(id); // Giả sử fileUtils đã được khởi tạo và có phương thức search(id)

        try {
            // Tạo đối tượng Path trỏ đến file
            Path filePath = Paths.get(files.getFilePath());
            UrlResource resource = new UrlResource(filePath.toUri());
            // Kiểm tra xem file có tồn tại không
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            // Xác định loại MIME của file
            String mimeType = files.getFileType();
            // Kiểm tra nếu là ảnh (image/jpeg, image/png, image/gif, v.v.)
            if (mimeType.startsWith("image/")) {
                // Trả về file trực tiếp cho trình duyệt để hiển thị
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(mimeType)) // Đảm bảo trình duyệt nhận diện đúng loại file
                        .body(resource);
            } else {
                // Nếu không phải ảnh, yêu cầu tải xuống
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(mimeType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + files.getFileName() + "\"")
                        .body(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    public ResponseEntity<Resource> downloadFile2(String fileId) throws IOException {
        Resource resource = getContent(fileId);
        Files file = fileUtils.search(fileId);
        String mimeType = file.getFileType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }
    public Resource getContent(String fileId) throws IOException {
        Files fileEntity = fileUtils.search(fileId);
        Path path = Paths.get(fileEntity.getFilePath());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new IOException("File not found: " + fileEntity.getFileName());
        }
        return resource;
    }



    public ResponseEntity<InputStreamResource> viewFile(String id, Integer width, Integer height, Double ratio)
            throws IOException {
        // Tìm file
        Files file = fileUtils.search(id);
        Path path = Paths.get(file.getFilePath());

        if (!path.toFile().exists()) {
            throw new AppException(ErrorCode.FILE_NOT_EXIST);
        }

        // Kiểm tra loại file là ảnh
        String contentType = file.getFileType();
        if (!contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        // Đọc file gốc
        BufferedImage originalImage = ImageIO.read(path.toFile());
        BufferedImage resizedImage = originalImage;

        // Điều chỉnh kích thước theo yêu cầu
        if (ratio != null) {
            int newWidth = (int) (originalImage.getWidth() * ratio);
            int newHeight = (int) (originalImage.getHeight() * ratio);
            resizedImage = resizeImage(originalImage, newWidth, newHeight);
        } else if (width != null || height != null) {
            int newWidth = width != null ? width : originalImage.getWidth();
            int newHeight = height != null ? height : originalImage.getHeight();
            resizedImage = resizeImage(originalImage, newWidth, newHeight);
        }

        // Ghi ảnh đã chỉnh sửa vào OutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", outputStream); // Đảm bảo đúng format (png/jpg)
        byte[] imageBytes = outputStream.toByteArray();

        // Tạo InputStreamResource từ byte[]
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(imageBytes));

        // Trả về kết quả
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + path.getFileName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(imageBytes.length)
                .body(resource);
    }

    // Helper: Resize ảnh
    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        return resizedImage;
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
