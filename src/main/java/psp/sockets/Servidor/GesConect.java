package psp.sockets.Servidor;

import lombok.AllArgsConstructor;
import psp.sockets.Servidor.Model.Credenciales;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@AllArgsConstructor
public class GesConect extends Thread {
    private final Socket socket;

    public void run() {
        try (
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())
        ) {
            // Leer credenciales del cliente
            Credenciales credenciales = (Credenciales) inputStream.readObject();

            // Procesar y verificar las credenciales
            String respuesta;
            if (verificarCredenciales(credenciales)) {
                respuesta = "Inicio de sesión exitoso";
            } else {
                respuesta = "Inicio de sesión fallido";
            }

            // Enviar respuesta al cliente
            outputStream.writeUTF(respuesta);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // Cerrar la conexión con el cliente
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verificarCredenciales(Credenciales credenciales) {
        // Verificar las credenciales
        if(credenciales == null || credenciales.getUsuario() == null || credenciales.getContraseña() == null) {
            return false;
        } else {
            dao.verificarCredenciales(credenciales);
        }


        return credenciales.getUsuario().equals("usuario") && credenciales.getContraseña().equals("hashContraseña");
    }
}