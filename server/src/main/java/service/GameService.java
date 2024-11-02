package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryGameDao;
import dataaccess.SQLGameDao;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private int i = 1234;
    public SQLGameDao gameDao;

    public GameService() {
        gameDao = new SQLGameDao();
    }

    SQLGameDao getGameDao(){
        return gameDao;
    }

    void setGameDao(SQLGameDao newGameDao){
        gameDao = newGameDao;
    }

    public void clear() throws DataAccessException {
        gameDao.clear();
    }

    public Collection<GameData> getAllGames() throws DataAccessException {
        return gameDao.getGameDataList();
    }

    public int createGame(String gameName) throws DataAccessException {
        ++i;
        GameData newGame = new GameData(i,"", "", gameName, new ChessGame() );
        return gameDao.createGame(newGame);
    }

    public GameData getGame(String gameID) throws DataAccessException {
        int gameID2 = Integer.parseInt(gameID);
        return gameDao.getGame(gameID2);
    }

    public void updateGame(GameData newGame, GameData oldGame) throws DataAccessException {
        gameDao.updateGame(newGame, oldGame);
    }
}
