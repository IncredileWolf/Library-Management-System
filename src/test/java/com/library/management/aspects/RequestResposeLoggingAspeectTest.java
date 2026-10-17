package com.library.management.aspects;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RequestResponseLoggingAspectTest {

    private final RequestResponseLoggingAspect aspect = new RequestResponseLoggingAspect();
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        logger = (Logger) LoggerFactory.getLogger(RequestResponseLoggingAspect.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void cleanUp() {
        logger.detachAppender(appender);
        RequestContextHolder.resetRequestAttributes();
    }

    private String loggedText() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
    }

    @Test
    void requestLoggingNeverWritesCredentialsOrSessionCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/txn/issue");
        request.addHeader("Authorization", "Basic c2VjcmV0LWNyZWRlbnRpYWxz");
        request.addHeader("Cookie", "SESSION=very-secret-session-id");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        aspect.logRequest();

        String logged = loggedText();
        assertTrue(logged.contains("POST /txn/issue"), "method and path should still be logged");
        assertFalse(logged.contains("c2VjcmV0LWNyZWRlbnRpYWxz"), "Authorization header must not be logged");
        assertFalse(logged.contains("very-secret-session-id"), "Cookie header must not be logged");
    }

    @Test
    void requestLoggingDoesNotFailWithoutARequestContext() {
        RequestContextHolder.resetRequestAttributes();

        assertDoesNotThrow(aspect::logRequest);
    }

    @Test
    void responseLoggingNeverCallsToStringOnTheBody() {
        // Entities with bidirectional relationships make toString() recurse until StackOverflowError.
        Object body = new Object() {
            @Override
            public String toString() {
                throw new IllegalStateException("toString() must not be called on response bodies");
            }
        };

        assertDoesNotThrow(() -> aspect.logResponse(body));
    }

    @Test
    void responseLoggingHandlesNullAndResponseEntities() {
        assertDoesNotThrow(() -> aspect.logResponse(null));
        aspect.logResponse(new ResponseEntity<>("body", HttpStatus.CREATED));

        assertTrue(loggedText().contains("201"));
        assertFalse(loggedText().contains("body"));
    }
}
