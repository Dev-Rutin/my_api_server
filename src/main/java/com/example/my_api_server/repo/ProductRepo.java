package com.example.my_api_server.repo;

import com.example.my_api_server.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    //FOR NO KEY UPDATA 레코드 락(PG), FOR UPDATE(Mysql)
    // 동일한 레코드에 대해서 동시 업데이트를 방지
    // 트랜잭션이 동일한 로우에 대해서 최신 스냅샷을 읽기 때문에 동시성 이슈가 없어진다.(정합성 보장)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id")
    // JPQL 자바 객체로 쿼리를 구성하는 방법
    List<Product> findAllByIdsWithXLock(List<Long> ids);
}
