package com.library.management.aspects;


import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RequestResponseLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingAspect.class);

    @Pointcut("execution(* com.library.management.controller..*(..))")
    public void controllerMethods(){}

    @Before("controllerMethods()")
    public void logRequest(){
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if(!(attributes instanceof ServletRequestAttributes servletAttributes)){
            return;
        }
        HttpServletRequest request = servletAttributes.getRequest();
        // Headers are deliberately NOT logged: they carry Authorization credentials and session cookies.
        log.info("Request : {} {}", request.getMethod(), request.getRequestURI());
    }

    @AfterReturning(value = "controllerMethods()", returning = "response")
    public void logResponse(Object response){
        if(response instanceof ResponseEntity<?> entity){
            log.info("Response status : {}", entity.getStatusCode());
        }else{
            // The body is deliberately NOT printed: entities can contain cyclic relationships
            // (toString would recurse forever) and responses can contain personal data.
            log.info("Response type : {}", response == null ? "none" : response.getClass().getSimpleName());
        }
    }

}
