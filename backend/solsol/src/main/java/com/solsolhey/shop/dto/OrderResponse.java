package com.solsolhey.shop.dto;

import com.solsolhey.shop.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주문 응답")
public class OrderResponse {
    
    @Schema(description = "주문 ID", example = "1")
    private Long orderId;
    
    @Schema(description = "메시지", example = "주문이 완료되었습니다!")
    private String message;
    
    @Schema(description = "총 가격", example = "100")
    private Integer totalPrice;
    
    @Schema(description = "주문 상태", example = "COMPLETED")
    private String status;
    
    public static OrderResponse from(Order order, String message) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .message(message)
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .build();
    }
}

