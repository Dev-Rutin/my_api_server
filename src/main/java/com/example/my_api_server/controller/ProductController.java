package com.example.my_api_server.controller;

import com.example.my_api_server.service.ProductService;
import com.example.my_api_server.service.dto.ProductCreateDto;
import com.example.my_api_server.service.dto.ProductResponseDto;
import com.example.my_api_server.service.dto.ProductUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ProductResponseDto createProduct(@Validated @RequestBody ProductCreateDto dto) {

        ProductResponseDto resDto = productService.createProduct(dto);

        return resDto;
    }

    @GetMapping("/{id}")
    public ProductResponseDto findProduct(@PathVariable Long id) {
        ProductResponseDto resDto = productService.findProduct(id);
        return resDto;
    }

    // @PutMapping = 전체 수정
    @PatchMapping // 일부 수정
    public ProductResponseDto updateProduct(@Validated @RequestBody ProductUpdateDto dto) {
        ProductResponseDto resDto = productService.updateProduct(dto);
        return resDto;
    }
}
