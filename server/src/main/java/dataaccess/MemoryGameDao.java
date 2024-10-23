package dataaccess;

import model.GameData;

import java.util.Collection;

public class MemoryGameDao {
    Collection<GameData> games;

    //DataAccess
    Collection<GameData> getGameDataList(){
        return games;
    }

    int createGame(GameData newGame){
        games.add(newGame);
        return newGame.gameID();
    }

    GameData getGame(int gameID){
        for (GameData game: games){
            if (game.gameID() == gameID){
                return game;
            }
        }
        return null;
    }

    void updateGame(GameData updatedGame){
        for (GameData game: games){
            if (game.equals(updatedGame)){
                games.remove(game);
                games.add(updatedGame);
            }
        }
    }

    void clear(){
        games.clear();
    }
}
