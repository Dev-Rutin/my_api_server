package com.example.my_api_server.service;

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.event.MemberSignUpEvent;
import com.example.my_api_server.repo.MemberDBRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class MemberDBService {

    private final MemberDBRepo memberDBRepo;
    private final MemberPointService memberPointService;
    private final ApplicationEventPublisher publisher; // 이벤트를 보내줄 publisher

    //DB에 저장
    /*
        1. @Transactional은 AOP로 돌아가서 begin tran() commit() 을 자동으로 실행
        2. DB에는 commit 명령어가 실행돼야 테이블에 반영됨 (redo, undo log)
        3. JPA의 구현체인 하이버네이트와 엔티티매니저 JDBC Driver <-> DB 통신 과정을 Spring이 자동으로 실행
    */

    @Transactional// 기본적으로 런타임만 롤백하지만, 특정 클래스를 예외로 롤백처리 한다.
    public Long signUp(String email, String password) throws IOException {
        Member member = Member.builder()
                .email(email)
                .password(password)
                .build();

        Member savedMember = memberDBRepo.save(member);

        // 변경 지점이 적어지고 유지보수성이 올라감, 강결합 해결
        publisher.publishEvent(new MemberSignUpEvent(savedMember.getId(), savedMember.getEmail()));// 이벤트 발송

        //sendNotification();

        //memberPointService.changeAllUserData();

        //throw new IOException("외부 API 호출도중 I/O 예외 터짐");
        // I/O 문제는 상대 측 문제이기 때문에 상대 측 서버의 헬스 체크가 된다면, 재전송 하는 로직을 구성해야 함
        //throw new RuntimeException("DB 저장 중 뭔가 오류가 터짐");

        return savedMember.getId();
    }

    public void sendNotification() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("알림 전송 완료");
    }

    //requires_new = 새로운 트랜젝션을 만든다.
    //다만, AOP에서 트랜젝션은 클래스 당 1개를 생성한다. 그렇기에 이 코드는 제대로 동작하지 않음
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeAllUserData() {
        List<Member> members = memberDBRepo.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRED, timeout = 2)
    public void tx1() {
        List<Member> members = memberDBRepo.findAll();
        members.stream()
                .forEach((m) -> {
                    log.info("member id = {}", m.getId());
                    log.info("member email = {}", m.getEmail());
                });

        memberPointService.changeAllUserData();

        memberPointService.timeOut();
    }
}
