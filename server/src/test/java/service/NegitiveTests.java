package service;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.GenericException;
import server.Server;

public class NegitiveTests {
    Server server = new Server();

    @Test
    public void Register(){
        String result = server.register(new UserData("username", "password", "email"));
        Assertions.assertInstanceOf(String.class, result);
    }

    @Test
    public void Login(){
        server.register(new UserData("username", "password", "email"));
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            server.login("fail", "password");
        });

        Assertions.assertInstanceOf(GenericException.class, exception);
    }

    @Test
    public void Logout(){
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
    public void ListGames(){
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
    public void CreateGames(){
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
    public void JoinGame(){
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
