package com.library.management.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericReturnClass {

    private Object data;
    private String error;
    private int code;
    private String msg;

}
