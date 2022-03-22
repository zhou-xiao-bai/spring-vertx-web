package org.vertx.web.test.inter;

import org.vertx.web.annotations.Interceptor;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

@Interceptor(url = "/TestController/v1", regex = "/TestController/.*")
public class TestInter implements org.vertx.web.middleware.url.handler.Interceptor {

    @Override
    public boolean intercept(HttpServerRequest request, HttpServerResponse response) {
        return true;
    }

}
