package com.example.my_api_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Getter
@Builder
public class Product { // 상품

    // 상품명, 상품번호(SHIRT-RED-S-001), 상품 타입(의류, 음식.. 등등), 가격, 재고 수량

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //PK

    private String productName;

    // Column은 생략해도 괜찮다 다만 옵션값을 넣으려면 선언
    private String productNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    private Long price;

    private Long stock;

    @Version
    private Long version;

    // 필요한 요소만 바꿀 수 있게 메서드 생성
    public void changeProductName(String changeProductName) {
        this.productName = changeProductName;
    }

    public void increaseStock(Long addStock) {
        this.stock += addStock;
    }

    public void decreaseStock(Long subStock) {
        this.stock -= subStock;
    }

    public void buyProductWithStock(Long orderCount) {
        if (this.getStock() - orderCount < 0) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        this.decreaseStock(orderCount);
    }
}
