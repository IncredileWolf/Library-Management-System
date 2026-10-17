package com.library.management.service.impl;

import com.library.management.dto.request.TxnRequest;
import com.library.management.dto.request.TxnReturnRequest;
import com.library.management.enums.TxnStatus;
import com.library.management.exception.BookException;
import com.library.management.exception.TxnException;
import com.library.management.exception.UserException;
import com.library.management.model.Book;
import com.library.management.model.Txn;
import com.library.management.model.User;
import com.library.management.repository.TxnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TxnService {

    @Autowired
    private TxnRepository txnRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Value("${user.valid.days}")
    private int validUpto;

    @Value("${user.delayed.finePerDay}")
    private int finePerDays;

    @Transactional(rollbackFor = {BookException.class, UserException.class})
    public String create(TxnRequest txnRequest) throws UserException, BookException {

        User userFromDb = userService.checkIfUserIsValid(txnRequest.getUserEmail());
        if(userFromDb == null){
            throw new UserException("User is not valid");
        }

        Book bookFromDb = bookService.checkIfBookIsValid(txnRequest.getBookNo());
        if(bookFromDb == null){
            throw new BookException("Book is not valid");
        }

        if(bookFromDb.getUser() != null){
            throw new BookException("Book is not free to be issued");
        }
        return createTxn(userFromDb, bookFromDb);

    }

    @Transactional()
    public String createTxn(User userFromDb, Book bookFromDb) throws UserException, BookException {

        String txnId = UUID.randomUUID().toString();
        Txn txn = Txn
                .builder()
                .txnId(txnId)
                .user(userFromDb)
                .book(bookFromDb)
                .txnStatus(TxnStatus.ISSUED)
                .issuedDate(new Date())
                .build();
        txnRepository.save(txn);
        bookService.markBookUnavailable(bookFromDb, userFromDb);
        return txnId;
    }

    @Transactional(rollbackFor = {BookException.class, UserException.class})
    public Double returnTxn(TxnReturnRequest txnReturnRequest) throws UserException, BookException {

        User userFromDb = userService.checkIfUserIsValid(txnReturnRequest.getUserEmail());
        if(userFromDb == null){
            throw new UserException("User is not valid");
        }

        Book bookFromDb = bookService.checkIfBookIsValid(txnReturnRequest.getBookNo());
        if(bookFromDb == null){
            throw new BookException("Book is not valid");
        }

        if(bookFromDb.getUser() == null || !Objects.equals(bookFromDb.getUser().getId(), userFromDb.getId())){
            throw new TxnException("Book is assigned to someone else, or not at all assigned");
        }

        Txn txnFromDb = txnRepository.findByTxnId(txnReturnRequest.getTxnId());
        if(txnFromDb == null){
            throw new TxnException("No txn has been found in my db with this txnid");
        }
        if(txnFromDb.getTxnStatus() != TxnStatus.ISSUED){
            throw new TxnException("This transaction has already been closed");
        }
        if(!belongsTo(txnFromDb, userFromDb, bookFromDb)){
            throw new TxnException("This transaction does not belong to the given user and book");
        }

        int fine = calculateFine(txnFromDb);
        double settlement = settlementAfterFine(bookFromDb, fine);

        txnFromDb.setTxnStatus(fine > 0 ? TxnStatus.FINED : TxnStatus.RETURNED);
        txnFromDb.setSettlementAmount(settlement);
        txnFromDb.setSubmittedDate(new Date());
        bookFromDb.setUser(null);
        txnRepository.save(txnFromDb);
        return settlement;
    }

    public Double calculateSettlementAmount(Txn txnFromDb, Book bookFromDb) {
        return settlementAfterFine(bookFromDb, calculateFine(txnFromDb));
    }

    private boolean belongsTo(Txn txn, User user, Book book) {
        return txn.getUser() != null
                && txn.getBook() != null
                && Objects.equals(txn.getUser().getId(), user.getId())
                && Objects.equals(txn.getBook().getId(), book.getId());
    }

    private int calculateFine(Txn txn) {
        long elapsedMillis = System.currentTimeMillis() - txn.getIssuedDate().getTime();
        int daysPassed = (int) TimeUnit.MILLISECONDS.toDays(elapsedMillis);
        int lateDays = Math.max(0, daysPassed - validUpto);
        return lateDays * finePerDays;
    }

    private double settlementAfterFine(Book book, int fine) {
        // The fine can never take more than the security deposit, so the refund is never negative.
        return Math.max(0.0, book.getSecurityAmount() - fine);
    }
}
//
//@Service
//public class TxnService {
//
//    @Autowired
//    private TxnRepository txnRepository;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private BookService bookService;
//
//    @Value("${user.valid.days}")
//    private int validUpto;
//
//    @Value("${user.delayed.finePerDay}")
//    private int finePerDays;
//
//    @Transactional(rollbackFor = {BookException.class, UserException.class})
//    public String create(TxnRequest txnRequest) throws UserException, BookException {
//
//        User userFromDb = userService.checkIfUserIsValid(txnRequest.getUserEmail());
//        if(userFromDb == null){
//             throw new UserException("User is not valid");
//        }
//
//        Book bookFromDb = bookService.checkIfBookIsValid(txnRequest.getBookNo());
//        if(bookFromDb == null){
//            throw new BookException("Book is not valid");
//        }
//
//        if(bookFromDb.getUser() != null){
//            throw new BookException("Book is not free to be issued");
//        }
//        return createTxn(userFromDb, bookFromDb);
//
//    }
//
//    @Transactional()
//    public String createTxn(User userFromDb, Book bookFromDb) throws UserException, BookException {
//
//        String txnId = UUID.randomUUID().toString();
//        Txn txn = Txn
//                .builder()
//                .txnId(txnId)
//                .user(userFromDb)
//                .book(bookFromDb)
//                .txnStatus(TxnStatus.ISSUED)
//                .issuedDate(new Date())
//                .build();
//        txnRepository.save(txn);
//        bookService.markBookUnavailable(bookFromDb, userFromDb);
//        return txnId;
//    }
//
//    @Transactional(rollbackFor = {BookException.class, UserException.class})
//    public Double returnTxn(TxnReturnRequest txnReturnRequest) throws UserException, BookException {
//
//        User userFromDb = userService.checkIfUserIsValid(txnReturnRequest.getUserEmail());
//        if(userFromDb == null){
//            throw new UserException("User is not valid");
//        }
//
//        Book bookFromDb = bookService.checkIfBookIsValid(txnReturnRequest.getBookNo());
//        if(bookFromDb == null){
//            throw new BookException("Book is not valid");
//        }
//
//        if(bookFromDb.getUser() != null && bookFromDb.getUser().equals(userFromDb)){
//            Txn txnFromDb = txnRepository.findByTxnId(txnReturnRequest.getTxnId());
//
//            if(txnFromDb == null)
//                throw new TxnException(" No txn has been found in my db with this txnid ");
//
//            Double amount = calculateSettlementAmount(txnFromDb, bookFromDb);
//            if(amount == bookFromDb.getSecurityAmount())
//                txnFromDb.setTxnStatus(TxnStatus.RETURNED);
//            else
//                txnFromDb.setTxnStatus(TxnStatus.FINED);
//            txnFromDb.setSettlementAmount(amount);
//            bookFromDb.setUser(null);
//            txnRepository.save(txnFromDb);
//            return amount;
//        }else{
//            throw new TxnException("Book is assigned to someone else, or not at all assigned");
//        }
//
//
//
//    }
//
//    public Double calculateSettlementAmount(Txn txnFromDb, Book bookFromDb) {
//        long issueTime = txnFromDb.getIssuedDate().getTime();
//        long returnTime = System.currentTimeMillis();
//        long diff = returnTime - issueTime;
//        int daysPassed = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
//        if(daysPassed >= validUpto){
//            int fineAmount = (daysPassed - validUpto)* finePerDays;
//            return bookFromDb.getSecurityAmount()-fineAmount;
//        }
//        return bookFromDb.getSecurityAmount();
//    }
//}
