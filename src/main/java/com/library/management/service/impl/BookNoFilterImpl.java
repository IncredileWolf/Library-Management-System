package com.library.management.service.impl;

import com.library.management.dto.respone.BookFilterResponse;
import com.library.management.enums.Operator;
import com.library.management.model.Book;
import com.library.management.repository.BookRepository;
import com.library.management.service.BookFilterStratergy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookNoFilterImpl implements BookFilterStratergy {

    @Autowired
    private BookRepository bookRepository;

    @Override
    public List<Book> getFilteredBook(Operator operator, String value) {

        if(!operator.equals(Operator.EQUALS)){
            throw  new IllegalArgumentException("Only Equals is expected with book no");
        }
        List<Book> books = bookRepository.findByBookNo(value);
        return books;
    }

    private BookFilterResponse convertToBookFilterResponse(Book book) {
        return BookFilterResponse.
                builder().
                bookNo(book.getBookNo()).
                authorEmail(book.getAuthor().getEmail()).
                authorName(book.getAuthor().getName()).
                bookType(book.getBookType()).
                bookName(book.getTitle()).
                build();
    }



}
