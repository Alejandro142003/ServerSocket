package psp.sockets.Servidor.Model;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@Getter
@Setter
public class Credenciales implements Serializable {
    private String usuario;
    private String contraseña;
}
