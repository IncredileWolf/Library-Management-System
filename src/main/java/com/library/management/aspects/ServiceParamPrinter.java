package com.library.management.aspects;

import com.library.management.dto.request.TxnRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceParamPrinter {

    private static final Logger log = LoggerFactory.getLogger(ServiceParamPrinter.class);

    @Pointcut("execution(* com.library.management.service.impl.TxnService.create(com.library.management.dto.request.TxnRequest))")
    public void txnServiceCreateMethod(){}

    @Before(value = "txnServiceCreateMethod() && args(txnRequest)")
    public void txnServiceAdvice(TxnRequest txnRequest){
        log.info("txnRequest is :{}", txnRequest);
    }

}
