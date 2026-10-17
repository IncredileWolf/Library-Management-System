package com.library.management.dto.respone;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFilterResponse {

    private String userName;
    private String userEmail;
    private String userAddress;
    private String userPhone;

}
