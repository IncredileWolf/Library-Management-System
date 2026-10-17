package com.library.management.service.impl;


import com.library.management.enums.Operator;
import com.library.management.model.Book;
import com.library.management.service.BookFilterStratergy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookTypeFilterImpl implements BookFilterStratergy {
    @Override
    public List<Book> getFilteredBook(Operator operator, String value) {
        return List.of();
    }
}
