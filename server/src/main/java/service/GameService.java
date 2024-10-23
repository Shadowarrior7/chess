package service;

import dataaccess.MemoryGameDao;
import model.AuthData;

public class GameService {
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
}
