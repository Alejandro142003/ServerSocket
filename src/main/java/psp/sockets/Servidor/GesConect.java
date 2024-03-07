package psp.sockets.Servidor;

import lombok.AllArgsConstructor;
import psp.sockets.Servidor.DAO.Services.CredentialsService;
import psp.sockets.Servidor.Model.Credentials;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@AllArgsConstructor
public class GesConect extends Thread {

    public void run() {
        try (
                ServerSocket serverSocket = new ServerSocket(12345); // Escuchar en el puerto 12345
                Socket socket = serverSocket.accept(); // Aceptar conexiones entrantes
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())
        ) {
            // Leer credenciales del cliente
            Credentials credentials = (Credentials) inputStream.readObject();

            // Procesar y verificar las credenciales
            String respuesta;
            if (verificarCredenciales(credentials)) {
                respuesta = "Inicio de sesión exitoso";
            } else {
                respuesta = "Inicio de sesión fallido";
            }

            // Enviar respuesta al cliente
            outputStream.writeUTF(respuesta);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean verificarCredenciales(Credentials credentials) {
        // Verificar las credenciales
        if(credentials == null || credentials.getName() == null || credentials.getContraseña() == null) {
            return false;
        }
        return CredentialsService.verificarCredenciales(credentials);
    }
}