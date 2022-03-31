package org.vertx.init.vertx;

import org.vertx.web.config.WebConfig;

import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.core.AbstractVerticle;

public class AbstractVerticleImpl extends AbstractVerticle {

    /**
     * 配置文件组
     */
    private String[] configs;

    /**
     * 默认端口80
     */
    private int port = 80;

    public AbstractVerticleImpl(int port, String... configs) {
        this.port = port;
        this.configs = configs;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // 将初始化操作放到vertx线程池做异步启动
        vertx.executeBlocking(future -> {
            WebConfig webConfig = new WebConfig(super.vertx, this.configs);
            future.complete(webConfig.getRouter());
        }, res -> {
            HttpServer server = super.vertx.createHttpServer();
            server.requestHandler(((Router) res.result())).listen(this.port).onSuccess(handler -> {
                System.out.println("Server is start");
            });
        });
    }
}
