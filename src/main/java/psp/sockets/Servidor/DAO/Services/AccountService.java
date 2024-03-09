package psp.sockets.Servidor.DAO.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psp.sockets.Servidor.DAO.Respositories.AccountRepository;
import psp.sockets.Servidor.Model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(UUID id) {
        return accountRepository.findById(id);
    }
    public Optional<Account> getAccountByNumber(int acountNumber) {
        return accountRepository.findByAcountNumber(acountNumber);
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public List<Account> getAccountsByUserId(UUID id){
        return accountRepository.findByUserId(id);
    }

    public void deleteAccount(UUID id) {
        accountRepository.deleteById(id);
    }

    public void deposit(float amount, Account account){
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
    }

    public void take(float amount, Account account){
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
    }
}
