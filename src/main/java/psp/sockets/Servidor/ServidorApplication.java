package psp.sockets.Servidor;

import org.springframework.boot.SpringApplication;
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
			GesConect servidor = new GesConect(/* pasar los servicios y dependencias necesarios */);

			// Iniciar el servidor (lanzar el hilo)
			servidor.start();

			// Retornar la instancia del servidor para que Spring pueda administrarla
			return servidor;
		} catch (Exception e) {
			throw new RuntimeException("Error al iniciar el servidor", e);
		}
	}
}
