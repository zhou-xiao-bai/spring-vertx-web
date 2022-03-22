package org.vertx.web.middleware.url.router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.vertx.web.annotations.Interceptor;
import org.vertx.web.annotations.Param;
import org.vertx.web.config.ControllerEntity;
import org.vertx.web.config.WebConfig;
import org.vertx.web.middleware.url.handler.method.RequestMethodHandler;
import org.vertx.web.middleware.url.handler.path.PathInterceptor;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import rbac.framework.Security;
import rbac.framework.annotations.Permissions;
import rbac.framework.exceptions.UserLoginException;
import rbac.framework.object.User;

/**
 * @author yangcong
 * 
 *         URL路由器
 */
public class RouterUrl {

    private Vertx vertx;

    private Router router;

    private WebConfig webConfig;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public RouterUrl(Vertx vertx, WebConfig webConfig) {
        this.vertx = vertx;
        this.webConfig = webConfig;
        this.router = Router.router(vertx);
    }

    public void pathConfig() {
        this.router.routeWithRegex("/.*").handler(BodyHandler.create()).handler(routingContext -> {
            routingContext.response().setChunked(true);// 必须设置
            // 进行拦截处理
            // step1 初始化拦截器
            if (!new PathInterceptor(webConfig).intercept(routingContext.request(), routingContext.response())) {// 找不到路径
                routingContext.response().write("404 NOT FIND");
                routingContext.next();
                return;
            }

            // step2 请求方式拦截
            if (!new RequestMethodHandler(webConfig).intercept(routingContext.request(), routingContext.response())) {// 请求方式不匹配
                routingContext.response().write("method is not " + routingContext.request().method().toString());
                routingContext.next();
                return;
            }

            // step3 自定义拦截器
            // 拿到所有的自定义拦截器
            Map<String, Object> inters = this.webConfig.getSpringApplication()
                    .getBeansWithAnnotation(Interceptor.class);
            Collection<Object> collections = inters.values();

            // 遍历执行所有拦截器
            for (Object object : collections) {
                if (object instanceof org.vertx.web.middleware.url.handler.Interceptor) {
                    org.vertx.web.middleware.url.handler.Interceptor interceptor = (org.vertx.web.middleware.url.handler.Interceptor) object;
                    if ((routingContext.request().path()
                            .equals(object.getClass().getAnnotation(Interceptor.class).url()) // 路径绝对匹配
                            ||
                            routingContext.request().path()
                                    .matches(object.getClass().getAnnotation(Interceptor.class).regex()) // 正则匹配
                    )
                            && !interceptor.intercept(routingContext.request(), routingContext.response()) // 拦截器逻辑正确
                    ) {
                        routingContext.next();
                        return;
                    }
                }
            }

            // step 4 拿到要执行业务的Controller
            ControllerEntity entity = this.webConfig.getUrl().get(routingContext.request().path());

            // step 5 对参数进行检验
            Method method = entity.getMethod();

            if (method.isAnnotationPresent(Permissions.class)) {
                Permissions permissions = method.getAnnotation(Permissions.class);
                // 拿到当前接口需要的角色
                String[] roles = permissions.roles();
                // 拿到头部的header
                String token = routingContext.request().headers().get("token");
                // 通过token拿到用户
                User user = Security.getUser(token);
                // 用户没登陆
                if (Objects.isNull(user)) {
                    throw new UserLoginException();
                }

                // 获取当前用户角色权限标识符
                Set<rbac.framework.object.Permissions> pis = user.getRole().getPermissions();
                // 进行鉴权(具体实现在Auth实现类里面做)@param pis当前用户权限标识符 @param roles当前接口所需角色
                if (!Security.auth().authentication(pis, roles)) {// 无权限抛出异常
                    throw new RuntimeException("No auth");
                }
            }

            // 打开参数注入
            method.setAccessible(true);

            // 获取当前方法的所有参数
            List<Object> params = new ArrayList<>();

            // 拿到json数据
            String jsonObject = routingContext.getBodyAsString("UTF-8");

            // 如果数据为空,或者不是json格式,重置新格式
            if (Objects.isNull(jsonObject) || jsonObject.length() == 0
                    || (jsonObject.startsWith("{") && jsonObject.endsWith("}"))) {
                jsonObject = "{}";
            }
            Parameter[] parameters = method.getParameters();

            try {
                // 对参数进行注入
                for (Parameter parameter : parameters) {
                    // 如果是组件包内的
                    if (parameter.getType().equals(HttpServerRequest.class)) {
                        params.add(routingContext.request());
                    } else if (parameter.getType().equals(HttpServerResponse.class)) {
                        params.add(routingContext.response());
                    } else {// 非组件包内的(也就是开发者自身定义的参数)
                            // 如果有param注解(说明要单独取值)
                        if (parameter.isAnnotationPresent(Param.class)) {
                            // 拿到json转换后的对象类型
                            Object val = new ObjectMapper().readValue(jsonObject, parameter.getClass());

                            // 如果类型与参数列表的类型不匹配,抛出异常
                            if (!(val.getClass() == parameter.getType())) {
                                // 此处需要进行日志记录了
                                // ....................

                                // 抛出不匹配异常
                                throw new RuntimeException("Method name " + method.getName() + " ==> param '" +
                                        parameter.getAnnotation(Param.class).name() +
                                        "' Type cast " + parameter.getType().getSimpleName() + " Error");
                            }
                            // json字符串当中筛选出对应key值
                            params.add(
                                    OBJECT_MAPPER.readValue(jsonObject, Map.class)
                                            .get(parameter.getAnnotation(Param.class).name()));
                            continue;
                        }
                        // 不加注解默认拿全部
                        params.add(OBJECT_MAPPER.readValue(jsonObject, parameter.getType()));
                    }
                }
                Object result = method.invoke(entity.getObject(), params.toArray());
                routingContext.response().write(OBJECT_MAPPER.writeValueAsString(result),
                        "GBK");
            } catch (Throwable throwable) {
                InvocationTargetException targetException = (InvocationTargetException) throwable;
                // 拿到目标异常
                Throwable targeThrowable = targetException.getTargetException();

                // 出现异常信息,反馈给前端
                try {
                    routingContext.response().write("{\"message\" : \"" + targeThrowable.getMessage() + "\"}",
                            "GBK");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                routingContext.next();
                return;
            }
            routingContext.next();
        }).failureHandler(ctx -> {// 捕获异常信息
            ctx.response().write(ctx.failure().getMessage().replace("Response head already sent", ""));
            ctx.end();
        });
    }

    public Router getRouter() {
        return router;
    }

}
