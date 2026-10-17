package com.library.management.repository;

import com.library.management.dto.respone.UserFilterResponse;
import com.library.management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findByName(String name);
    List<User> findByNameContains(String value);


    @Query(value = "select u from User u where name=:name", nativeQuery = false)
    List<User> retrieveUserViaName(String name);

    @Query(value = "select u from User u where name=:name", nativeQuery = true)
    List<User> retrieveUserViaNameNative(String name);

    User findByEmail(String email);

}
