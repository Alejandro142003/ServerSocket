package psp.sockets.Servidor.DAO.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psp.sockets.Servidor.DAO.Respositories.AccountRepository;
import psp.sockets.Servidor.Model.Account;

import java.util.Random;
import java.util.UUID;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;


    /**
     * Recive un objeto Account, comprueba que no es null, lo rellena y lo inserta en la base de datos
     * @param account objeto a insertar en la base de datos
     * @return objeto Account insertado en la base de datos
     */
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

    /**
     * Busca un objeto Account en la base de datos por su UUID
     * @param id UUID del objeto Account a buscar en la base de datos
     * @return objeto Account buscado.
     */
    public Account get(UUID id) {
        try {
            if (id == null) throw new RuntimeException("Account id is null");
            return accountRepository.findById(id).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Actualiza el balance y las transacciones de un objeto Account
     * @param account objeto Account a actualizar
     * @return objeto Account actualizado
     */
    public Account update(Account account) {
        try {
         if (account == null) throw new RuntimeException("Accoutn id is null");
         Account result = accountRepository.findById(account.getId()).get();

         result.setBalance(account.getBalance());
         result.setTransactions(account.getTransactions());

         return accountRepository.save(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
