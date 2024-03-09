package psp.sockets.Servidor.DAO.Respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import psp.sockets.Servidor.Model.Account;
import psp.sockets.Servidor.Model.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAcountNumber(int acountNumber);
}
