package psp.sockets.Servidor;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

import psp.sockets.Servidor.DAO.Services.AccountService;
import psp.sockets.Servidor.DAO.Services.TransactionService;
import psp.sockets.Servidor.DAO.Services.UserService;
import psp.sockets.Servidor.Model.Account;
import psp.sockets.Servidor.Model.Credentials;
import psp.sockets.Servidor.Model.Transaction;
import psp.sockets.Servidor.Model.User;

public class GesConect extends Thread {
    private final Socket socket;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;

    public GesConect(Socket socket, AccountService accountService, TransactionService transactionService, UserService userService) {
        this.socket = socket;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @Override
    public void run() {
        try {
            // Establecer flujo de entrada y salida
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            // Recibir credenciales del cliente
            Credentials credentials = (Credentials) inputStream.readObject();

            // Autenticar al usuario
            User user = userService.getUserByName(credentials.getName()).orElse(null);
            if (user == null || !user.getPassword().equals(credentials.getPassword())) {
                // Enviar mensaje de error al cliente si la autenticación falla
                outputStream.writeObject("Autenticación fallida");
                return;
            }

            // Autenticación exitosa, enviar mensaje de éxito al cliente
            outputStream.writeObject("Inicio de sesión exitoso");

            outputStream.writeObject(user.getRole());

            System.out.println(user.getRole());

            int userOption = inputStream.readInt();

            if (user.getRole().equals("cajero")) {
                switch (userOption) {
                    case 1:
                        Account gettedAccount = accountService.getAccountByNumber(inputStream.readInt()).get();
                        outputStream.writeFloat(gettedAccount.getBalance());
                        break;
                    case 2:
                        Account accountTake = accountService.getAccountByNumber(inputStream.readInt()).get();
                        accountService.take(inputStream.readFloat(), accountTake);
                        outputStream.writeObject("Retiro exitoso");
                        break;
                    case 3:
                        Account accountDepo = accountService.getAccountByNumber(inputStream.readInt()).get();
                        accountService.deposit(inputStream.readFloat(), accountDepo);
                        outputStream.writeObject("Deposito exitoso");
                        break;
                }
            } else if (user.getRole().equals("operario")) {
                switch (userOption) {
                    case 1:
                        User newUser = (User) inputStream.readObject();
                        userService.saveUser(newUser);
                        outputStream.writeObject("Usuario creado exitosamente");
                        break;
                    case 2:
                        Account newAccount = (Account) inputStream.readObject();
                        accountService.saveAccount(newAccount);
                        outputStream.writeObject("Cuenta creada exitosamente");
                        break;
                    case 3:
                        int numCuenta = inputStream.readInt();
                        Account account = accountService.getAccountByNumber(numCuenta).get();
                        outputStream.writeFloat(account.getBalance());
                        break;
                    case 4:
                        User gettedUser = (User) inputStream.readObject();
                        outputStream.writeObject(userService.getUserByName(gettedUser.getName()).get());
                        break;
                    case 5:
                        Account accountToDelete = (Account) inputStream.readObject();
                        accountService.deleteAccount(accountToDelete.getId());
                        outputStream.writeObject("Cuenta eliminada exitosamente");
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // Cerrar el socket al finalizar la conexión
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}