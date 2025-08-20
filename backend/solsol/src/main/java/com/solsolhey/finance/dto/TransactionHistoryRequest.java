package com.solsolhey.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "계좌 거래내역 조회 요청")
public class TransactionHistoryRequest {
    
    @NotBlank(message = "계좌번호는 필수입니다")
    @Schema(description = "계좌번호", example = "001-123456-789")
    private String accountNumber;
    
    @Schema(description = "조회 시작일", example = "20240101")
    private String startDate;
    
    @Schema(description = "조회 종료일", example = "20241231")
    private String endDate;
    
    @Min(value = 1, message = "조회 개수는 최소 1개 이상이어야 합니다")
    @Max(value = 100, message = "조회 개수는 최대 100개까지 가능합니다")
    @Schema(description = "조회할 거래내역 개수", example = "10", defaultValue = "10")
    private Integer size = 10;
}
