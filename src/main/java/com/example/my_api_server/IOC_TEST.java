package com.example.my_api_server;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test") // api address
@RequiredArgsConstructor // 생성자 주입 방식을 어노테이션으로 사용(DI)
public class IOC_TEST {

    //1. 핃드 주입(사용 빈도 매우 낮음)
    @Autowired
    private IOC ioc2;

    //2. Setter(수정자) 주입(사용 빈도 매우 낮음)
    public IOC setIoc(IOC ioc)
    {
        ioc2 = ioc;
        return ioc;
    }

    //3. 생성자 주입(주로 사용)
    public void IOC(IOC ioc) {
        ioc2 = ioc;
    }

    //final : 불변성 객체의 변경이 불가능
    private final IOC ioc;

    @GetMapping
    public void iocTest(){
        ioc.func1();
    }

}
