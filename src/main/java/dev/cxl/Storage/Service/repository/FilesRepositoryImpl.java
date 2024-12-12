package dev.cxl.Storage.Service.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.cxl.Storage.Service.dto.request.FilesSearchRequest;
import dev.cxl.Storage.Service.entity.Files;

@Service
public class FilesRepositoryImpl implements FilesRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Files> search(FilesSearchRequest request) {
        Map<String, Object> values = new HashMap<>();
        String sql =
                "select f from Files f " + createWhereQuery(request, values) + createOrderQuery(request.getSortBy());
        Query query = entityManager.createQuery(sql, Files.class);

        // Set các parameter
        values.forEach(query::setParameter);

        // Tính toán phân trang
        int firstResult = (request.getPageIndex() - 1) * request.getPageSize();
        query.setFirstResult(firstResult);
        query.setMaxResults(request.getPageSize());

        // Lấy kết quả từ database
        List<Files> resultList = query.getResultList();

        // Tạo đối tượng Pageable với pageIndex và pageSize
        Pageable pageable = PageRequest.of(request.getPageIndex() - 1, request.getPageSize());

        // Trả về PageImpl với số lượng bản ghi và các tham số phân trang
        //        new PageImpl<>(resultList, pageable, count(request));
        return new PageImpl<>(resultList, pageable, count(request));
    }

    @Override
    public Long count(FilesSearchRequest request) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select count(f) from Files f " + createWhereQuery(request, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(FilesSearchRequest request, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();
        //        sql.append(" left join RoleEntity r on (e.roleId = r.id) ");
        sql.append(" where f.deleted = false");
        if (StringUtils.isNotBlank(request.getKeyword())) {
            sql.append(" and ( lower(f.fileName) like :keyword" + " or lower(f.fileType) like :keyword)");
            values.put("keyword", "%" + request.getKeyword().toLowerCase() + "%");
        }
        if (StringUtils.isNotBlank(request.getFileName())) {
            sql.append(" and f.fileName = :fileName ");
            values.put("fileName", request.getFileName());
        }
        if (!StringUtils.isEmpty(request.getFileType())) {
            sql.append(" and f.fileType in :fileType ");
            values.put("fileType", request.getFileType());
        }
        return sql.toString();
    }

    public StringBuilder createOrderQuery(String sortBy) {
        StringBuilder query = new StringBuilder(" ");
        if (StringUtils.isNotBlank(sortBy)) {
            query.append("order by f.").append(sortBy.replace(".", " "));
        } else {
            query.append("order by f.fileType desc");
        }
        return query;
    }
}
