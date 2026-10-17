package com.library.management.controller;

import com.library.management.configuration.SecurityConfig;
import com.library.management.service.impl.TxnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TxnController.class)
@Import(SecurityConfig.class)
class TxnControllerTest {

    private static final String ISSUE_BODY = """
            { "userEmail": "u@x.com", "bookNo": "B-001" }
            """;

    private static final String RETURN_BODY = """
            { "bookNo": "B-001", "txnId": "T-1", "userEmail": "u@x.com" }
            """;

    private static final String RETURN_BODY_BLANK_TXN_ID = """
            { "bookNo": "B-001", "txnId": "", "userEmail": "u@x.com" }
            """;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TxnService txnService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanIssueABook() throws Exception {
        when(txnService.create(any())).thenReturn("txn-123");

        mockMvc.perform(post("/txn/issue").contentType(MediaType.APPLICATION_JSON).content(ISSUE_BODY))
                .andExpect(status().isOk())
                .andExpect(content().string("txn-123"));
    }

    @Test
    @WithMockUser(authorities = "STUDENT")
    void studentCannotIssueABook() throws Exception {
        mockMvc.perform(post("/txn/issue").contentType(MediaType.APPLICATION_JSON).content(ISSUE_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(txnService);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanReturnABookAndGetTheSettlementAmount() throws Exception {
        when(txnService.returnTxn(any())).thenReturn(95.0);

        mockMvc.perform(post("/txn/return").contentType(MediaType.APPLICATION_JSON).content(RETURN_BODY))
                .andExpect(status().isOk())
                .andExpect(content().string("95.0"));
    }

    @Test
    @WithMockUser(authorities = "STUDENT")
    void studentCannotReturnABookOnBehalfOfAnyone() throws Exception {
        // Regression test for the route/security-rule mismatch: this endpoint used to be open to any logged-in user.
        mockMvc.perform(post("/txn/return").contentType(MediaType.APPLICATION_JSON).content(RETURN_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(txnService);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void returnRequestWithBlankFieldIsRejectedBeforeReachingTheService() throws Exception {
        // Status is 406 today (audit finding F-09b); it becomes 400 when error handling is cleaned up.
        mockMvc.perform(post("/txn/return").contentType(MediaType.APPLICATION_JSON).content(RETURN_BODY_BLANK_TXN_ID))
                .andExpect(status().is4xxClientError());
        verifyNoInteractions(txnService);
    }
}
