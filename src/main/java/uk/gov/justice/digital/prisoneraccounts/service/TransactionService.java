package uk.gov.justice.digital.prisoneraccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.TransactionRepository;

import java.util.Optional;

@Service
public class TransactionService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(AccountService accountService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    public Transaction creditAccount(Account account, Long amountPence, String description) {
        return transactionRepository.save(Transaction.builder()
                .account(account)
                .amountPence(amountPence)
                .description(description)
                .transactionType(Transaction.TransactionTypes.CREDIT)
                .build());
    }

    public Optional<Transaction> creditAccount(String establishmentId, String prisonerId, String accName, Long amountPence, String description) {
        return accountService.accountFor(establishmentId, prisonerId, accName)
                .map(account -> creditAccount(account, amountPence, description));

    }


    public Transaction debitAccount(Account account, Long amountPence, String description) throws DebitNotSupportedException, InsufficientFundsException {

        checkNotSavingsAccount(account);
        checkSufficientFunds(account, amountPence);

        return transactionRepository.save(Transaction.builder()
                .account(account)
                .amountPence(amountPence)
                .description(description)
                .transactionType(Transaction.TransactionTypes.DEBIT)
                .build());
    }

    private void checkSufficientFunds(Account acc, Long amountPence) throws InsufficientFundsException {
        if (accountService.balanceOf(acc) < amountPence) {
            throw new InsufficientFundsException("Insufficient funds.");
        }
    }

    private void checkNotSavingsAccount(Account acc) throws DebitNotSupportedException {
        if (acc.getAccountType() == Account.AccountTypes.SAVINGS) {
            throw new DebitNotSupportedException("Cannot debit a savings account.");
        }
    }

    public Optional<Transaction> debitAccount(String establishmentId, String prisonerId, String accountName, long amountPence, String description) throws DebitNotSupportedException, InsufficientFundsException {
        Optional<Account> account = accountService.accountFor(establishmentId, prisonerId, accountName);

        if (account.isPresent()) {
            return Optional.of(debitAccount(account.get(), amountPence, description));
        } else {
            return Optional.empty();
        }
    }
}