package org.vertx.web.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.rpc.client.config.tcp.Tcp;
import org.springframework.context.support.AbstractApplicationContext;
import org.vertx.web.annotations.rpc.RpcConnection;
import org.vertx.web.annotations.rpc.RpcServersConnection;
import org.vertx.web.annotations.rpc.Server;

/**
 * @author yangcong
 * 
 *         Rpc配置类
 */
public class RpcConfig {

    /**
     * spring上下文
     */
    private AbstractApplicationContext abstractApplicationContext;

    /**
     * 当前服务端的tcp
     */
    private org.rpc.service.config.tcp.Tcp serverTcp;

    /**
     * 连接的服务 k服务名称, v对应服务的tcp
     */
    private static Map<String, org.rpc.client.config.tcp.Tcp> clientTcps = new HashMap<>();

    /**
     * 
     * @param abstractApplicationContext
     */
    public RpcConfig(AbstractApplicationContext abstractApplicationContext) {
        this.abstractApplicationContext = abstractApplicationContext;
        // 启动server
        this.serverStart();
        // 连接远程服务
        this.connectionRemoteServer();

    }

    /**
     * 启动当前系统服务
     */
    private void serverStart() {
        Map<String, Object> beans = this.abstractApplicationContext.getBeansWithAnnotation(Server.class);
        if (beans.size() != 1) {
            throw new RuntimeException("当前服务地址存在多个,请检查配置项!!");
        }
        // 拿到server
        Set<Map.Entry<String, Object>> servers = beans.entrySet();
        Server server = null;
        for (Map.Entry<String, Object> entry : servers) {// 拿出对应的注解
            server = entry.getValue().getClass().getAnnotation(Server.class);
            break;
        }
        // 启动当前服务
        this.serverTcp = new org.rpc.service.config.tcp.Tcp(server.host(), server.port(),
                this.abstractApplicationContext);
    }

    /**
     * 连接远程服务
     */
    private void connectionRemoteServer() {
        Map<String, Object> beans = this.abstractApplicationContext.getBeansWithAnnotation(RpcServersConnection.class);
        if (beans.size() != 1) {
            throw new RuntimeException("当前服务地址存在多个,请检查配置项!!");
        }
        // 拿到RemoteServer
        Set<Map.Entry<String, Object>> servers = beans.entrySet();
        RpcServersConnection rpcServersConnection = null;
        RpcConnection[] rpcConnections = null;
        // 遍历服务
        for (Map.Entry<String, Object> entry : servers) {
            rpcServersConnection = entry.getValue().getClass().getAnnotation(RpcServersConnection.class);
            rpcConnections = rpcServersConnection.rpcConnections();
            for (int i = 0; i < rpcConnections.length; i++) {
                clientTcps.put(rpcConnections[i].serviceName(),
                        new org.rpc.client.config.tcp.Tcp(rpcConnections[i].host(), rpcConnections[i].port()));
            }
        }
    }

    /**
     * 获取对应服务的rpc
     * 
     * @param serviceName
     * @return
     */
    public static org.rpc.client.config.tcp.Tcp getService(String serviceName) {
        return RpcConfig.clientTcps.get(serviceName);
    }
}
