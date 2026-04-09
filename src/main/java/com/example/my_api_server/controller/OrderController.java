package com.example.my_api_server.controller;

import com.example.my_api_server.service.OrderService;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public OrderResponseDto createOrder(@Validated @RequestBody OrderCreateDto dto) {
        return orderService.createOrderPLock(dto);
    }

    @GetMapping("/{id}")
    public OrderResponseDto findOrder(@PathVariable Long id) {
        return orderService.findOrder(id);
    }
}
