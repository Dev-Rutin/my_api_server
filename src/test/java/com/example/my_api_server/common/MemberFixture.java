package com.example.my_api_server.common;


import com.example.my_api_server.entity.Member;

public class MemberFixture {

    // 이메일, 비밀번호(이메일은 고정된 값을 쓴다고 가정)
    // 정적 팩토리 메서드 패턴
    public static Member.MemberBuilder defaultMember() {
        return Member.builder()
                .email("test1@gmail.com");
    }
}
