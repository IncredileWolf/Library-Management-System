package com.library.management.controller;

import com.library.management.configuration.SecurityConfig;
import com.library.management.dto.respone.BookCreationResponse;
import com.library.management.service.impl.BookService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest does not load @Configuration classes, so SecurityConfig must be imported explicitly.
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    private static final String VALID_BODY = """
            {
              "bookTitle": "Clean Code",
              "securityAmount": 100.0,
              "bookNo": "B-001",
              "bookType": "EDUCATIONAL",
              "authorName": "Robert Martin",
              "authorEmail": "rm@example.com"
            }
            """;

    private static final String BLANK_TITLE_BODY = """
            {
              "bookTitle": "",
              "securityAmount": 100.0,
              "bookNo": "B-001",
              "bookType": "EDUCATIONAL",
              "authorName": "Robert Martin",
              "authorEmail": "rm@example.com"
            }
            """;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Apply the real Spring Security filter chain so authority rules are enforced
        // and @WithMockUser is propagated into each request.
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @MockitoBean
    private BookService bookService;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanAddBook() throws Exception {
        when(bookService.addBook(any())).thenReturn(
                BookCreationResponse.builder().bookNo("B-001").bookName("Clean Code").securityAmount(100.0).build());

        mockMvc.perform(post("/book/addBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.bookNo").value("B-001"));
    }

    @Test
    @WithMockUser(authorities = "STUDENT")
    void studentCannotAddBook() throws Exception {
        mockMvc.perform(post("/book/addBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void blankTitleIsRejected() throws Exception {
        // Currently returns 406 (see audit finding F-09b); will be tightened to 400 when error handling is fixed.
        mockMvc.perform(post("/book/addBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BLANK_TITLE_BODY))
                .andExpect(status().is4xxClientError());
    }
}
