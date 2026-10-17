package com.library.management.controller;


import com.library.management.dto.GenericReturnClass;
import com.library.management.dto.request.BookCreationRequest;
import com.library.management.dto.respone.BookCreationResponse;
import com.library.management.enums.BookFilter;
import com.library.management.enums.Operator;
import com.library.management.model.Book;
import com.library.management.service.impl.BookService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;




    @PostMapping("/addBook")
    public ResponseEntity<GenericReturnClass> addBook(@RequestBody @Validated BookCreationRequest request){
        BookCreationResponse response = bookService.addBook(request);
        GenericReturnClass returnObject = GenericReturnClass.builder().data(response).build();
        if(response != null){
            returnObject.setCode(0);
            returnObject.setMsg("Its successful");
        }else{
            returnObject.setCode(1);
            returnObject.setMsg("Its Failed");
        }
        return new ResponseEntity<>(returnObject, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public List<Book> filteredUser(@NotNull(message = "filterby must not be null") @RequestParam("filterBy") BookFilter filterBy,
                                   @NotNull(message = "operator must not be null")  @RequestParam("operator") Operator operator,
                                   @NotBlank(message = "value must not be blank")  @RequestParam("value") String value
    ){
        return bookService.filter(filterBy, operator, value);
    }



}
