package com.example.my_api_server.service;

import com.example.my_api_server.common.MemberFixture;
import com.example.my_api_server.common.ProductFixture;
import com.example.my_api_server.config.TestContainerConfig;
import com.example.my_api_server.entity.Member;
import com.example.my_api_server.entity.Product;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderProductRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest // spring DI를 통해 모든 빈을 주입
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private MemberDBRepo memberDBRepo;

    @Autowired
    private OrderProductRepo orderProductRepo;
    private LocalDateTime orderTime;

    private List<Long> getProductIds(List<Product> products) {
        return products.stream()
                .map(Product::getId)
                .toList();
    }

    @BeforeEach
    public void setUp() {
        orderProductRepo.deleteAllInBatch();
        productRepo.deleteAllInBatch();
        orderRepo.deleteAllInBatch();
        memberDBRepo.deleteAllInBatch();
    }

    private Member getSavedMember(String password) {
        return memberDBRepo.save(MemberFixture.defaultMember()
                .password(password)
                .build());
    }

    private List<Product> getProducts() {
        return productRepo.saveAll(ProductFixture.defaultProducts());
    }

    @Nested
    @DisplayName("주문 생성 TC")
    class OrderCreateTest {


        @Test
        @DisplayName("주문 생성 시 DB에 저장되고 주문 시간이 NULL이 아니다.")
        public void createOrderPersistAndReturn() {
            //given
            List<Long> counts = List.of(1L, 1L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);

            OrderCreateDto createDto = new OrderCreateDto(
                    savedMember.getId(),
                    productIds,
                    counts);

            orderTime = LocalDateTime.now();

            //when
            OrderResponseDto retDto = orderService.createOrder(createDto, orderTime);

            //then
            assertThat(retDto.getOrderCompletedTime()).isNotNull();
        }

        @Test
        @DisplayName("주문 생성 시 재고가 정상적으로 차감이 된다.")
        public void createOrderStockDecreaseSuccess() {
            //given
            List<Long> counts = List.of(1L, 1L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);

            OrderCreateDto createDto = new OrderCreateDto(
                    savedMember.getId(),
                    productIds,
                    counts);

            orderTime = LocalDateTime.now();

            //when
            OrderResponseDto retDto = orderService.createOrder(createDto, orderTime);

            //then
            List<Product> resultProducts = productRepo.findAllById(productIds);

            // 현재 재고 - 주문 재고 = 최신 재고
            for (int i = 0; i < products.size(); i++) {
                Product beforeProduct = products.get(i); // 이전 상품 정보
                Product nowProduct = resultProducts.get(i); // 최신 상품 정보
                Long orderStock = counts.get(i); // 주문 재고
                assertThat(nowProduct.getStock()).isEqualTo(beforeProduct.getStock() - orderStock);
            }
        }

        @Test
        @DisplayName("주문 생성 시 재고가 부족할 때 예외가 정상 동작한다.")
        public void createOrderStockValidation() {
            //given
            List<Long> counts = List.of(10L, 10L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);

            OrderCreateDto createDto = new OrderCreateDto(
                    savedMember.getId(),
                    productIds,
                    counts);

            orderTime = LocalDateTime.now();

            //when

            //then
            assertThatThrownBy(() -> orderService.createOrder(createDto, orderTime))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("재고가 부족합니다.");
        }
    }

    @Nested
    @DisplayName("주문과 연관된 도메인 예외 TC")
    class OrderRelatedExceptionTest {
        @Test
        @DisplayName("주문 시 회원이 존재하지 않으면 예외가 발생한다.")
        public void validateMemberWhenCreateOrder() {
            //given
            List<Long> counts = List.of(1L, 1L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);

            OrderCreateDto createDto = new OrderCreateDto(
                    1234123L,
                    productIds,
                    counts);

            orderTime = LocalDateTime.now();

            //when

            //then
            assertThatThrownBy(() -> orderService.createOrder(createDto, orderTime))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("회원 정보가 없습니다.");

        }
    }

}
