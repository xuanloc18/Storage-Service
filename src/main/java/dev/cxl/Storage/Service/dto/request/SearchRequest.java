package dev.cxl.Storage.Service.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchRequest {
    private String keyword; // Từ khóa chung cho nhiều trường
    private int pageIndex; // Trang bắt đầu
    private int pageSize; // Số lượng bản ghi trên mỗi trang
    private String sortBy;
}
