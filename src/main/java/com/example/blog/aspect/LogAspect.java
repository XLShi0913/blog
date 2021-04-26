package com.example.blog.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/*
* 日志处理
* 所谓切面，实际上是指定的方法集合，这个方法集合可以由开发者通过参数定义来指定
* 其中的每个方法都是一个断点，之所以叫做断点，是因为获取这个方法时，可以在其前后进行操作，如获取其名称参数、返回值等
* 这种切面操作对于日志十分友好，因为可以在业务代码之外定义指定的方法集合，并通过断点将之记入日志
* */

@Aspect // 说明是切面操作
@Component // 开启组件扫描
public class LogAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 声明切面，规定横切的类的范围：(所有访问权限的类)web包下的所有类的所有方法
    @Pointcut("execution(* com.example.blog.web.*.*(..))")
    public void log() {
    }

    @Before("log()")
    public void doBefore(JoinPoint joinPoint) { // 方法的断点
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes(); // 获取请求
        HttpServletRequest request = attributes.getRequest();
        String url = request.getRequestURI();
        String ip = request.getRemoteAddr();
        String classMethod = joinPoint.getSignature().getDeclaringTypeName() + "."
                + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        RequestLog requestLog = new RequestLog(url, ip, classMethod, args);

        logger.info("Requesst : {}", requestLog);
    }

    @After("log()")
    public void doAfter() {
        logger.info("----------doAfter----------");
    }

    // 在返回之后拦截，捕获方法的返回值
    @AfterReturning(returning = "result",pointcut = "log()")
    public void doAfterRetuen(Object result) {
        logger.info("Result : {}", result);
    }

    // 对http请求数据的封装
    private class RequestLog {
        private String url;
        private String ip;
        private String classMethod;
        private Object[] args;

        public RequestLog(String url, String ip, String classMethod, Object[] args) {
            this.url = url;
            this.ip = ip;
            this.classMethod = classMethod;
            this.args = args;
        }

        @Override
        public String toString() {
            return "{" +
                    "url='" + url + '\'' +
                    ", ip='" + ip + '\'' +
                    ", classMethod='" + classMethod + '\'' +
                    ", args=" + Arrays.toString(args) +
                    '}';
        }
    }
}
