package com.example.my_api_server.service;

import com.example.my_api_server.entity.*;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepo orderRepo;
    private final MemberDBRepo memberRepo;
    private final ProductRepo productRepo;

    // 주문 생성
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto, LocalDateTime orderTime) {
        Member member = memberRepo.findById(dto.memberId())
                .orElseThrow(() -> new RuntimeException("회원 정보가 없습니다."));

        // 점심시간 이벤트 쿠폰 발행(특정 시간에 대한 로직 수행 가정)
        if (orderTime.getHour() == 13) {
            return null;
        }

        Order order = Order.createOrder(member, orderTime);

        // 1 주문에 여러 상품

        // 상품 id로 조회
/*        List<Product> products = dto.productId().stream()
                .map((pId) -> productRepo.findById(pId).orElseThrow())
                .toList();*/
        List<Product> products = productRepo.findAllById(dto.productId()); // IN 쿼리
        // product <-> 주문 개수 매핑
        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {
                    // 재고 차감
                    Product product = products.get(idx);
                    Long orderCount = dto.count().get(idx);

                    product.buyProductWithStock(orderCount);

                    return order.createOrderProduct(orderCount, product);
                }).toList();

        order.addOrderProducts(orderProducts);
//        HashMap<Product, Long> productCountMap = new HashMap<>();
//        for (int i = 0; i < dto.count().size(); i++) {
//            productCountMap.put(products.get(i), dto.count().get(i));
//        }
//
//        // 상품 개수 만큼 오더 생성
//        List<OrderProduct> orderProducts = products.stream()
//                .map(p -> OrderProduct.builder()
//                        .order(order)
//                        .number(productCountMap.get(p))
//                        .product(p)
//                        .build()
//                ).toList();
        // orderProduct 생명주기 order와 싱크 생성

        // save 전까지는 영속화를 하지 않음
        Order savedOrder = orderRepo.save(order);
        // save 이후 영속화

        // Entity -> Dto 변환
        return OrderResponseDto.of(
                savedOrder.getOrderTime(),
                OrderStatus.COMPLETED,
                true);
    }

    //주문 상태 변경 직접 구현
    public OrderResponseDto changeStatusCompleted(Long orderId) {

        return null;
    }

    // 주문 조회

    /**
     * JPA는 내부적으로 캐시 매커니즘을 사용
     * - 내부에 1차 캐시, 2차 캐시가 존재
     * - 1차 캐시 = Entity를 내부적으로 영속화
     * - readonly = true 시 내부 하이버네이트 동작 원리가 간소화 된다.(dirty checking X)
     */
    @Transactional(readOnly = true)
    public OrderResponseDto findOrder(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(); // ID로 주문 조회

        // 조회 정보 -> DTO  변환
        OrderResponseDto orderResponseDto = OrderResponseDto.of(order.getOrderTime(), order.getOrderStatus(), true);

        return orderResponseDto;
    }

    //낙관락 적용 예시
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(includes = ObjectOptimisticLockingFailureException.class, maxRetries = 3)
    public OrderResponseDto createOrderOptLock(OrderCreateDto dto) {
        log.info("Retryable test");
        Member member = memberRepo.findById(dto.memberId()).orElseThrow();

        LocalDateTime orderTime = LocalDateTime.now();

        Order order = Order.builder()
                .buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(orderTime)
                .build();

        // 1 주문에 여러 상품

        // 상품 id로 조회
/*        List<Product> products = dto.productId().stream()
                .map((pId) -> productRepo.findById(pId).orElseThrow())
                .toList();*/
        List<Product> products = productRepo.findAllById(dto.productId()); // IN 쿼리

        // product <-> 주문 개수 매핑
        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {
                    // 재고 차감
                    Product product = products.get(idx);
                    if (product.getStock() - dto.count().get(idx) < 0) {
                        throw new RuntimeException("재고가 부족합니다.");
                    }

                    product.decreaseStock(dto.count().get(idx));

                    return OrderProduct.builder()
                            .order(order)
                            .number(dto.count().get(idx))
                            .product(products.get(idx))
                            .build();
                }).toList();

//        HashMap<Product, Long> productCountMap = new HashMap<>();
//        for (int i = 0; i < dto.count().size(); i++) {
//            productCountMap.put(products.get(i), dto.count().get(i));
//        }
//
//        // 상품 개수 만큼 오더 생성
//        List<OrderProduct> orderProducts = products.stream()
//                .map(p -> OrderProduct.builder()
//                        .order(order)
//                        .number(productCountMap.get(p))
//                        .product(p)
//                        .build()
//                ).toList();
        // orderProduct 생명주기 order와 싱크 생성
        order.addOrderProducts(orderProducts);

        // save 전까지는 영속화를 하지 않음
        Order savedOrder = orderRepo.save(order);
        // save 이후 영속화

        // Entity -> Dto 변환
        OrderResponseDto orderResponseDto = OrderResponseDto.of(
                savedOrder.getOrderTime(),
                OrderStatus.COMPLETED,
                true);


        return orderResponseDto;
    }

    //비관적 락 예시
    @Transactional
    public OrderResponseDto createOrderPLock(OrderCreateDto dto) {
        Member member = memberRepo.findById(dto.memberId()).orElseThrow();

        LocalDateTime orderTime = LocalDateTime.now();

        Order order = Order.builder()
                .buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(orderTime)
                .build();

        List<Product> products = productRepo.findAllByIdsWithXLock(dto.productId()); // For no update lock 베타락

        // product <-> 주문 개수 매핑
        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {
                    // 재고 차감
                    Product product = products.get(idx);
                    if (product.getStock() - dto.count().get(idx) < 0) {
                        throw new RuntimeException("재고가 부족합니다.");
                    }

                    product.decreaseStock(dto.count().get(idx));

                    return OrderProduct.builder()
                            .order(order)
                            .number(dto.count().get(idx))
                            .product(products.get(idx))
                            .build();
                }).toList();

//        HashMap<Product, Long> productCountMap = new HashMap<>();
//        for (int i = 0; i < dto.count().size(); i++) {
//            productCountMap.put(products.get(i), dto.count().get(i));
//        }
//
//        // 상품 개수 만큼 오더 생성
//        List<OrderProduct> orderProducts = products.stream()
//                .map(p -> OrderProduct.builder()
//                        .order(order)
//                        .number(productCountMap.get(p))
//                        .product(p)
//                        .build()
//                ).toList();
        // orderProduct 생명주기 order와 싱크 생성
        order.addOrderProducts(orderProducts);

        // save 전까지는 영속화를 하지 않음
        Order savedOrder = orderRepo.save(order);
        // save 이후 영속화

        // Entity -> Dto 변환
        OrderResponseDto orderResponseDto = OrderResponseDto.of(
                savedOrder.getOrderTime(),
                OrderStatus.COMPLETED,
                true);


        return orderResponseDto;
    }
}
