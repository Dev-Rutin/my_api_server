package com.example.my_api_server.service;

import com.example.my_api_server.entity.*;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {
    @Mock // 가짜 객체 생성
    ProductRepo productRepo;

    @Mock // 가짜 객체 생성
    MemberDBRepo memberDBRepo;

    @Mock // 가짜 객체 생성
    OrderRepo orderRepo;

    @InjectMocks // 실제로 테스트 할 대상 클래스(Mock 객체를 자동으로 주입 받음)
    OrderService orderService;

    InitData initData; // 초기 데이터 클래스 객체

    OrderCreateDto orderCreateDto;

    @BeforeEach
    public void init() {
        initData = new InitData();
        initData.memberId = 1L;
        initData.productIds = List.of(1L, 2L);
        initData.counts = List.of(1L, 2L);

        initData.product1 = Product.builder()
                .productNumber("TEST1")
                .productName("티셔츠 1")
                .productType(ProductType.CLOTHES)
                .stock(1L)
                .price(1000L)
                .build();

        initData.product2 = Product.builder()
                .productNumber("TEST2")
                .productName("티셔츠 2")
                .productType(ProductType.CLOTHES)
                .stock(2L)
                .price(2000L)
                .build();

        initData.member = Member.builder()
                .email("test1@gmail.com")
                .password("1234")
                .build();

        orderCreateDto = new OrderCreateDto(
                initData.memberId,
                initData.productIds,
                initData.counts);
    }

    @Test
    @DisplayName("test1")
    public void test1() {
        // given (when에 필요한 데이터 생성)
        int a = 10;


        // when (실제 수행 매서드)
        a++;


        // then (테스트 결과 확인)
        assertThat(a).isEqualTo(11);
    }

    @Test
    @DisplayName("[HAPPY]주문 요청이 정장적으로 잘 등록된다.")
    public void createOrderSuccess() {
        //given


        // DB 대신 프록시 데이터 넣기
        when(productRepo.findAllById(initData.productIds)).thenReturn(List.of(initData.product1, initData.product2));
        when(memberDBRepo.findById(initData.memberId)).thenReturn(Optional.of(initData.member));


        when(orderRepo.save(any())).thenAnswer(invocation ->
                invocation.getArgument(0));

        //when
        OrderResponseDto dto = orderService.createOrder(orderCreateDto, LocalDateTime.now());

        //then
        ArgumentCaptor<Order> capture = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo).save(capture.capture());

        assertThat(dto.isSuccess()).isTrue(); // 성공 여부 검증
        assertThat(dto.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED); // 주문 상태 검증
    }
    // tc1

    @Test
    @DisplayName("[Exception] 주문 요청 시 재고 부족하면 예외 처리가 정상 동작한다.")
    public void ProductStockValid() {
        //given
        Long memberId = 1L;
        List<Long> productIds = List.of(1L, 2L);
        List<Long> counts = List.of(10L, 20L);

        Product product1 = Product.builder()
                .productNumber("TEST1")
                .productName("티셔츠 1")
                .productType(ProductType.CLOTHES)
                .stock(1L)
                .price(1000L)
                .build();

        Product product2 = Product.builder()
                .productNumber("TEST2")
                .productName("티셔츠 2")
                .productType(ProductType.CLOTHES)
                .stock(2L)
                .price(2000L)
                .build();

        Member member = Member.builder()
                .email("test1@gmail.com")
                .password("1234")
                .build();

        OrderCreateDto createDto = new OrderCreateDto(
                memberId,
                productIds,
                counts);

        when(productRepo.findAllById(productIds)).thenReturn(List.of(product1, product2));
        when(memberDBRepo.findById(memberId)).thenReturn(Optional.of(member));
        //when


        //then
        assertThatThrownBy(() -> orderService.createOrder(createDto, LocalDateTime.now()))
                .isInstanceOf(RuntimeException.class) // 예외 클래스를 지정
                .hasMessage("재고가 부족합니다."); // 예외 메시지를 지정
    }

    @DisplayName("[Exception] 주문 시간 날짜 오류 테스트")
    public void orderTimeException() {
        //given


        when(productRepo.findAllById(initData.productIds)).thenReturn(List.of(initData.product1, initData.product2));
        when(memberDBRepo.findById(initData.memberId)).thenReturn(Optional.of(initData.member));

        //when
        // 테스트가 시간 여부에 따라 달라짐
        OrderResponseDto dto = orderService.createOrder(orderCreateDto, LocalDateTime.now());

        //then
        assertThat(dto).isNotNull();
    }
    //@Test

    public class InitData {
        Long memberId;

        List<Long> productIds;

        List<Long> counts;

        Product product1;

        Product product2;

        Member member;
    }

}