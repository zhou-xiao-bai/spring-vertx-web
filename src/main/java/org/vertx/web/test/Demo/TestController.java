package org.vertx.web.test.Demo;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vertx.web.annotations.Controller;
import org.vertx.web.annotations.RequestMappping;
import org.vertx.web.method.Method;
import org.vertx.web.test.entity.Pojo;
import org.vertx.web.test.service.TestService;

import io.vertx.core.http.HttpServerRequest;

@Controller
public class TestController {

    @Autowired
    @Qualifier("service")
    private TestService testService;

    @RequestMappping(version = "v1", type = Method.GET)
    public Object result(Pojo pojo, HttpServerRequest request) {
        System.out.println("Age is " + pojo.getAge());
        System.out.println("Name is " + pojo.getName());
        testService.test1();
        return new HashMap<String, Object>() {
            {
                put("code", 200);
                put("message", "ok");
            }
        };
    }
}
