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
                outputStream.writeObject(null);
                return;
            }

            // Obtener el ID del usuario autenticado
            UUID userId = user.getId();

            // Autenticación exitosa, enviar mensaje de éxito al cliente
            outputStream.writeObject("Inicio de sesión exitoso");

            outputStream.writeObject(user.getRole());

            System.out.println(user.getRole());

            Object option = inputStream.readObject();

            if ((int)option == 1){
                System.out.println("Ver dinero cuenta");

                int accountNumber = (int) inputStream.readObject();


                /*Account account =  accountService.getAccountByNumber(accountNumber).get();

                outputStream.writeObject(account.getBalance());*/

                Optional<Account> optionalAccount = accountService.getAccountByNumber(accountNumber);
                if (optionalAccount.isPresent()) {
                    Account account = optionalAccount.get();
                    if (account.getUser().getId().equals(userId)) { // Comprobar el ID del usuario asociado a la cuenta
                        outputStream.writeObject(account.getBalance());
                    } else {
                        outputStream.writeObject("La cuenta no pertenece al usuario actual.");
                    }
                } else {
                    outputStream.writeObject("La cuenta no existe.");
                }

            } else if ((int)option == 2) {
                System.out.println("Sacar dinero");

                int accountNumber = (int) inputStream.readObject();
                int money = (int) inputStream.readObject();

                try {
                    Optional<Account> optionalAccount = accountService.getAccountByNumber(accountNumber);
                    if (optionalAccount.isPresent()) {
                        Account account = optionalAccount.get();
                        if (account.getUser().getId().equals(userId)) { // Comprobar el ID del usuario asociado a la cuenta
                            float balance = account.getBalance();
                            if (balance >= money) {
                                // Suficiente saldo para realizar la transacción
                                balance -= money;
                                account.setBalance(balance);
                                accountService.saveAccount(account); // Guardar los cambios en la cuenta
                                outputStream.writeObject("Transacción exitosa. Nuevo saldo: " + balance);
                            } else {
                                // Saldo insuficiente
                                outputStream.writeObject("Saldo insuficiente para realizar la transacción.");
                            }
                        } else {
                            outputStream.writeObject("La cuenta no pertenece al usuario actual.");
                        }
                    } else {
                        // La cuenta no existe
                        outputStream.writeObject("La cuenta no existe.");
                    }
                } catch (Exception e) {
                    // Manejar cualquier excepción que pueda ocurrir durante la operación
                    outputStream.writeObject("Error al procesar la transacción: " + e.getMessage());
                }
            } else if ((int)option == 3) {
                System.out.println("Ingresar dinero");

                int accountNumber = (int) inputStream.readObject();
                int money = (int) inputStream.readObject();

                try {
                    Optional<Account> optionalAccount = accountService.getAccountByNumber(accountNumber);
                    if (optionalAccount.isPresent()) {
                        Account account = optionalAccount.get();
                        if (account.getUser().getId().equals(userId)) { // Comprobar el ID del usuario asociado a la cuenta
                            float balance = account.getBalance();

                            // Actualizar el saldo de la cuenta
                            balance += money;
                            account.setBalance(balance);
                            accountService.saveAccount(account); // Guardar los cambios en la cuenta

                            outputStream.writeObject("Depósito exitoso. Nuevo saldo: " + balance);
                        } else {
                            outputStream.writeObject("La cuenta no pertenece al usuario actual.");
                        }
                    } else {
                        // La cuenta no existe
                        outputStream.writeObject("La cuenta no existe.");
                    }
                } catch (Exception e) {
                    // Manejar cualquier excepción que pueda ocurrir durante la operación
                    outputStream.writeObject("Error al procesar el depósito: " + e.getMessage());
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