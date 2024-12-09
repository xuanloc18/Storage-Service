package dev.cxl.Storage.Service.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "files")
public class Files extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    String iD;

    @Column(name = "file_name")
    String fileName;

    @Column(name = "file_type")
    String fileType;

    @Column(name = "file_size")
    Long fileSize;

    @Column(name = "file_path")
    String filePath;

    @Column(name = "file_version")
    String fileVersion;

    @Column(name = "visibility")
    Boolean visibility;

    @Column(name = "owner_id")
    String ownerId;

    @Column(name = "deleted")
    Boolean deleted;
}
