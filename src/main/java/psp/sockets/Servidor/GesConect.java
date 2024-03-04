package psp.sockets.Servidor;

import lombok.AllArgsConstructor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@AllArgsConstructor
public class GesConect extends Thread {
    private final DAO dao;
    private final Socket socket;

    @Override
    public void run() {
        try (
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())
        ) {
            // -------------------IMPORTANTE------------------------
            // Esto es un ejemplo y se debera modificar mas adelante
            // -----------------------------------------------------

            // Lógica para manejar la conexión con el cliente
            System.out.println("Cliente conectado desde: " + socket.getInetAddress());

            // Leer objeto enviado por el cliente
            Object objetoRecibido = inputStream.readObject();
            System.out.println("Objeto recibido del cliente: " + objetoRecibido);

            // Enviar respuesta al cliente
            String respuesta = "Respuesta desde el servidor";
            outputStream.writeObject(respuesta);
            System.out.println("Respuesta enviada al cliente: " + respuesta);

            // Cerrar la sesión con el cliente
            socket.close();
            System.out.println("Sesión cerrada con el cliente: " + socket.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}