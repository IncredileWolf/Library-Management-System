package com.library.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TxnReturnRequest {

    @NotBlank
    private String bookNo;

    @NotBlank
    private String txnId;

    @NotBlank
    private String userEmail;//we should not this from user


}
