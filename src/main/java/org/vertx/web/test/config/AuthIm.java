package org.vertx.web.test.config;

import java.util.Set;

import org.vertx.web.annotations.Auth;

import rbac.framework.object.Permissions;
import rbac.framework.object.User;

@Auth
public class AuthIm implements rbac.framework.interfaces.Auth {

    /**
     * 鉴权
     */
    @Override
    public boolean authentication(Set<Permissions> pis, String[] roles) {
        // 从db里面查询当前角色

        // 拿到角色的pis

        // 与参数的pis进行比较
        return false;
    }

    /**
     * 授权
     */
    @Override
    public User authorization(String username, String password) {
        return null;
    }

}
