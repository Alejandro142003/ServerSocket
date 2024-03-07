package psp.sockets.Servidor.DAO.Respositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import psp.sockets.Servidor.Model.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRespository extends CrudRepository<User, UUID> {

    Optional<User> findByName(String name);
}
