package service;

import chess.ChessGame;
import dataaccess.MemoryGameDao;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private int i = 1234;
    public MemoryGameDao gameDao;

    public GameService(){
        gameDao = new MemoryGameDao();
    }

    MemoryGameDao getGameDao(){
        return gameDao;
    }

    void setGameDao(MemoryGameDao newGameDao){
        gameDao = newGameDao;
    }

    public void clear(){
        gameDao.clear();
    }

    public Collection<GameData> getAllGames(){
        return gameDao.getGameDataList();
    }

    public int createGame(String gameName){
        ++i;
        GameData newGame = new GameData(i,"", "", gameName, new ChessGame() );
        return gameDao.createGame(newGame);
    }

    public GameData getGame(String gameID){
        int gameID2 = Integer.parseInt(gameID);
        return gameDao.getGame(gameID2);
    }
}
