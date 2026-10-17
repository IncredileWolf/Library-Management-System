package com.library.management.service.impl;

import com.library.management.model.Author;
import com.library.management.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    public Author findAuthorInDb(String authorEmail){
        return authorRepository.findByEmail(authorEmail);
    }

    public Author saveMyAuthor(Author author){
        return authorRepository.save(author);
    }


}
