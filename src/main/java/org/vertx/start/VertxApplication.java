package org.vertx.start;

import org.vertx.init.impl.InitImpl;

/**
 * 启动类
 */
public class VertxApplication {

    /**
     * 
     * @param configs
     */
    public static void start(int port, String... configs) {

        // 启动mq

        // 启动服务器
        new InitImpl().start(port, configs);
    }
}
