package psp.sockets.Servidor.Model;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@Getter
@Setter
public class Credentials implements Serializable {
    private String name;
    private String contraseña;
}
