package com.library.management.repository;


import com.library.management.model.Txn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TxnRepository extends JpaRepository<Txn,  Integer> {

    Txn findByTxnId(String txnId);

}
