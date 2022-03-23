package org.vertx.init.impl;

import org.vertx.init.InitInterface;
import org.vertx.init.vertx.AbstractVerticleImpl;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

/**
 * 初始化类
 * 
 * @author yangcong
 */
public class InitImpl implements InitInterface {

    @Override
    public void start(int port, String... configs) {
        Vertx.vertx().deployVerticle(new AbstractVerticleImpl(port, configs),
                new DeploymentOptions().setMaxWorkerExecuteTime(2000));
    }
}
