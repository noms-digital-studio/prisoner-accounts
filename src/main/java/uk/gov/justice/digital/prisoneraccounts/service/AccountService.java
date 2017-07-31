package uk.gov.justice.digital.prisoneraccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.AccountRepository;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.TransactionRepository;

import java.util.List;
import java.util.Optional;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account newAccount(String establishmentId, String prisonerId, String accName, Account.AccountTypes accountType) {
        return accountRepository.save(Account.builder()
                .establishmentId(establishmentId)
                .prisonerId(prisonerId)
                .accountName(accName)
                .accountType(accountType)
                .build());
    }

    public Optional<Account> accountFor(String establishmentId, String prisonerId, String accName) {
        return accountRepository.findByEstablishmentIdAndPrisonerIdAndAccountNameAndAccountStatus(
                establishmentId, prisonerId, accName, Account.AccountStatuses.OPEN);
    }

    public Optional<Long> balanceOf(String establishmentId, String prisonerId, String accName) {
        return accountFor(establishmentId, prisonerId, accName)
                .map(acc -> balanceOf(acc));
    }

    public long balanceOf(Account account) {
        List<Transaction> transactions = transactionRepository.findAllByAccount(account);

        long sumCredits = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == Transaction.TransactionTypes.CREDIT)
                .mapToLong(Transaction::getAmountPence)
                .sum();

        long sumDebits = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == Transaction.TransactionTypes.DEBIT)
                .mapToLong(Transaction::getAmountPence)
                .sum();

        return sumCredits - sumDebits;
    }


}
