package com.library.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TxnRequest {

    @NotBlank(message = "user email must not be blank")
    private String userEmail;

    @NotBlank(message = "bookNo must not be blank")
    private String bookNo;

}
