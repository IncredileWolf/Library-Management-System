package com.library.management.dto.request;

import com.library.management.enums.UserStatus;
import com.library.management.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserCreationRequest {

    private String userName;

    @NotBlank(message = "user email must not be blank ")
    private String userEmail;

    private String userAddress;

    private String userPhone;
    private String password;

    public User toUser(){
        return User
                .builder()
                .name(this.userName)
                .email(this.userEmail)
                .phoneNo(this.userPhone)
                .address(this.userAddress)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }


}
