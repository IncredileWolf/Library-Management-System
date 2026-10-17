package com.library.management.controller;


import com.library.management.dto.request.UserCreationRequest;
import com.library.management.dto.respone.UserCreationResponse;
import com.library.management.enums.Operator;
import com.library.management.enums.UserFilter;
import com.library.management.model.User;
import com.library.management.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
//@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/addStudent")
    public UserCreationResponse addStudent(@RequestBody @Validated UserCreationRequest request) {
        return userService.addStudent(request);
    }

    @PostMapping("/addAdmin")
    public UserCreationResponse addAdmin(@RequestBody @Validated UserCreationRequest request) {

        return userService.addAdmin(request);

    }

    @GetMapping("/filter")
    public List<User> filteredByUser(@RequestParam("filterBy") UserFilter filterBy,
                                                    @RequestParam("operator") Operator operator,
                                                    @RequestParam("value") String value
                                                    ) {
        return userService.filter(filterBy, operator, value);
    }

}

