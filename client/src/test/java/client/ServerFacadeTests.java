package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.JoinGame;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    public static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("https::/localhost:"+port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    //positive
    @Test
    public void registerPos(){
        UserData user7 = new UserData("user234", "password3434", "ema34il");
        try {
            Assertions.assertThrows(Exception.class, ()-> serverFacade.register(user7));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void loginPos(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            serverFacade.logout(token);
            Assertions.assertDoesNotThrow(() -> serverFacade.login(user));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void logoutPos(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            Assertions.assertDoesNotThrow(() -> serverFacade.logout(token));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void listPos(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            Assertions.assertDoesNotThrow(() -> serverFacade.listGames(token));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void createPos(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            Assertions.assertDoesNotThrow(() -> serverFacade.createGame(token, new GameData(1,
                    null, null, "new game", new ChessGame())));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void joinPos(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            String gameId = serverFacade.createGame(token, new GameData(1,
                    null, null, "new game", new ChessGame()));
            Assertions.assertDoesNotThrow(() -> serverFacade.joinGame(token, new JoinGame("WHITE", gameId)));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //negitive
    @Test
    public void registerNeg(){
        UserData user = new UserData("user", "password", "email");
        try {
            Assertions.assertThrows(Exception.class, ()-> serverFacade.register(user));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void loginNeg(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            serverFacade.logout(token);
            Assertions.assertThrows(Exception.class, () -> serverFacade.login(user));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void logoutNeg(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            Assertions.assertThrows(Exception.class, () -> serverFacade.logout(token));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void listNeg(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            Assertions.assertThrows(Exception.class, () -> serverFacade.listGames(token));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void createNeg(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            Assertions.assertThrows(Exception.class, () -> serverFacade.createGame(token, new GameData(1,
                    null, null, "new game", new ChessGame())));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void joinNeg(){
        UserData user = new UserData("user", "password", "email");
        try {
            String token = serverFacade.register(user).authToken();
            String gameId = serverFacade.createGame(token, new GameData(1,
                    null, null, "new game", new ChessGame()));
            Assertions.assertThrows(Exception.class, () -> serverFacade.joinGame(token, new JoinGame("WHITE",
                    gameId)));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
