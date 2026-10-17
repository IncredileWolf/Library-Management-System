package com.library.management.dto.respone;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookCreationResponse {

    private String bookNo;
    private String bookName;
    private Double securityAmount;

}
