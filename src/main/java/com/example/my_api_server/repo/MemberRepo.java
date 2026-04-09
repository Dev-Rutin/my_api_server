package com.example.my_api_server.repo;

import com.example.my_api_server.entity.Member;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// db통신 없이 간단한 버전, 인메모리 DB를 사용해서 CRUD
// DAO
@Component // Bean 등록
public class MemberRepo {
    Map<Long, Member> members = new HashMap<>();

    //save
    public Long saveMember(String email, String password) {
        Random random = new Random();
        long id = random.nextLong();
        Member member = Member.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();

        members.put(id, member);
        return id;
    }

    //find
    public Member findMember(Long id) {
        return members.get(id);
    }

}
