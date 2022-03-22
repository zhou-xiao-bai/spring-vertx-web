package org.vertx.init;

/**
 * 初始化接口
 * 
 * @author yangcong
 */
public interface InitInterface {

    /**
     * 启动方法
     */
    public void start(int port, String... configs);
}
