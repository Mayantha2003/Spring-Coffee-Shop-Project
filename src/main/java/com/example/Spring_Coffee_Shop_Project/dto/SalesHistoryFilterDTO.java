package com.example.Spring_Coffee_Shop_Project.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesHistoryFilterDTO {
    private Long batchId;
    private Integer year;
    private Integer month;          // 1-12
    private Integer day;            // 1-31

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private String sort = "newest"; // newest | oldest | highest | lowest
    private String groupBy = "customer"; // customer | batch
}