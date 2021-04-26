package com.example.blog.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

// 异常处理控制
@ControllerAdvice // 拦截带有Controller注解的控制器
public class ControllerExceptionHandler {

    private Logger  logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(Exception.class) // 拦截Exception级别的异常
    public ModelAndView exceptionHander(HttpServletRequest request, Exception e) throws Exception {
        logger.error("Requst URL : {}, Exception : {}", request.getRequestURI(), e); // logger的用法

        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
            throw e; // 若存在指定状态码的异常，则将之直接抛出
        }

        ModelAndView mv = new ModelAndView();
        mv.addObject("url", request.getRequestURI());
        mv.addObject("exception", e);
        mv.setViewName("error/error");
        return mv;
    }
}
