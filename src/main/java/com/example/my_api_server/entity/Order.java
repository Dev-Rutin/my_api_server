package com.example.my_api_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@Getter
@Builder
public class Order {

    // 상품(N) 바지, 신발, 모자 등 : 주문(1) 1:N 관계를 나타내야 한다.
    // 주문(1) <-> 주문목록(여러가지 품목) <-> 상품(1)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderProduct> orderProducts = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK
    // 구매자, 주문 상태, 주문 시간
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member buyer; // 구매자
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문상태
    @Column(nullable = false)
    private LocalDateTime orderTime; // 주문시간

    public static Order createOrder(Member member, LocalDateTime orderTime) {
        Order order = Order.builder()
                .buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(orderTime)
                .build();


        return order;
    }

    public OrderProduct createOrderProduct(Long orderCount, Product product) {
        OrderProduct orderProduct = OrderProduct.builder()
                .order(this)
                .number(orderCount)
                .product(product)
                .build();
        return orderProduct;
    }

    //양방향 매핑
    public void addOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

}
