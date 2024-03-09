package psp.sockets.Servidor;

/*import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServidorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServidorApplication.class, args);

	}

	@Bean
	public GesConect iniciarServidor() {
		try {
			// Instanciar el servidor
			GesConect servidor = new GesConect(*//* pasar los servicios y dependencias necesarios *//*);

			// Iniciar el servidor (lanzar el hilo)
			servidor.start();

			// Retornar la instancia del servidor para que Spring pueda administrarla
			return servidor;
		} catch (Exception e) {
			throw new RuntimeException("Error al iniciar el servidor", e);
		}
	}
}*/

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import psp.sockets.Servidor.DAO.Respositories.AccountRepository;
import psp.sockets.Servidor.DAO.Respositories.TransactionRepository;
import psp.sockets.Servidor.DAO.Respositories.UserRepository;
import psp.sockets.Servidor.DAO.Services.AccountService;
import psp.sockets.Servidor.DAO.Services.TransactionService;
import psp.sockets.Servidor.DAO.Services.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class ServidorApplication {

	public static void main(String[] args) {
		// Inicializar la aplicación Spring Boot
		ConfigurableApplicationContext context = SpringApplication.run(ServidorApplication.class, args);

		// Obtener los repositorios y servicios necesarios
		AccountRepository accountRepository = context.getBean(AccountRepository.class);
		TransactionRepository transactionRepository = context.getBean(TransactionRepository.class);
		UserRepository userRepository = context.getBean(UserRepository.class);
		AccountService accountService = context.getBean(AccountService.class);
		TransactionService transactionService = context.getBean(TransactionService.class);
		UserService userService = context.getBean(UserService.class);

		// Inicializar un servidor en un puerto específico
		int puerto = 12345; // Puerto de escucha del servidor
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(puerto);
			System.out.println("Servidor iniciado en el puerto " + puerto);

			// Bucle infinito para el funcionamiento del servidor
			while (true) {
				// Aceptar nuevas conexiones de clientes
				Socket socket = serverSocket.accept();
				System.out.println("Nueva conexión aceptada desde " + socket.getInetAddress());

				// Generar un nuevo hilo que gestione la conexión con el cliente
				GesConect gesConect = new GesConect(socket, accountService, transactionService, userService);
				gesConect.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null) {
				try {
					// Cerrar el socket del servidor al finalizar
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
