package psp.sockets.Servidor.DAO.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psp.sockets.Servidor.DAO.Respositories.UserRespository;
import psp.sockets.Servidor.Model.Credentials;

@Service
public class CredentialsService {
    @Autowired
    static UserRespository userRespository;

    public static boolean verificarCredenciales(Credentials credentials) {
        // Verificar las credenciales
        if(credentials == null || credentials.getName() == null || credentials.getContraseña() == null) {
            return false;
        }
        return credentials.getName().equals(userRespository.findByName(credentials.getName()))
                && credentials.getContraseña().equals(credentials.getContraseña());
    }
}