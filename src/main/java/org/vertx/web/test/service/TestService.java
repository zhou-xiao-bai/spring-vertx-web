package org.vertx.web.test.service;

import org.springframework.stereotype.Component;

@Component("service")
public class TestService {

    public void test1() {
        throw new RuntimeException("123");
        // System.out.println("≤‚ ‘“µŒÒ÷¥––");
    }
}
