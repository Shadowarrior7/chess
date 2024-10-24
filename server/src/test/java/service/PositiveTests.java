package service;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.Server;


public class PositiveTests {
    Server server = new Server();

    @Test
    public void Register(){
        String result = server.register(new UserData("username", "password", "email"));
        Assertions.assertInstanceOf(String.class, result);
    }

    @Test
    public void Login(){
        server.register(new UserData("username", "password", "email"));
        String result = server.login("username", "password");
        Assertions.assertInstanceOf(String.class, result);
    }

    @Test
    public void Logout(){
        var serializer = new Gson();
        server.register(new UserData("username", "password", "email"));
        String authData= server.login("username", "password");
        AuthData authData2 = serializer.fromJson(authData, AuthData.class);
        String token =authData2.authToken();
        Assertions.assertDoesNotThrow(() -> server.logout(token), "Logout is throwing an exception");
    }

    @Test
    public void ListGames(){
        var serializer = new Gson();
        server.register(new UserData("username", "password", "email"));
        String authData= server.login("username", "password");
        AuthData authData2 = serializer.fromJson(authData, AuthData.class);
        String token =authData2.authToken();
        String good = serializer.toJson(server.listGames(token));
        Assertions.assertInstanceOf(String.class, good);
    }

    @Test
    public void CreateGames(){
        var serializer = new Gson();
        server.register(new UserData("username", "password", "email"));
        String authData= server.login("username", "password");
        AuthData authData2 = serializer.fromJson(authData, AuthData.class);
        String token =authData2.authToken();
        int result = server.createGame(token, "game");
        Assertions.assertInstanceOf(Integer.class, result);
    }

    @Test
    public void JoinGame(){
        var serializer = new Gson();
        server.register(new UserData("username", "password", "email"));
        String authData= server.login("username", "password");
        AuthData authData2 = serializer.fromJson(authData, AuthData.class);
        String token =authData2.authToken();
        int result = server.createGame(token, "game");

        Assertions.assertDoesNotThrow(() -> server.joinGame("WHITE", String.valueOf(result), token), "Join game is throwing an exception");
    }

    @Test
    public void Clear(){
        Assertions.assertDoesNotThrow(() -> server.clear(), "Clear is throwing an exception");
    }


}
