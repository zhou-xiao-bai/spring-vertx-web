package org.vertx.web.test;

import org.vertx.start.VertxApplication;
import org.vertx.web.annotations.rpc.RpcConnection;
import org.vertx.web.annotations.rpc.RpcServersConnection;
import org.vertx.web.annotations.rpc.Server;

/**
 * @author yangcong
 * 
 *         使用demo
 */
// 本地服务
@Server(serverName = "testServer", host = "localhost", port = 8888)
// 远程服务调用配置
@RpcServersConnection(rpcConnections = { @RpcConnection(serviceName = "hello", host = "localhost", port = 8081) })
public class Test {

    public static void main(String[] args) {
        // 启动对于http调用的服务
        VertxApplication.start(80, "spring.xml");
    }
}
