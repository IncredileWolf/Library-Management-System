package com.library.management.dto.respone;

import com.library.management.enums.BookType;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookFilterResponse {

    private String bookNo;
    private String bookName;
    private BookType bookType;
    private String authorName;
    private String authorEmail;

}

