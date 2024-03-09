package psp.sockets.Servidor;

import java.io.*;
import java.net.Socket;
import java.util.List;
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

            //Object option = inputStream.readObject();

            if(user.getRole().equals("cajero")) {
                Object option = inputStream.readObject();
                if ((int)option == 1){
                    System.out.println("Ver dinero cuenta");

                    int accountNumber = (int) inputStream.readObject();



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

                    autenticate(inputStream, outputStream, userId);

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
                                    autenticate(inputStream, outputStream, userId);
                                } else {
                                    // Saldo insuficiente
                                    outputStream.writeObject("Saldo insuficiente para realizar la transacción.");
                                    autenticate(inputStream, outputStream, userId);
                                }
                            } else {
                                outputStream.writeObject("La cuenta no pertenece al usuario actual.");
                                autenticate(inputStream, outputStream, userId);
                            }
                        } else {
                            // La cuenta no existe
                            outputStream.writeObject("La cuenta no existe.");
                            autenticate(inputStream, outputStream, userId);
                        }
                    } catch (Exception e) {
                        // Manejar cualquier excepción que pueda ocurrir durante la operación
                        outputStream.writeObject("Error al procesar la transacción: " + e.getMessage());
                        autenticate(inputStream, outputStream, userId);
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
                                autenticate(inputStream, outputStream, userId);
                            } else {
                                outputStream.writeObject("La cuenta no pertenece al usuario actual.");
                                autenticate(inputStream, outputStream, userId);
                            }
                        } else {
                            // La cuenta no existe
                            outputStream.writeObject("La cuenta no existe.");
                            autenticate(inputStream, outputStream, userId);
                        }
                    } catch (Exception e) {
                        // Manejar cualquier excepción que pueda ocurrir durante la operación
                        outputStream.writeObject("Error al procesar el depósito: " + e.getMessage());
                        autenticate(inputStream, outputStream, userId);
                    }
                }
            } else if (user.getRole().equals("operario")) {
                Object optionn = inputStream.readObject();

                if ((int)optionn == 1){
                    System.out.println("Ingresar un nuevo usuario en el banco");

                    String newUserName = (String) inputStream.readObject();
                    String newUserPassword = (String) inputStream.readObject();
                    String newUserRole = (String) inputStream.readObject();

                    // Verificar si ya existe un usuario con el mismo nombre
                    User existingUser = userService.getUserByName(newUserName).orElse(null);
                    if (existingUser != null) {
                        outputStream.writeObject("Ya existe un usuario con el mismo nombre.");
                        autenticate2(inputStream, outputStream, existingUser.getId());
                    } else {
                        // No hay ningún usuario con el mismo nombre, proceder con la inserción
                        User newUser = new User();
                        newUser.setName(newUserName);
                        newUser.setPassword(newUserPassword);
                        newUser.setRole(newUserRole);

                        // Insertar el nuevo usuario en la base de datos
                        userService.saveUser(newUser);

                        outputStream.writeObject("Usuario ingresado correctamente.");
                        autenticate2(inputStream, outputStream, existingUser.getId());

                    }
                } else if ((int)optionn == 2) {
                    System.out.println("Crear una nueva cuenta bancaria");

                    try {
                        // Leer los datos necesarios para crear la nueva cuenta bancaria
                        int newAccountNumber = (int) inputStream.readObject();
                        float newBalance = (float) inputStream.readObject();
                        String userNameAssociated = (String) inputStream.readObject();

                        // Verificar si el número de cuenta ya existe
                        Optional<Account> existingAccount = accountService.getAccountByNumber(newAccountNumber);
                        if (existingAccount.isPresent()) {
                            // El número de cuenta ya existe, enviar un mensaje de error al cliente
                            outputStream.writeObject("El número de cuenta ya existe. No se puede crear la cuenta bancaria.");

                            return;
                        }

                        // Buscar al usuario por su nombre
                        Optional<User> optionalUser = userService.getUserByName(userNameAssociated);
                        if (optionalUser.isPresent()) {
                            // El usuario existe, por lo tanto, podemos asociar la nueva cuenta a este usuario
                            User userSelected = optionalUser.get();

                            // Crear la nueva cuenta bancaria
                            Account newAccount = new Account();
                            newAccount.setAcountNumber(newAccountNumber);
                            newAccount.setBalance(newBalance);
                            newAccount.setUser(userSelected);

                            // Guardar la nueva cuenta bancaria en la base de datos o en el sistema
                            accountService.saveAccount(newAccount);

                            outputStream.writeObject("Cuenta bancaria creada exitosamente.");
                            autenticate2(inputStream, outputStream, userSelected.getId());

                        } else {
                            // El usuario no existe
                            outputStream.writeObject("El usuario no existe. No se puede crear la cuenta bancaria.");
                        }
                    } catch (IOException e) {
                        // Manejar la excepción de lectura de datos
                        e.printStackTrace();
                        outputStream.writeObject("Error al leer los datos para crear la cuenta bancaria.");
                    }

                } else if ((int)optionn == 3) {
                    System.out.println("Ver los datos de una cuenta bancaria");

                    try {
                        // Leer el número de cuenta y el nombre de usuario asociado
                        int accountNumber = (int) inputStream.readObject();
                        String userNameAssociated = (String) inputStream.readObject();

                        // Buscar al usuario por su nombre
                        Optional<User> optionalUser = userService.getUserByName(userNameAssociated);
                        if (optionalUser.isPresent()) {
                            User userShowing = optionalUser.get();

                            // Verificar que el usuario es propietario de la cuenta
                            Optional<Account> optionalAccount = accountService.getAccountByNumber(accountNumber);
                            if (optionalAccount.isPresent()) {
                                Account account = optionalAccount.get();
                                if (account.getUser().getId().equals(userShowing.getId())) {
                                    // El usuario es propietario de la cuenta, enviar los datos de la cuenta al cliente
                                    outputStream.writeObject("Número de cuenta: " + account.getAcountNumber() +
                                            ", Saldo: " + account.getBalance() +
                                            ", Propietario: " + account.getUser().getName());

                                    autenticate2(inputStream, outputStream, userShowing.getId());

                                } else {
                                    // El usuario no es propietario de la cuenta, enviar un mensaje de error al cliente
                                    outputStream.writeObject("El usuario no tiene permiso para ver los datos de esta cuenta.");
                                    autenticate2(inputStream, outputStream, userShowing.getId());
                                }
                            } else {
                                // La cuenta no existe
                                outputStream.writeObject("La cuenta bancaria no existe.");
                                autenticate2(inputStream, outputStream, userShowing.getId());
                            }
                        } else {
                            // El usuario no existe
                            outputStream.writeObject("El usuario no existe.");

                        }
                    } catch (IOException e) {
                        // Manejar la excepción de lectura de datos
                        e.printStackTrace();
                        outputStream.writeObject("Error al leer los datos para ver la cuenta bancaria.");
                    }
                } else if ((int)optionn == 4) {
                    System.out.println("Opción seleccionada: Ver los datos de un cliente");

                    try {
                        // Leer el nombre de usuario
                        String userNameAssociated = (String) inputStream.readObject();

                        // Buscar al usuario por su nombre
                        Optional<User> optionalUser = userService.getUserByName(userNameAssociated);
                        if (optionalUser.isPresent()) {
                            // El usuario existe, enviar los datos del usuario al cliente
                            User userShow = optionalUser.get();
                            StringBuilder userData = new StringBuilder();
                            userData.append("Nombre: ").append(userShow.getName()).append(", Rol: ").append(userShow.getRole());

                            // Verificar si el usuario tiene cuentas bancarias asociadas
                            List<Account> userAccounts = accountService.getAccountsByUserId(userShow.getId());
                            if (!userAccounts.isEmpty()) {
                                userData.append(", Numero de la cuenta asociada : ");
                                for (Account account : userAccounts) {
                                    userData.append(account.getAcountNumber()).append(", ");
                                }
                                // Eliminar la coma adicional al final
                                userData.delete(userData.length() - 2, userData.length());
                            }

                            outputStream.writeObject(userData.toString());
                            autenticate2(inputStream, outputStream, userShow.getId());
                        } else {
                            // El usuario no existe
                            outputStream.writeObject("El usuario no existe.");
                        }
                    } catch (IOException e) {
                        // Manejar la excepción de lectura de datos
                        e.printStackTrace();
                        outputStream.writeObject("Error al leer los datos para ver el usuario.");
                    }
                } else if ((int)optionn == 5) {
                    System.out.println("Opción seleccionada: Eliminar una cuenta bancaria");

                    try {
                        // Leer el número de cuenta y el nombre de usuario asociado
                        int accountNumberToDelete = (int) inputStream.readObject();
                        String userNameAssociatedToDelete = (String) inputStream.readObject();

                        // Buscar al usuario por su nombre
                        Optional<User> optionalUser = userService.getUserByName(userNameAssociatedToDelete);
                        if (optionalUser.isPresent()) {
                            User userDeleting = optionalUser.get();

                            // Verificar que el usuario es propietario de la cuenta a eliminar
                            Optional<Account> optionalAccount = accountService.getAccountByNumber(accountNumberToDelete);
                            if (optionalAccount.isPresent()) {
                                Account accountToDelete = optionalAccount.get();
                                if (accountToDelete.getUser().getId().equals(userDeleting.getId())) {
                                    // El usuario es propietario de la cuenta, proceder con la eliminación
                                    accountService.deleteAccount(accountToDelete.getId());
                                    outputStream.writeObject("Cuenta bancaria eliminada exitosamente.");
                                    autenticate2(inputStream, outputStream, userDeleting.getId());
                                } else {
                                    // El usuario no es propietario de la cuenta, enviar un mensaje de error al cliente
                                    outputStream.writeObject("El usuario no tiene permiso para eliminar esta cuenta bancaria.");
                                    autenticate2(inputStream, outputStream, userDeleting.getId());

                                }
                            } else {
                                // La cuenta no existe
                                outputStream.writeObject("La cuenta bancaria no existe.");
                                autenticate2(inputStream, outputStream, userDeleting.getId());

                            }
                        } else {
                            // El usuario no existe
                            outputStream.writeObject("El usuario no existe.");

                        }
                    } catch (IOException e) {
                        // Manejar la excepción de lectura de datos
                        e.printStackTrace();
                        outputStream.writeObject("Error al leer los datos para eliminar la cuenta bancaria.");
                    }
                }

            }else {
                outputStream.writeObject("Error");
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

    /**
     * Autentica al usuario y maneja las operaciones relacionadas con cuentas bancarias.
     *
     * @param inputStream  El flujo de entrada para recibir datos del cliente.
     * @param outputStream El flujo de salida para enviar datos al cliente.
     * @param userId       El identificador único del usuario autenticado.
     * @throws IOException            Si ocurre un error de entrada/salida durante la comunicación con el cliente.
     * @throws ClassNotFoundException Si la clase de un objeto serializado recibido no se encuentra.
     */
    public void autenticate( ObjectInputStream inputStream, ObjectOutputStream outputStream, UUID userId) throws IOException, ClassNotFoundException {

        //Object option = inputStream.readObject();

            Object option = inputStream.readObject();
            if ((int)option == 1){
                System.out.println("Ver dinero cuenta");

                int accountNumber = (int) inputStream.readObject();



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

    }

    /**
     * Autentica al usuario y maneja las operaciones relacionadas con la administración del banco.
     *
     * @param inputStream  El flujo de entrada para recibir datos del cliente.
     * @param outputStream El flujo de salida para enviar datos al cliente.
     * @param userId       El identificador único del usuario autenticado.
     * @throws IOException            Si ocurre un error de entrada/salida durante la comunicación con el cliente.
     * @throws ClassNotFoundException Si la clase de un objeto serializado recibido no se encuentra.
     */
    public void autenticate2( ObjectInputStream inputStream, ObjectOutputStream outputStream, UUID userId) throws IOException, ClassNotFoundException {
        Object optionn = inputStream.readObject();

        if ((int)optionn == 1){
            System.out.println("Ingresar un nuevo usuario en el banco");

            String newUserName = (String) inputStream.readObject();
            String newUserPassword = (String) inputStream.readObject();
            String newUserRole = (String) inputStream.readObject();

            // Verificar si ya existe un usuario con el mismo nombre
            User existingUser = userService.getUserByName(newUserName).orElse(null);
            if (existingUser != null) {
                outputStream.writeObject("Ya existe un usuario con el mismo nombre.");
            } else {
                // No hay ningún usuario con el mismo nombre, proceder con la inserción
                User newUser = new User();
                newUser.setName(newUserName);
                newUser.setPassword(newUserPassword);
                newUser.setRole(newUserRole);

                // Insertar el nuevo usuario en la base de datos
                userService.saveUser(newUser);

                outputStream.writeObject("Usuario ingresado correctamente.");
            }
        } else if ((int)optionn == 2) {
            System.out.println("Crear una nueva cuenta bancaria");

            try {
                // Leer los datos necesarios para crear la nueva cuenta bancaria
                int newAccountNumber = (int) inputStream.readObject();
                float newBalance = (float) inputStream.readObject();
                String userNameAssociated = (String) inputStream.readObject();

                // Verificar si el número de cuenta ya existe
                Optional<Account> existingAccount = accountService.getAccountByNumber(newAccountNumber);
                if (existingAccount.isPresent()) {
                    // El número de cuenta ya existe, enviar un mensaje de error al cliente
                    outputStream.writeObject("El número de cuenta ya existe. No se puede crear la cuenta bancaria.");
                    return;
                }

                // Buscar al usuario por su nombre
                Optional<User> optionalUser = userService.getUserByName(userNameAssociated);
                if (optionalUser.isPresent()) {
                    // El usuario existe, por lo tanto, podemos asociar la nueva cuenta a este usuario
                    User userSelected = optionalUser.get();

                    // Crear la nueva cuenta bancaria
                    Account newAccount = new Account();
                    newAccount.setAcountNumber(newAccountNumber);
                    newAccount.setBalance(newBalance);
                    newAccount.setUser(userSelected);

                    // Guardar la nueva cuenta bancaria en la base de datos o en el sistema
                    accountService.saveAccount(newAccount);

                    outputStream.writeObject("Cuenta bancaria creada exitosamente.");
                } else {
                    // El usuario no existe
                    outputStream.writeObject("El usuario no existe. No se puede crear la cuenta bancaria.");
                }
            } catch (IOException e) {
                // Manejar la excepción de lectura de datos
                e.printStackTrace();
                outputStream.writeObject("Error al leer los datos para crear la cuenta bancaria.");
            }

        } else if ((int)optionn == 3) {
            System.out.println("Ver los datos de una cuenta bancaria");

            try {
                // Leer el número de cuenta y el nombre de usuario asociado
                int accountNumber = (int) inputStream.readObject();
                String userNameAssociated = (String) inputStream.readObject();

                // Buscar al usuario por su nombre
                Optional<User> optionalUser = userService.getUserByName(userNameAssociated);
                if (optionalUser.isPresent()) {
                    User userShowing = optionalUser.get();

                    // Verificar que el usuario es propietario de la cuenta
                    Optional<Account> optionalAccount = accountService.getAccountByNumber(accountNumber);
                    if (optionalAccount.isPresent()) {
                        Account account = optionalAccount.get();
                        if (account.getUser().getId().equals(userShowing.getId())) {
                            // El usuario es propietario de la cuenta, enviar los datos de la cuenta al cliente
                            outputStream.writeObject("Número de cuenta: " + account.getAcountNumber() +
                                    ", Saldo: " + account.getBalance() +
                                    ", Propietario: " + account.getUser().getName());
                        } else {
                            // El usuario no es propietario de la cuenta, enviar un mensaje de error al cliente
                            outputStream.writeObject("El usuario no tiene permiso para ver los datos de esta cuenta.");
                        }
                    } else {
                        // La cuenta no existe
                        outputStream.writeObject("La cuenta bancaria no existe.");
                    }
                } else {
                    // El usuario no existe
                    outputStream.writeObject("El usuario no existe.");
                }
            } catch (IOException e) {
                // Manejar la excepción de lectura de datos
                e.printStackTrace();
                outputStream.writeObject("Error al leer los datos para ver la cuenta bancaria.");
            }
        } else if ((int)optionn == 4) {
            System.out.println("Opción seleccionada: Ver los datos de un cliente");

            try {
                // Leer el nombre de usuario
                String userNameAssociated = (String) inputStream.readObject();

                // Buscar al usuario por su nombre
                Optional<User> optionalUser = userService.getUserByName(userNameAssociated);
                if (optionalUser.isPresent()) {
                    // El usuario existe, enviar los datos del usuario al cliente
                    User userShow = optionalUser.get();
                    StringBuilder userData = new StringBuilder();
                    userData.append("Nombre: ").append(userShow.getName()).append(", Rol: ").append(userShow.getRole());

                    // Verificar si el usuario tiene cuentas bancarias asociadas
                    List<Account> userAccounts = accountService.getAccountsByUserId(userShow.getId());
                    if (!userAccounts.isEmpty()) {
                        userData.append(", Numero de la cuenta asociada : ");
                        for (Account account : userAccounts) {
                            userData.append(account.getAcountNumber()).append(", ");
                        }
                        // Eliminar la coma adicional al final
                        userData.delete(userData.length() - 2, userData.length());
                    }

                    outputStream.writeObject(userData.toString());
                } else {
                    // El usuario no existe
                    outputStream.writeObject("El usuario no existe.");
                }
            } catch (IOException e) {
                // Manejar la excepción de lectura de datos
                e.printStackTrace();
                outputStream.writeObject("Error al leer los datos para ver el usuario.");
            }
        } else if ((int)optionn == 5) {
            System.out.println("Opción seleccionada: Eliminar una cuenta bancaria");

            try {
                // Leer el número de cuenta y el nombre de usuario asociado
                int accountNumberToDelete = (int) inputStream.readObject();
                String userNameAssociatedToDelete = (String) inputStream.readObject();

                // Buscar al usuario por su nombre
                Optional<User> optionalUser = userService.getUserByName(userNameAssociatedToDelete);
                if (optionalUser.isPresent()) {
                    User userDeleting = optionalUser.get();

                    // Verificar que el usuario es propietario de la cuenta a eliminar
                    Optional<Account> optionalAccount = accountService.getAccountByNumber(accountNumberToDelete);
                    if (optionalAccount.isPresent()) {
                        Account accountToDelete = optionalAccount.get();
                        if (accountToDelete.getUser().getId().equals(userDeleting.getId())) {
                            // El usuario es propietario de la cuenta, proceder con la eliminación
                            accountService.deleteAccount(accountToDelete.getId());
                            outputStream.writeObject("Cuenta bancaria eliminada exitosamente.");
                        } else {
                            // El usuario no es propietario de la cuenta, enviar un mensaje de error al cliente
                            outputStream.writeObject("El usuario no tiene permiso para eliminar esta cuenta bancaria.");
                        }
                    } else {
                        // La cuenta no existe
                        outputStream.writeObject("La cuenta bancaria no existe.");
                    }
                } else {
                    // El usuario no existe
                    outputStream.writeObject("El usuario no existe.");
                }
            } catch (IOException e) {
                // Manejar la excepción de lectura de datos
                e.printStackTrace();
                outputStream.writeObject("Error al leer los datos para eliminar la cuenta bancaria.");
            }
        }
    }

}