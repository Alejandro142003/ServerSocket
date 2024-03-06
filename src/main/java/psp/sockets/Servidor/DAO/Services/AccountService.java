package psp.sockets.Servidor.DAO.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psp.sockets.Servidor.DAO.Respositories.AccountRepository;
import psp.sockets.Servidor.Model.Account;

import java.util.Random;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;


    public Account save(Account account) {
        try {
            if (account.getUser() == null) {
                throw new RuntimeException("User is null");
            }
            Account result = Account.builder()
                    .user(account.getUser())
                    .acountNumber(new Random().nextInt())
                    .balance(0)
                    .transactions(null)
                    .build();

            return accountRepository.save(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
