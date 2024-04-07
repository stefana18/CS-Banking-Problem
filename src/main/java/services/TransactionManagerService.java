package services;

import domain.AccountModel;
import domain.AccountType;
import domain.MoneyModel;
import domain.TransactionModel;
import repository.AccountsRepository;
import utils.MoneyUtils;
import utils.MoneyUtils.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionManagerService {

    public TransactionModel transfer(String fromAccountId, String toAccountId, MoneyModel value) {
        AccountModel fromAccount = AccountsRepository.INSTANCE.get(fromAccountId);
        AccountModel toAccount = AccountsRepository.INSTANCE.get(toAccountId);


        if (fromAccount == null || toAccount == null) {
            throw new RuntimeException("Specified account does not exist");
        }

        if (fromAccount.getAccountType() == AccountType.SAVINGS && toAccount.getAccountType() == AccountType.CHECKING) {
            throw new RuntimeException("Cannot transfer between Savings Account to Checking Account");
        }

        if (fromAccount.getAccountType() == AccountType.SAVINGS && toAccount.getAccountType() == AccountType.SAVINGS) {
            throw new RuntimeException("Cannot transfer between Savings Account to Savings Account");

        }

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        if (!value.getCurrency().equals(toAccount.getBalance().getCurrency())) {
            value = MoneyUtils.convert(value, toAccount.getBalance().getCurrency());
        }

        if (fromAccount.getBalance().getAmount() - value.getAmount() < 0) {
            throw new RuntimeException("The transfer amount exceeds the account balance");
        }


        TransactionModel transaction = new TransactionModel(
                UUID.randomUUID(),
                fromAccountId,
                toAccountId,
                value,
                LocalDate.now()
        );

        fromAccount.getBalance().setAmount(fromAccount.getBalance().getAmount() - value.getAmount());
        fromAccount.getTransactions().add(transaction);

        toAccount.getBalance().setAmount(toAccount.getBalance().getAmount() + value.getAmount());
        toAccount.getTransactions().add(transaction);

        return transaction;
    }

    public TransactionModel withdraw(String accountId, MoneyModel amount) {
        AccountModel account = AccountsRepository.INSTANCE.get(accountId);

        if (account == null) {
            throw new RuntimeException("Specified account does not exist");
        }

        if (account.getBalance().getAmount() - amount.getAmount() < 0) {
            throw new RuntimeException("The withdrawal amount exceeds the account balance");
        }

        TransactionModel transaction = new TransactionModel(
                UUID.randomUUID(),
                accountId,
                accountId,
                amount,
                LocalDate.now()
        );

        account.getBalance().setAmount(account.getBalance().getAmount() - amount.getAmount());
        account.getTransactions().add(transaction);

        return transaction;
    }

    public MoneyModel checkFunds(String accountId) {
        if (!AccountsRepository.INSTANCE.exist(accountId)) {
            throw new RuntimeException("Specified account does not exist");
        }
        return AccountsRepository.INSTANCE.get(accountId).getBalance();
    }

    public List<TransactionModel> retrieveTransactions(String accountId) {
        if (!AccountsRepository.INSTANCE.exist(accountId)) {
            throw new RuntimeException("Specified account does not exist");
        }
        return new ArrayList<>(AccountsRepository.INSTANCE.get(accountId).getTransactions());
    }
}

