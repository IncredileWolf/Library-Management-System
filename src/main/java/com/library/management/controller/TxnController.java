package com.library.management.controller;

import com.library.management.dto.request.TxnRequest;
import com.library.management.dto.request.TxnReturnRequest;
import com.library.management.exception.BookException;
import com.library.management.exception.UserException;
import com.library.management.model.Txn;
import com.library.management.service.impl.TxnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/txn")
public class TxnController {

    @Autowired
    private TxnService txnService;

    @PostMapping("/issue")
    public ResponseEntity<String> create(@RequestBody @Validated TxnRequest txnRequest) throws UserException, BookException {
        String txnId = txnService.create(txnRequest);
        if (txnId != null && !txnId.isEmpty()) {
            return new ResponseEntity<>(txnId, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/return")
    public Double returnTxn(@RequestBody @Validated TxnReturnRequest txnReturnRequest) throws UserException, BookException {
        return txnService.returnTxn(txnReturnRequest);
    }
}
