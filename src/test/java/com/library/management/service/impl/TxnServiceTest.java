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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TxnServiceTest {

    private static final long ONE_DAY_MS = 24L * 60 * 60 * 1000;
    private static final int ALLOWED_DAYS = 10;
    private static final double SECURITY = 100.0;

    @Mock
    private TxnRepository txnRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private TxnService txnService;

    private User student;
    private Book heldBook;

    @BeforeEach
    void setUp() {
        // @Value fields are not populated when using plain Mockito
        ReflectionTestUtils.setField(txnService, "validUpto", ALLOWED_DAYS);
        ReflectionTestUtils.setField(txnService, "finePerDays", 1);
        student = User.builder().id(1).build();
        heldBook = Book.builder().id(5).securityAmount(SECURITY).user(student).build();
    }

    private static Date daysAgo(int days) {
        // +1 hour keeps us safely away from the day boundary
        return new Date(System.currentTimeMillis() - days * ONE_DAY_MS - 60 * 60 * 1000);
    }

    private Txn issuedTxn(Date issuedDate) {
        return Txn.builder().txnId("T1").txnStatus(TxnStatus.ISSUED)
                .issuedDate(issuedDate).user(student).book(heldBook).build();
    }

    private static TxnReturnRequest returnRequest() {
        return TxnReturnRequest.builder().bookNo("B1").txnId("T1").userEmail("u@x.com").build();
    }

    /** Stubs the lookups every successful or rejected return goes through. */
    private void givenStudentHoldsBookAndTxnExists(Txn txn) {
        when(userService.checkIfUserIsValid(any())).thenReturn(student);
        when(bookService.checkIfBookIsValid(any())).thenReturn(heldBook);
        when(txnRepository.findByTxnId("T1")).thenReturn(txn);
    }

    // ---------- issue (create) ----------

    @Test
    void createThrowsWhenUserIsInvalid() {
        TxnRequest request = TxnRequest.builder().bookNo("B1").userEmail("u@x.com").build();
        when(userService.checkIfUserIsValid(any())).thenReturn(null);

        assertThrows(UserException.class, () -> txnService.create(request));
    }

    @Test
    void createThrowsWhenBookIsInvalid() {
        TxnRequest request = TxnRequest.builder().bookNo("B1").userEmail("u@x.com").build();
        when(userService.checkIfUserIsValid(any())).thenReturn(student);
        when(bookService.checkIfBookIsValid(any())).thenReturn(null);

        assertThrows(BookException.class, () -> txnService.create(request));
    }

    @Test
    void createThrowsWhenBookIsAlreadyIssued() {
        TxnRequest request = TxnRequest.builder().bookNo("B1").userEmail("u@x.com").build();
        Book issuedBook = Book.builder().id(5).bookNo("B1").user(User.builder().id(2).build()).build();
        when(userService.checkIfUserIsValid(any())).thenReturn(student);
        when(bookService.checkIfBookIsValid(any())).thenReturn(issuedBook);

        BookException ex = assertThrows(BookException.class, () -> txnService.create(request));
        assertEquals("Book is not free to be issued", ex.getMessage());
    }

    @Test
    void createSavesIssuedTxnAndMarksBookUnavailable() throws Exception {
        TxnRequest request = TxnRequest.builder().bookNo("B1").userEmail("u@x.com").build();
        Book freeBook = Book.builder().id(5).bookNo("B1").build();
        when(userService.checkIfUserIsValid(any())).thenReturn(student);
        when(bookService.checkIfBookIsValid(any())).thenReturn(freeBook);

        String txnId = txnService.create(request);

        ArgumentCaptor<Txn> captor = ArgumentCaptor.forClass(Txn.class);
        verify(txnRepository).save(captor.capture());
        Txn saved = captor.getValue();
        assertEquals(txnId, saved.getTxnId());
        assertEquals(TxnStatus.ISSUED, saved.getTxnStatus());
        assertSame(student, saved.getUser());
        assertSame(freeBook, saved.getBook());
        assertNotNull(saved.getIssuedDate());
        verify(bookService).markBookUnavailable(freeBook, student);
    }

    // ---------- return: rejected requests ----------

    @Test
    void returnThrowsWhenUserIsInvalid() {
        when(userService.checkIfUserIsValid(any())).thenReturn(null);

        assertThrows(UserException.class, () -> txnService.returnTxn(returnRequest()));
    }

    @Test
    void returnThrowsWhenBookIsNotHeldByThisUser() {
        Book freeBook = Book.builder().id(5).build(); // nobody holds it
        when(userService.checkIfUserIsValid(any())).thenReturn(student);
        when(bookService.checkIfBookIsValid(any())).thenReturn(freeBook);

        assertThrows(TxnException.class, () -> txnService.returnTxn(returnRequest()));
    }

    @Test
    void returnThrowsWhenBookIsHeldBySomeoneElse() {
        Book otherHolder = Book.builder().id(5).user(User.builder().id(99).build()).build();
        when(userService.checkIfUserIsValid(any())).thenReturn(student);
        when(bookService.checkIfBookIsValid(any())).thenReturn(otherHolder);

        assertThrows(TxnException.class, () -> txnService.returnTxn(returnRequest()));
    }

    @Test
    void returnThrowsWhenTxnIdIsUnknown() {
        givenStudentHoldsBookAndTxnExists(null);

        assertThrows(TxnException.class, () -> txnService.returnTxn(returnRequest()));
    }

    @Test
    void returnRejectsTxnThatWasAlreadyClosed() {
        Txn closed = issuedTxn(daysAgo(2));
        closed.setTxnStatus(TxnStatus.RETURNED);
        givenStudentHoldsBookAndTxnExists(closed);

        TxnException ex = assertThrows(TxnException.class, () -> txnService.returnTxn(returnRequest()));
        assertEquals("This transaction has already been closed", ex.getMessage());
    }

    @Test
    void returnRejectsTxnThatBelongsToAnotherBook() {
        Txn otherBooksTxn = issuedTxn(daysAgo(2));
        otherBooksTxn.setBook(Book.builder().id(77).build());
        givenStudentHoldsBookAndTxnExists(otherBooksTxn);

        assertThrows(TxnException.class, () -> txnService.returnTxn(returnRequest()));
    }

    @Test
    void returnRejectsTxnThatBelongsToAnotherUser() {
        Txn otherUsersTxn = issuedTxn(daysAgo(2));
        otherUsersTxn.setUser(User.builder().id(42).build());
        givenStudentHoldsBookAndTxnExists(otherUsersTxn);

        assertThrows(TxnException.class, () -> txnService.returnTxn(returnRequest()));
    }

    // ---------- return: successful settlements ----------

    @Test
    void returnOnTimeRefundsFullSecurityAndFreesTheBook() throws Exception {
        Txn txn = issuedTxn(daysAgo(5));
        givenStudentHoldsBookAndTxnExists(txn);

        Double amount = txnService.returnTxn(returnRequest());

        assertEquals(100.0, amount);
        assertEquals(TxnStatus.RETURNED, txn.getTxnStatus());
        assertEquals(100.0, txn.getSettlementAmount());
        assertNotNull(txn.getSubmittedDate());
        assertNull(heldBook.getUser());
        verify(txnRepository).save(txn);
    }

    @Test
    void returnOnTheLastAllowedDayIsNotFined() throws Exception {
        // Regression test: this used to be reported as FINED because of a Double == Double reference comparison.
        Txn txn = issuedTxn(daysAgo(ALLOWED_DAYS));
        givenStudentHoldsBookAndTxnExists(txn);

        Double amount = txnService.returnTxn(returnRequest());

        assertEquals(100.0, amount);
        assertEquals(TxnStatus.RETURNED, txn.getTxnStatus());
    }

    @Test
    void returnLateDeductsFinePerDayAndMarksFined() throws Exception {
        Txn txn = issuedTxn(daysAgo(ALLOWED_DAYS + 5)); // 5 days late, 1 per day
        givenStudentHoldsBookAndTxnExists(txn);

        Double amount = txnService.returnTxn(returnRequest());

        assertEquals(95.0, amount);
        assertEquals(TxnStatus.FINED, txn.getTxnStatus());
        assertEquals(95.0, txn.getSettlementAmount());
        assertNull(heldBook.getUser());
    }

    @Test
    void returnVeryLateNeverRefundsLessThanZero() throws Exception {
        Txn txn = issuedTxn(daysAgo(500));
        givenStudentHoldsBookAndTxnExists(txn);

        Double amount = txnService.returnTxn(returnRequest());

        assertEquals(0.0, amount);
        assertEquals(TxnStatus.FINED, txn.getTxnStatus());
    }

    // ---------- settlement calculation ----------

    @Test
    void settlementIsFullSecurityWithinAllowedDays() {
        assertEquals(100.0, txnService.calculateSettlementAmount(issuedTxn(daysAgo(5)), heldBook));
    }

    @Test
    void settlementDeductsFinePerDayAfterAllowedDays() {
        // 31 days held, 10 allowed, 1 per day -> 21 fine -> 79
        assertEquals(79.0, txnService.calculateSettlementAmount(issuedTxn(daysAgo(31)), heldBook));
    }

    @Test
    void settlementIsNeverNegative() {
        assertEquals(0.0, txnService.calculateSettlementAmount(issuedTxn(daysAgo(1000)), heldBook));
    }
}
