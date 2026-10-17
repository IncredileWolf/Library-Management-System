package com.library.management.service.impl;

import com.library.management.dto.request.BookCreationRequest;
import com.library.management.dto.respone.BookCreationResponse;
import com.library.management.enums.BookFilter;
import com.library.management.enums.Operator;
import com.library.management.model.Author;
import com.library.management.model.Book;
import com.library.management.model.User;
import com.library.management.repository.AuthorRepository;
import com.library.management.repository.BookRepository;
import com.library.management.service.BookFilterStratergy;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorService authorService;
    @Autowired
    private BookFilterFactory bookFilterFactory;

    public BookCreationResponse addBook(BookCreationRequest request){

        Author authorFromDb = authorService.findAuthorInDb(request.getAuthorEmail());

        Book book = request.toBook();

        if(authorFromDb == null){
            authorFromDb = Author.builder()
                    .id(UUID.randomUUID().toString())
                    .name(request.getAuthorName())
                    .email(request.getAuthorEmail())
                    .build();
        }

        book.setAuthor(authorFromDb);
        book = bookRepository.save(book);
        return BookCreationResponse
                .builder()
                .bookName(book.getTitle())
                .bookNo(book.getBookNo())
                .securityAmount(book.getSecurityAmount())
                .build();

    }

    public List<Book> filter(BookFilter filterBy, Operator operator, String value) {
        BookFilterStratergy stratergy = bookFilterFactory.getStratergy(filterBy);
        return stratergy.getFilteredBook(operator, value);
    }

    public Book checkIfBookIsValid(String bookNo) {
        List<Book> books = bookRepository.findByBookNo(bookNo);
        if(books.isEmpty()){
            return null;
        }
        return books.get(0);
    }

    public void markBookUnavailable(Book bookFromDb, User userFromDb) {
        bookFromDb.setUser(userFromDb);
        bookRepository.save(bookFromDb);
    }
}
