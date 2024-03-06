package psp.sockets.Servidor.DAO.Respositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import psp.sockets.Servidor.Model.Transaction;

import java.util.UUID;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, UUID> {
}
