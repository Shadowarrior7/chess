package DataAccess;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.Server;

import javax.xml.crypto.Data;

public class PositiveTests {
    Server server = new Server();
    SQLGameDao gameDao = new SQLGameDao();
    SQLAuthDao authDao = new SQLAuthDao();
    SQLUserDao userDao = new SQLUserDao();
    DatabaseManager database = new DatabaseManager();

    public PositiveTests() throws DataAccessException {
    }

    //auth

    @Test
    public void addAuth() throws DataAccessException {
        String token = authDao.addAuthData("user");
        AuthData auth2 = authDao.getAuthData(token);
        Assertions.assertEquals(token, auth2.authToken());
    }

    @Test
    void testGenerateToken() {
        String token = SQLAuthDao.generateToken();
        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isEmpty());
    }

    @Test
    void deleteAuth() throws DataAccessException {
        String token = authDao.addAuthData("user");
        authDao.deleteAuthData(token);
        AuthData auth = authDao.getAuthData(token);
        Assertions.assertNull(auth);
    }

    @Test
    void getAuth() throws DataAccessException {
        String token = authDao.addAuthData("user");
        AuthData auth2 = authDao.getAuthData(token);
        Assertions.assertNotNull(auth2);
    }

    @Test
    void authClear() throws DataAccessException {
        Assertions.assertDoesNotThrow(() -> authDao.clear(), "Clear is throwing an exception");

    }
    //users
    @Test
    void getUser() throws DataAccessException {
        userDao.addUser("user", "password", "email");
        Assertions.assertNotNull(userDao.getUser("user"));
    }

    @Test
    void addUser(){
        Assertions.assertDoesNotThrow(() -> userDao.addUser("user", "password", "email"), "Clear is throwing an exception");
    }

    @Test
    void getPass() throws DataAccessException {
        userDao.addUser("user", "password", "email");
        Assertions.assertNotNull(userDao.getUserPassword("user"));
    }

    @Test
    void userClear(){
        Assertions.assertDoesNotThrow(() -> userDao.clear(), "Clear is throwing an exception");

    }

    //game

    @Test
    void getGames() throws DataAccessException {
        gameDao.clear();
        gameDao.createGame(new GameData(123, "user",
                "user2", "name", new ChessGame()));
        Assertions.assertNotNull(gameDao.getGameDataList());
        gameDao.clear();
    }

    @Test
    void createGame(){
        Assertions.assertDoesNotThrow(() -> gameDao.createGame(new GameData(1234, "user",
                "user2", "name", new ChessGame())), "Clear is throwing an exception");

    }

    @Test
    void getGame() throws DataAccessException {
        gameDao.clear();
        gameDao.createGame(new GameData(123, "user",
                "user2", "name", new ChessGame()));
        Assertions.assertNotNull(gameDao.getGame(123));
        gameDao.clear();
    }

    @Test
    void updateGame() throws DataAccessException {

        Assertions.assertDoesNotThrow(() -> gameDao.updateGame(new GameData(123, "user",
                        "user2", "name", new ChessGame()),
                new GameData(1234, "user",
                        "user3", "name1", new ChessGame())), "Clear is throwing an exception");

    }

}
