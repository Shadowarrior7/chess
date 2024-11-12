package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.Server;

public class NegativeTests {
    Server server = new Server();
    SQLGameDao gameDao = new SQLGameDao();
    SQLAuthDao authDao = new SQLAuthDao();
    SQLUserDao userDao = new SQLUserDao();
    DatabaseManager database = new DatabaseManager();

    public NegativeTests() throws DataAccessException {
    }

    //auth

    @Test
    public void addAuth() throws DataAccessException {
        String token = authDao.addAuthData("user");
        AuthData auth2 = authDao.getAuthData(token);
        Assertions.assertNotEquals(token + "1", auth2.authToken());
    }

    @Test
    void testGenerateToken() {
        String token = SQLAuthDao.generateToken();
        Assertions.assertNotNull(token);
    }

    @Test
    void deleteAuth() throws DataAccessException {
        String token = authDao.addAuthData("user");
        //authDao.deleteAuthData(token);
        AuthData auth = authDao.getAuthData(token);
        Assertions.assertNotNull(auth);
    }

    @Test
    void getAuth() throws DataAccessException {
        String token = authDao.addAuthData("user");
        AuthData auth2 = authDao.getAuthData(token + "1");
        Assertions.assertNull(auth2);
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
        Assertions.assertNull(gameDao.getGame(124));
        gameDao.clear();
    }

    @Test
    void updateGame2() throws DataAccessException {
        Assertions.assertDoesNotThrow(() -> gameDao.updateGame(new GameData(12323423, "user",
                        "user8", "name", new ChessGame()),
                new GameData(1234, "user",
                        "user23", "name1", new ChessGame())), "Clear is throwing an exception");

    }
}
