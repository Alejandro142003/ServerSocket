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
        try {
            if (user == null) throw new RuntimeException("User is null");

            User result = User.builder()
                    .name(user.getName())
                    .password(user.getPassword())
                    .role(user.getRole())
                    .accounts(user.getAccounts())
                    .build();

            return userRespository.save(result);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public User findByName(String name) {
        try{
             User result = userRespository.findByName(name)
                    .orElse(null);
             return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
