package com.library.management.service;


import com.library.management.enums.Operator;
import com.library.management.model.Book;

import java.util.List;

public interface BookFilterStratergy {

    List<Book> getFilteredBook(Operator operator, String value);

}
