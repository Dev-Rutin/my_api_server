package com.example.my_api_server.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class MemberSignUpListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxRetries = 3) // 1. 알림을 실패했을 시 재시도 3번(하지만 상대 서버에 문제가 있다면 수리시간이 필요하기에 큰 의미는 없음)
    public void sendNotification(MemberSignUpEvent event) {

        log.info("member ID = {}", event.getId());
        log.info("member Email = {}", event.getEmail());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //2. 실패한 것을 db에 저장하고 특정 시간에 한번에 대량 알림을 발송 -> 트랜젝션 아웃박스 패턴(실무)
            throw new RuntimeException(e);
        }
        log.info("알림 전송 완료");
    }
}
