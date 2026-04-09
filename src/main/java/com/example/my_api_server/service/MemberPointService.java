package com.example.my_api_server.service;

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.repo.MemberDBRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberPointService {

    private final MemberDBRepo memberDBRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public void changeAllUserData() {
        List<Member> members = memberDBRepo.findAll();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void supportTxText() {

        //DB를 사용하지 안는 단순 자바 코드, readonly = true 와 같은 주로 최적확된 읽기를 사용할 때 가끔 사용
        List<Member> members = memberDBRepo.findAll();
    }

    @Transactional(timeout = 2)
    public void timeOut() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        memberDBRepo.findAll();
    }
}
