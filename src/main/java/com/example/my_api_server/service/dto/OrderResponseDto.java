package com.example.my_api_server.service.dto;

import com.example.my_api_server.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(staticName = "of")
@Builder
public class OrderResponseDto {

    // 주문 완료 시간, 주문 상태, 주문 성공 여부
    private LocalDateTime orderCompletedTime;

    private OrderStatus orderStatus;

    private boolean isSuccess;
}
