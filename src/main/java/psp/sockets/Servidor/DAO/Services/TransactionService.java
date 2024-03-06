package psp.sockets.Servidor.DAO.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psp.sockets.Servidor.Model.Transaction;

@Service
public class TransactionService {
    @Autowired
    TransactionService transactionService;

    Transaction save(Transaction transaction) {
        try{
         if (transaction == null) throw new RuntimeException("Transaction is null");

         Transaction result = Transaction.builder()
                 .type(transaction.getType())
                 .amount(transaction.getAmount())
                 .date(transaction.getDate())
                 .account(transaction.getAccount())
                 .accountDestiny(transaction.getAccountDestiny())
                 .build();

         return transactionService.save(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
