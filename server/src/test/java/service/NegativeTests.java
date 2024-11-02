package service;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.GenericException;
import server.Server;

public class NegativeTests {
    Server server = new Server();

    @Test
    public void register() throws DataAccessException {
        server.clear();
        String result = server.register(new UserData("username", "password", "email"));
        Assertions.assertThrows(GenericException.class, () -> {
            server.register(new UserData("username", "password", "email"));
        }, "Error: already taken");
    }

    @Test
    public void login() throws DataAccessException {
        server.clear();
        server.register(new UserData("username", "password", "email"));
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            server.login("fail", "password");
        });

        Assertions.assertInstanceOf(GenericException.class, exception);
    }

    @Test
    public void logout() throws DataAccessException {
        server.clear();
        var serializer = new Gson();
        server.register(new UserData("username", "password", "email"));
        String authData= server.login("username", "password");
        AuthData authData2 = serializer.fromJson(authData, AuthData.class);
        String token =authData2.authToken();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            server.logout("hello");
        }, "Expected an exception to be thrown, but none was.");

        Assertions.assertInstanceOf(GenericException.class, exception);
    }

    @Test
    public void listGames() throws DataAccessException {
        server.clear();
        var serializer = new Gson();
        server.register(new UserData("username", "password", "email"));
        String authData= server.login("username", "password");
        AuthData authData2 = serializer.fromJson(authData, AuthData.class);
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            server.listGames("fail");
        });

        Assertions.assertInstanceOf(GenericException.class, exception);
    }

    @Test
    public void createGames() throws DataAccessException {
        server.clear();
        var serializer = new Gson();
        server.register(new UserData("username", "password", "email"));
        String authData= server.login("username", "password");
        AuthData authData2 = serializer.fromJson(authData, AuthData.class);
        String token =authData2.authToken();
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            server.createGame("fail", "name");
        });

        Assertions.assertInstanceOf(GenericException.class, exception);
    }

    @Test
    public void joinGame() throws DataAccessException {
        server.clear();
        var serializer = new Gson();
        server.register(new UserData("username", "password", "email"));
        String authData= server.login("username", "password");
        AuthData authData2 = serializer.fromJson(authData, AuthData.class);
        String token =authData2.authToken();
        int result = server.createGame(token, "game");

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            server.joinGame("WHITE", "sdssdsdgwrg", "fail");
        });

        Assertions.assertInstanceOf(GenericException.class, exception);
    }
}
