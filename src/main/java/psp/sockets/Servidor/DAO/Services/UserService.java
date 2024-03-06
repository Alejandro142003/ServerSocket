package psp.sockets.Servidor.DAO.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psp.sockets.Servidor.DAO.Respositories.UserRespository;
import psp.sockets.Servidor.Model.User;

@Service
public class UserService {
    @Autowired
    UserRespository userRespository;

    public User save(User user){
        
    }

}
